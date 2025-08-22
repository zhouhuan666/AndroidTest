package com.gdet.testapp.someip.client;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.gdet.testapp.someip.car.CarServices;
import com.gdet.testapp.someip.network.SomeIpTransport;
import com.gdet.testapp.someip.protocol.MessageType;
import com.gdet.testapp.someip.protocol.ReturnCode;
import com.gdet.testapp.someip.protocol.SomeIpMessage;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * SOME/IP服务消费者
 * 实现服务发现、方法调用和事件订阅
 * 
 * @author Android Team
 * @version 1.0
 */
public class SomeIpServiceConsumer {
    
    private static final String TAG = "SomeIpServiceConsumer";
    
    // 传输层
    private final SomeIpTransport transport;
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private final AtomicInteger sessionIdGenerator = new AtomicInteger(0);
    private final short clientId;
    
    // 线程管理
    private HandlerThread backgroundThread;
    private Handler backgroundHandler;
    
    // 服务发现
    private final ConcurrentHashMap<Short, ServiceInfo> availableServices = new ConcurrentHashMap<>();
    private final Object serviceDiscoveryLock = new Object();
    
    // 请求-响应映射
    private final ConcurrentHashMap<String, CompletableFuture<SomeIpMessage>> pendingRequests = new ConcurrentHashMap<>();
    
    // 事件监听器
    private final ConcurrentHashMap<Short, EventListener> eventListeners = new ConcurrentHashMap<>();
    
    /**
     * 服务信息数据类
     */
    public static class ServiceInfo {
        private final short serviceId;
        private final short instanceId;
        private final String address;
        private final int port;
        private final boolean isAvailable;
        
        public ServiceInfo(short serviceId, short instanceId, String address, int port, boolean isAvailable) {
            this.serviceId = serviceId;
            this.instanceId = instanceId;
            this.address = address;
            this.port = port;
            this.isAvailable = isAvailable;
        }
        
        // Getter方法
        public short getServiceId() { return serviceId; }
        public short getInstanceId() { return instanceId; }
        public String getAddress() { return address; }
        public int getPort() { return port; }
        public boolean isAvailable() { return isAvailable; }
        
        @Override
        @NonNull
        public String toString() {
            return "ServiceInfo{" +
                    "serviceId=0x" + Integer.toHexString(serviceId & 0xFFFF) +
                    ", instanceId=" + instanceId +
                    ", address='" + address + '\'' +
                    ", port=" + port +
                    ", isAvailable=" + isAvailable +
                    '}';
        }
    }
    
    /**
     * 事件监听器接口
     */
    public interface EventListener {
        /**
         * 处理接收到的事件
         * @param message 事件消息
         */
        void onEventReceived(@NonNull SomeIpMessage message);
    }
    
    /**
     * 构造函数
     * @param transport SOME/IP传输层
     */
    public SomeIpServiceConsumer(@NonNull SomeIpTransport transport) {
        this.transport = transport;
        this.clientId = (short) (Math.random() * 65535);
    }
    
    /**
     * 启动服务消费者
     */
    public void start() {
        if (isRunning.get()) {
            Log.w(TAG, "Service consumer is already running");
            return;
        }
        
        try {
            // 创建后台线程
            backgroundThread = new HandlerThread("SomeIpConsumer");
            backgroundThread.start();
            backgroundHandler = new Handler(backgroundThread.getLooper());
            
            // 注册消息监听器
            transport.addMessageListener("consumer_" + clientId, this::handleIncomingMessage);
            
            isRunning.set(true);
            
            // 启动服务发现
            startServiceDiscovery();
            
            Log.d(TAG, "Service consumer started with client ID: 0x" + 
                  Integer.toHexString(clientId & 0xFFFF));
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to start service consumer", e);
            cleanup();
            throw e;
        }
    }
    
