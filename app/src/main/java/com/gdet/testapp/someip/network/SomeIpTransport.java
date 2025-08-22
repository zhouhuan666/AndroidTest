package com.gdet.testapp.someip.network;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.gdet.testapp.someip.car.CarServices;
import com.gdet.testapp.someip.protocol.SomeIpMessage;

import java.io.IOException;
import java.net.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * SOME/IP网络传输层
 * 负责UDP数据包的发送和接收
 * 
 * @author Android Team
 * @version 1.0
 */
public class SomeIpTransport {
    
    private static final String TAG = "SomeIpTransport";
    
    // 网络配置
    private static final String MULTICAST_ADDRESS = CarServices.NetworkConfig.MULTICAST_ADDRESS;
    private static final int SERVICE_DISCOVERY_PORT = CarServices.NetworkConfig.SERVICE_DISCOVERY_PORT;
    private static final int MAX_PACKET_SIZE = 1500; // MTU大小
    
    // 网络组件
    private DatagramSocket udpSocket;
    private MulticastSocket multicastSocket;
    private ExecutorService executorService;
    private HandlerThread backgroundThread;
    private Handler backgroundHandler;
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    
    // 消息监听器
    private final ConcurrentHashMap<String, MessageListener> messageListeners = new ConcurrentHashMap<>();
    
    // 本地端口
    private int localPort;
    
    /**
     * 消息监听器接口
     */
    public interface MessageListener {
        /**
         * 处理接收到的消息
         * @param message SOME/IP消息
         */
        void onMessageReceived(@NonNull SomeIpMessage message);
    }
    
    /**
     * 启动传输层
     * @param port 本地端口
     * @throws IOException 如果网络初始化失败
     */
    public void start(int port) throws IOException {
        if (isRunning.get()) {
            Log.w(TAG, "Transport is already running");
            return;
        }
        
        this.localPort = port;
        
        try {
            // 创建UDP socket
            udpSocket = new DatagramSocket(port);
            Log.d(TAG, "UDP socket started on port " + port);
            
            // 尝试创建组播socket用于服务发现
            boolean multicastSupported = initializeMulticast();
            if (!multicastSupported) {
                Log.w(TAG, "Multicast not supported, using unicast only");
            }
            
            // 创建后台线程和执行器
            backgroundThread = new HandlerThread("SomeIpTransport");
            backgroundThread.start();
            backgroundHandler = new Handler(backgroundThread.getLooper());
            executorService = Executors.newCachedThreadPool();
            
            isRunning.set(true);
            
            // 启动接收
            startReceiving();
            if (multicastSupported) {
                startMulticastReceiving();
            }
            
            Log.d(TAG, "SOME/IP transport started successfully (multicast: " + multicastSupported + ")");
            
        } catch (IOException e) {
            Log.e(TAG, "Failed to start transport", e);
            cleanup();
            throw e;
        }
    }
    
    /**
     * 停止传输层
     */
    public void stop() {
        if (!isRunning.compareAndSet(true, false)) {
            Log.w(TAG, "Transport is not running");
            return;
        }
        
        cleanup();
        Log.d(TAG, "SOME/IP transport stopped");
    }
    
    /**
     * 发送SOME/IP消息
     * @param message 要发送的消息
     * @param targetAddress 目标地址
     * @param targetPort 目标端口
     * @throws IOException 如果发送失败
     */
    public void sendMessage(@NonNull SomeIpMessage message, @NonNull String targetAddress, int targetPort) throws IOException {
        if (!isRunning.get()) {
            throw new IllegalStateException("Transport is not running");
        }
        
        byte[] data = message.toByteArray();
        DatagramPacket packet = new DatagramPacket(
                data, 
                data.length, 
                InetAddress.getByName(targetAddress), 
                targetPort
        );
        
        backgroundHandler.post(() -> {
            try {
                udpSocket.send(packet);
                Log.d(TAG, String.format("Sent message to %s:%d, service=0x%04X, method=0x%04X",
                        targetAddress, targetPort, 
                        message.getServiceId() & 0xFFFF, 
                        message.getMethodId() & 0xFFFF));
            } catch (IOException e) {
                Log.e(TAG, "Failed to send message", e);
            }
        });
    }
    