    /**
     * 停止服务消费者
     */
    public void stop() {
        if (!isRunning.compareAndSet(true, false)) {
            Log.w(TAG, "Service consumer is not running");
            return;
        }
        
        // 移除消息监听器
        transport.removeMessageListener("consumer_" + clientId);
        
        // 取消所有待处理的请求
        for (CompletableFuture<SomeIpMessage> future : pendingRequests.values()) {
            future.cancel(true);
        }
        pendingRequests.clear();
        
        cleanup();
        Log.d(TAG, "Service consumer stopped");
    }
    
    /**
     * 发现指定的服务
     * @param serviceId 服务ID
     * @param timeoutSeconds 超时时间（秒）
     * @return 服务信息，如果未找到返回null
     */
    @Nullable
    public ServiceInfo discoverService(short serviceId, int timeoutSeconds) {
        synchronized (serviceDiscoveryLock) {
            // 检查是否已经发现该服务
            ServiceInfo existingService = availableServices.get(serviceId);
            if (existingService != null) {
                return existingService;
            }
            
            try {
                // 发送服务发现请求
                SomeIpMessage discoveryMessage = new SomeIpMessage(
                        serviceId, (short) 0xFFFF, clientId, generateSessionId(),
                        MessageType.REQUEST, ReturnCode.OK
                );
                
                try {
                    transport.sendMulticastMessage(discoveryMessage);
                    Log.d(TAG, "Service discovery request sent for service 0x" + 
                          Integer.toHexString(serviceId & 0xFFFF));
                } catch (Exception e) {
                    Log.w(TAG, "Multicast service discovery failed, using fallback");
                    // 降级方案：创建一个本地服务信息
                    ServiceInfo fallbackService = createFallbackServiceInfo(serviceId);
                    if (fallbackService != null) {
                        availableServices.put(serviceId, fallbackService);
                        return fallbackService;
                    }
                }
                
                // 等待服务响应
                long startTime = System.currentTimeMillis();
                long timeoutMs = timeoutSeconds * 1000L;
                
                while (System.currentTimeMillis() - startTime < timeoutMs) {
                    ServiceInfo discoveredService = availableServices.get(serviceId);
                    if (discoveredService != null) {
                        return discoveredService;
                    }
                    
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
                
                // 如果通过组播发现失败，尝试fallback方案
                ServiceInfo fallbackService = createFallbackServiceInfo(serviceId);
                if (fallbackService != null) {
                    availableServices.put(serviceId, fallbackService);
                    Log.w(TAG, "Using fallback service info for service 0x" + 
                          Integer.toHexString(serviceId & 0xFFFF));
                    return fallbackService;
                }
                
                Log.w(TAG, "Service discovery timeout for service 0x" + 
                      Integer.toHexString(serviceId & 0xFFFF));
                return null;
                
            } catch (Exception e) {
                Log.e(TAG, "Error in service discovery", e);
                return null;
            }
        }
    }
    
    /**
     * 调用远程方法
     * @param serviceId 服务ID
     * @param methodId 方法ID
     * @param payload 请求负载
     * @param timeoutSeconds 超时时间（秒）
     * @return 响应消息，如果失败返回null
     */
    @Nullable
    public SomeIpMessage callMethod(short serviceId, short methodId, @Nullable byte[] payload, int timeoutSeconds) {
        ServiceInfo serviceInfo = availableServices.get(serviceId);
        if (serviceInfo == null) {
            Log.w(TAG, "Service not available: 0x" + Integer.toHexString(serviceId & 0xFFFF));
            return null;
        }
        
        short sessionId = generateSessionId();
        String requestKey = serviceId + "_" + methodId + "_" + clientId + "_" + sessionId;
        
        SomeIpMessage request = new SomeIpMessage(
                serviceId, methodId, clientId, sessionId,
                MessageType.REQUEST, ReturnCode.OK, payload
        );
        
        CompletableFuture<SomeIpMessage> responseFuture = new CompletableFuture<>();
        pendingRequests.put(requestKey, responseFuture);
        
        try {
            transport.sendMessage(request, serviceInfo.getAddress(), serviceInfo.getPort());
            Log.d(TAG, String.format("Method call sent: service=0x%04X, method=0x%04X",
                    serviceId & 0xFFFF, methodId & 0xFFFF));
            
            return responseFuture.get(timeoutSeconds, TimeUnit.SECONDS);
            
        } catch (Exception e) {
            Log.w(TAG, String.format("Method call timeout or error: service=0x%04X, method=0x%04X",
                    serviceId & 0xFFFF, methodId & 0xFFFF), e);
            return null;
        } finally {
            pendingRequests.remove(requestKey);
        }
    }
    
    /**
     * 调用远程方法（使用默认超时）
     * @param serviceId 服务ID
     * @param methodId 方法ID
     * @param payload 请求负载
     * @return 响应消息，如果失败返回null
     */
    @Nullable
    public SomeIpMessage callMethod(short serviceId, short methodId, @Nullable byte[] payload) {
        return callMethod(serviceId, methodId, payload, CarServices.ProtocolConfig.REQUEST_TIMEOUT_MS / 1000);
    }
    
    /**
     * 调用远程方法（无负载）
     * @param serviceId 服务ID
     * @param methodId 方法ID
     * @return 响应消息，如果失败返回null
     */
    @Nullable
    public SomeIpMessage callMethod(short serviceId, short methodId) {
        return callMethod(serviceId, methodId, null);
    }
    
    /**
     * 调用Fire-and-Forget方法
     * @param serviceId 服务ID
     * @param methodId 方法ID
     * @param payload 请求负载
     * @return true表示发送成功，false表示失败
     */
    public boolean callFireAndForgetMethod(short serviceId, short methodId, @Nullable byte[] payload) {
        ServiceInfo serviceInfo = availableServices.get(serviceId);
        if (serviceInfo == null) {
            Log.w(TAG, "Service not available: 0x" + Integer.toHexString(serviceId & 0xFFFF));
            return false;
        }
        
        SomeIpMessage request = new SomeIpMessage(
                serviceId, methodId, clientId, generateSessionId(),
                MessageType.REQUEST_NO_RETURN, ReturnCode.OK, payload
        );
        
        try {
            transport.sendMessage(request, serviceInfo.getAddress(), serviceInfo.getPort());
            Log.d(TAG, String.format("Fire-and-forget method call sent: service=0x%04X, method=0x%04X",
                    serviceId & 0xFFFF, methodId & 0xFFFF));
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Failed to send fire-and-forget method call", e);
            return false;
        }
    }
    
    /**
     * 订阅事件
     * @param serviceId 服务ID
     * @param eventId 事件ID
     * @param listener 事件监听器
     * @return true表示订阅成功，false表示失败
     */
    public boolean subscribeEvent(short serviceId, short eventId, @NonNull EventListener listener) {
        ServiceInfo serviceInfo = availableServices.get(serviceId);
        if (serviceInfo == null) {
            Log.w(TAG, "Service not available: 0x" + Integer.toHexString(serviceId & 0xFFFF));
            return false;
        }
        
        // 添加事件监听器
        eventListeners.put(eventId, listener);
        
        SomeIpMessage subscribeMessage = new SomeIpMessage(
                serviceId, eventId, clientId, generateSessionId(),
                MessageType.SUBSCRIBE, ReturnCode.OK
        );
        
        try {
            transport.sendMessage(subscribeMessage, serviceInfo.getAddress(), serviceInfo.getPort());
            Log.d(TAG, String.format("Event subscription sent: service=0x%04X, event=0x%04X",
                    serviceId & 0xFFFF, eventId & 0xFFFF));
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Failed to subscribe event", e);
            eventListeners.remove(eventId);
            return false;
        }
    }
    
    /**
     * 取消事件订阅
     * @param serviceId 服务ID
     * @param eventId 事件ID
     * @return true表示取消成功，false表示失败
     */
    public boolean unsubscribeEvent(short serviceId, short eventId) {
        ServiceInfo serviceInfo = availableServices.get(serviceId);
        if (serviceInfo == null) {
            Log.w(TAG, "Service not available: 0x" + Integer.toHexString(serviceId & 0xFFFF));
            return false;
        }
        
        // 移除事件监听器
        eventListeners.remove(eventId);
        
        SomeIpMessage unsubscribeMessage = new SomeIpMessage(
                serviceId, eventId, clientId, generateSessionId(),
                MessageType.UNSUBSCRIBE, ReturnCode.OK
        );
        
        try {
            transport.sendMessage(unsubscribeMessage, serviceInfo.getAddress(), serviceInfo.getPort());
            Log.d(TAG, String.format("Event unsubscription sent: service=0x%04X, event=0x%04X",
                    serviceId & 0xFFFF, eventId & 0xFFFF));
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Failed to unsubscribe event", e);
            return false;
        }
    }
    
    /**
     * 获取可用服务列表
     * @return 服务信息列表
     */
    @NonNull
    public ServiceInfo[] getAvailableServices() {
        return availableServices.values().toArray(new ServiceInfo[0]);
    }
    
    /**
     * 检查服务是否可用
     * @param serviceId 服务ID
     * @return true表示可用，false表示不可用
     */
    public boolean isServiceAvailable(short serviceId) {
        ServiceInfo serviceInfo = availableServices.get(serviceId);
        return serviceInfo != null && serviceInfo.isAvailable();
    }
    
    /**
     * 获取客户端ID
     * @return 客户端ID
     */
    public short getClientId() {
        return clientId;
    }
    
    /**
     * 检查消费者是否正在运行
     * @return true表示正在运行
     */
    public boolean isRunning() {
        return isRunning.get();
    }
    
    // ================= 私有方法 =================
    
    /**
     * 处理接收到的消息
     * @param message 接收到的消息
     */
    private void handleIncomingMessage(@NonNull SomeIpMessage message) {
        backgroundHandler.post(() -> {
            try {
                switch (message.getMessageType()) {
                    case RESPONSE:
                    case ERROR:
                        handleResponse(message);
                        break;
                    case NOTIFICATION:
                        handleNotification(message);
                        break;
                    case SUBSCRIBE_ACK:
                    case UNSUBSCRIBE_ACK:
                        handleSubscriptionResponse(message);
                        break;
                    default:
                        Log.d(TAG, "Unhandled message type: " + message.getMessageType());
                        break;
                }
            } catch (Exception e) {
                Log.e(TAG, "Error handling incoming message", e);
            }
        });
    }
    
    /**
     * 处理响应消息
     * @param message 响应消息
     */
    private void handleResponse(@NonNull SomeIpMessage message) {
        String requestKey = message.getServiceId() + "_" + message.getMethodId() + "_" +
                           message.getClientId() + "_" + message.getSessionId();
        
        CompletableFuture<SomeIpMessage> future = pendingRequests.get(requestKey);
        if (future != null) {
            future.complete(message);
            Log.d(TAG, String.format("Response received: service=0x%04X, method=0x%04X",
                    message.getServiceId() & 0xFFFF, message.getMethodId() & 0xFFFF));
        } else {
            Log.w(TAG, "No pending request found for response: " + requestKey);
        }
    }
    
    /**
     * 处理通知消息（事件）
     * @param message 通知消息
     */
    private void handleNotification(@NonNull SomeIpMessage message) {
        short methodId = message.getMethodId();
        
        switch (methodId) {
            case (short) 0xFFFF:
                handleServiceAvailableNotification(message);
                break;
            case (short) 0xFFFE:
                handleServiceUnavailableNotification(message);
                break;
            default:
                handleEventNotification(message);
                break;
        }
    }
    
    /**
     * 处理服务可用通知
     * @param message 通知消息
     */
    private void handleServiceAvailableNotification(@NonNull SomeIpMessage message) {
        ServiceInfo serviceInfo = new ServiceInfo(
                message.getServiceId(),
                (short) 1, // 默认实例ID
                transport.getLocalAddress(), // 这里需要从消息中获取实际地址
                CarServices.NetworkConfig.DEFAULT_SERVICE_PORT, // 默认端口
                true
        );
        
        availableServices.put(message.getServiceId(), serviceInfo);
        Log.d(TAG, "Service available: 0x" + Integer.toHexString(message.getServiceId() & 0xFFFF));
    }
    
    /**
     * 处理服务不可用通知
     * @param message 通知消息
     */
    private void handleServiceUnavailableNotification(@NonNull SomeIpMessage message) {
        availableServices.remove(message.getServiceId());
        Log.d(TAG, "Service unavailable: 0x" + Integer.toHexString(message.getServiceId() & 0xFFFF));
    }
    
    /**
     * 处理事件通知
     * @param message 事件消息
     */
    private void handleEventNotification(@NonNull SomeIpMessage message) {
        EventListener listener = eventListeners.get(message.getMethodId());
        if (listener == null) {
            Log.w(TAG, "No listeners for event: 0x" + 
                  Integer.toHexString(message.getMethodId() & 0xFFFF));
            return;
        }
        
        // 在主线程中通知监听器
        new Handler(Looper.getMainLooper()).post(() -> {
            try {
                listener.onEventReceived(message);
            } catch (Exception e) {
                Log.e(TAG, "Error in event listener", e);
            }
        });
        
        Log.d(TAG, "Event notification handled: event=0x" + 
              Integer.toHexString(message.getMethodId() & 0xFFFF));
    }
    
    /**
     * 处理订阅响应
     * @param message 订阅响应消息
     */
    private void handleSubscriptionResponse(@NonNull SomeIpMessage message) {
        String action = (message.getMessageType() == MessageType.SUBSCRIBE_ACK) ? 
                        "subscription" : "unsubscription";
        Log.d(TAG, "Event " + action + " confirmed: event=0x" + 
              Integer.toHexString(message.getMethodId() & 0xFFFF));
    }
    
    /**
     * 启动服务发现
     */
    private void startServiceDiscovery() {
        backgroundHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isRunning.get()) {
                    return;
                }
                
                // 这里可以实现主动的服务发现逻辑
                // 例如定期广播服务发现消息
                
                // 5秒后再次执行
                backgroundHandler.postDelayed(this, 5000);
            }
        }, 1000); // 延迟1秒开始
    }
    
    /**
     * 创建降级服务信息
     * 当组播服务发现失败时使用
     * @param serviceId 服务ID
     * @return 服务信息，如果不支持降级则返回null
     */
    @Nullable
    private ServiceInfo createFallbackServiceInfo(short serviceId) {
        try {
            // 对于本地演示，假设服务在同一设备上的默认端口
            String localAddress = transport.getLocalAddress();
            int servicePort = CarServices.NetworkConfig.DEFAULT_SERVICE_PORT;
            
            // 根据服务ID确定是否支持fallback
            switch (serviceId) {
                case CarServices.HVAC_SERVICE_ID:
                case CarServices.DOOR_SERVICE_ID:
                case CarServices.ENGINE_SERVICE_ID:
                case CarServices.NAVIGATION_SERVICE_ID:
                case CarServices.MEDIA_SERVICE_ID:
                    return new ServiceInfo(serviceId, (short) 1, localAddress, servicePort, true);
                default:
                    return null;
            }
        } catch (Exception e) {
            Log.w(TAG, "Failed to create fallback service info", e);
            return null;
        }
    }
    
    /**
     * 生成新的会话ID
     * @return 会话ID
     */
    private short generateSessionId() {
        return (short) sessionIdGenerator.incrementAndGet();
    }
    
    /**
     * 清理资源
     */
    private void cleanup() {
        availableServices.clear();
        pendingRequests.clear();
        eventListeners.clear();
        
        if (backgroundThread != null) {
            backgroundThread.quitSafely();
            try {
                backgroundThread.join(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            backgroundThread = null;
            backgroundHandler = null;
        }
    }
} 