    /**
     * 尝试初始化组播功能
     * @return true表示组播初始化成功，false表示不支持组播
     */
    private boolean initializeMulticast() {
        try {
            multicastSocket = new MulticastSocket(SERVICE_DISCOVERY_PORT);
            InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);
            multicastSocket.joinGroup(group);
            Log.d(TAG, "Joined multicast group " + MULTICAST_ADDRESS + ":" + SERVICE_DISCOVERY_PORT);
            return true;
        } catch (IOException e) {
            Log.w(TAG, "Multicast initialization failed: " + e.getMessage());
            if (multicastSocket != null) {
                multicastSocket.close();
                multicastSocket = null;
            }
            return false;
        }
    }
    
    /**
     * 发送组播消息（用于服务发现）
     * @param message 要发送的消息
     * @throws IOException 如果发送失败
     */
    public void sendMulticastMessage(@NonNull SomeIpMessage message) throws IOException {
        if (!isRunning.get()) {
            throw new IllegalStateException("Transport is not running");
        }
        
        if (multicastSocket == null) {
            Log.w(TAG, "Multicast not supported, cannot send multicast message");
            // 降级为广播或者直接忽略
            return;
        }
        
        byte[] data = message.toByteArray();
        DatagramPacket packet = new DatagramPacket(
                data, 
                data.length, 
                InetAddress.getByName(MULTICAST_ADDRESS), 
                SERVICE_DISCOVERY_PORT
        );
        
        backgroundHandler.post(() -> {
            try {
                if (multicastSocket != null) {
                    multicastSocket.send(packet);
                    Log.d(TAG, "Sent multicast message, service=0x" + 
                          Integer.toHexString(message.getServiceId() & 0xFFFF));
                }
            } catch (IOException e) {
                Log.e(TAG, "Failed to send multicast message", e);
            }
        });
    }
    
    /**
     * 注册消息监听器
     * @param key 监听器标识
     * @param listener 消息监听器
     */
    public void addMessageListener(@NonNull String key, @NonNull MessageListener listener) {
        messageListeners.put(key, listener);
        Log.d(TAG, "Added message listener: " + key);
    }
    
    /**
     * 移除消息监听器
     * @param key 监听器标识
     */
    public void removeMessageListener(@NonNull String key) {
        MessageListener removed = messageListeners.remove(key);
        if (removed != null) {
            Log.d(TAG, "Removed message listener: " + key);
        }
    }
    
    /**
     * 启动接收UDP消息
     */
    private void startReceiving() {
        executorService.execute(() -> {
            byte[] buffer = new byte[MAX_PACKET_SIZE];
            
            while (isRunning.get()) {
                try {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    udpSocket.receive(packet);
                    
                    // 复制实际接收到的数据
                    byte[] data = new byte[packet.getLength()];
                    System.arraycopy(packet.getData(), packet.getOffset(), data, 0, packet.getLength());
                    
                    // 解析并处理消息
                    processReceivedMessage(data, packet.getAddress(), packet.getPort());
                    
                } catch (SocketException e) {
                    if (isRunning.get()) {
                        Log.e(TAG, "Socket error in receiving", e);
                    }
                    break;
                } catch (IOException e) {
                    Log.e(TAG, "Error in receiving message", e);
                } catch (Exception e) {
                    Log.e(TAG, "Unexpected error in message receiving", e);
                }
            }
            
            Log.d(TAG, "UDP receiving thread stopped");
        });
    }
    
    /**
     * 启动接收组播消息
     */
    private void startMulticastReceiving() {
        if (multicastSocket == null) {
            Log.w(TAG, "Multicast socket not available, skipping multicast receiving");
            return;
        }
        
        executorService.execute(() -> {
            byte[] buffer = new byte[MAX_PACKET_SIZE];
            
            while (isRunning.get() && multicastSocket != null) {
                try {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    multicastSocket.receive(packet);
                    
                    // 复制实际接收到的数据
                    byte[] data = new byte[packet.getLength()];
                    System.arraycopy(packet.getData(), packet.getOffset(), data, 0, packet.getLength());
                    
                    // 解析并处理消息
                    processReceivedMessage(data, packet.getAddress(), packet.getPort());
                    
                } catch (SocketException e) {
                    if (isRunning.get()) {
                        Log.e(TAG, "Socket error in multicast receiving", e);
                    }
                    break;
                } catch (IOException e) {
                    Log.e(TAG, "Error in receiving multicast message", e);
                } catch (Exception e) {
                    Log.e(TAG, "Unexpected error in multicast receiving", e);
                }
            }
            
            Log.d(TAG, "Multicast receiving thread stopped");
        });
    }
    
    /**
     * 处理接收到的消息
     * @param data 消息数据
     * @param senderAddress 发送者地址
     * @param senderPort 发送者端口
     */
    private void processReceivedMessage(@NonNull byte[] data, @NonNull InetAddress senderAddress, int senderPort) {
        try {
            SomeIpMessage message = SomeIpMessage.fromByteArray(data);
            
            Log.d(TAG, String.format("Received message from %s:%d, service=0x%04X, method=0x%04X",
                    senderAddress.getHostAddress(), senderPort,
                    message.getServiceId() & 0xFFFF,
                    message.getMethodId() & 0xFFFF));
            
            // 通知所有监听器
            notifyListeners(message);
            
        } catch (IllegalArgumentException e) {
            Log.w(TAG, "Failed to parse received message", e);
        } catch (Exception e) {
            Log.e(TAG, "Error processing received message", e);
        }
    }
    
    /**
     * 通知所有监听器
     * @param message 接收到的消息
     */
    private void notifyListeners(@NonNull SomeIpMessage message) {
        // 在主线程中通知监听器
        new Handler(Looper.getMainLooper()).post(() -> {
            for (MessageListener listener : messageListeners.values()) {
                try {
                    listener.onMessageReceived(message);
                } catch (Exception e) {
                    Log.e(TAG, "Error in message listener", e);
                }
            }
        });
    }
    
    /**
     * 获取本地IP地址
     * @return IP地址字符串
     */
    @NonNull
    public String getLocalAddress() {
        try {
            // 创建临时socket来获取本地地址
            try (DatagramSocket socket = new DatagramSocket()) {
                socket.connect(InetAddress.getByName("8.8.8.8"), 80);
                return socket.getLocalAddress().getHostAddress();
            }
        } catch (Exception e) {
            Log.w(TAG, "Failed to get local address, using fallback", e);
            return "127.0.0.1";
        }
    }
    
    /**
     * 获取本地端口
     * @return 端口号
     */
    public int getLocalPort() {
        return localPort;
    }
    
    /**
     * 检查传输层是否正在运行
     * @return true表示正在运行
     */
    public boolean isRunning() {
        return isRunning.get();
    }
    
    /**
     * 清理资源
     */
    private void cleanup() {
        // 关闭socket
        if (udpSocket != null) {
            udpSocket.close();
            udpSocket = null;
        }
        
        if (multicastSocket != null) {
            multicastSocket.close();
            multicastSocket = null;
        }
        
        // 关闭执行器
        if (executorService != null) {
            executorService.shutdown();
            executorService = null;
        }
        
        // 关闭后台线程
        if (backgroundThread != null) {
            backgroundThread.quitSafely();
            try {
                backgroundThread.join(1000); // 等待最多1秒
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            backgroundThread = null;
            backgroundHandler = null;
        }
        
        // 清除监听器
        messageListeners.clear();
    }
    
    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        if (isRunning.get()) {
            Log.w(TAG, "Transport was not properly stopped, cleaning up in finalize");
            stop();
        }
    }
} 