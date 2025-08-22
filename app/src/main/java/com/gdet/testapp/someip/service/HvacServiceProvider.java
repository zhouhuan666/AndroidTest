package com.gdet.testapp.someip.service;

import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.gdet.testapp.someip.car.CarServices;
import com.gdet.testapp.someip.network.SomeIpTransport;
import com.gdet.testapp.someip.protocol.MessageType;
import com.gdet.testapp.someip.protocol.ReturnCode;
import com.gdet.testapp.someip.protocol.SomeIpMessage;
import com.google.gson.Gson;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * HVAC空调控制服务提供者
 * 实现车载空调的温度、风速、开关等功能
 * 
 * @author Android Team
 * @version 1.0
 */
public class HvacServiceProvider {
    
    private static final String TAG = "HvacServiceProvider";
    private static final short SERVICE_ID = CarServices.HVAC_SERVICE_ID;
    private static final short INSTANCE_ID = 1;
    
    // 网络传输
    private final SomeIpTransport transport;
    private final Gson gson = new Gson();
    
    // 线程管理
    private HandlerThread backgroundThread;
    private Handler backgroundHandler;
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private final AtomicInteger sessionIdGenerator = new AtomicInteger(0);
    
    // 方法处理器映射
    private final ConcurrentHashMap<Short, MethodHandler> methodHandlers = new ConcurrentHashMap<>();
    
    // 事件订阅者映射 (eventId -> set of subscribers)
    private final ConcurrentHashMap<Short, ConcurrentHashMap<String, String>> eventSubscribers = new ConcurrentHashMap<>();
    
    // 当前HVAC状态
    private HvacStatus currentStatus = new HvacStatus(22.0f, 3, false, true);
    
    /**
     * HVAC状态数据类
     */
    public static class HvacStatus {
        private final float temperature;
        private final int fanSpeed;
        private final boolean isAcOn;
        private final boolean isAutoMode;
        
        public HvacStatus(float temperature, int fanSpeed, boolean isAcOn, boolean isAutoMode) {
            this.temperature = temperature;
            this.fanSpeed = fanSpeed;
            this.isAcOn = isAcOn;
            this.isAutoMode = isAutoMode;
        }
        
        // Getter方法
        public float getTemperature() { return temperature; }
        public int getFanSpeed() { return fanSpeed; }
        public boolean isAcOn() { return isAcOn; }
        public boolean isAutoMode() { return isAutoMode; }
        
        // 创建修改后的副本
        public HvacStatus withTemperature(float newTemperature) {
            return new HvacStatus(newTemperature, fanSpeed, isAcOn, isAutoMode);
        }
        
        public HvacStatus withFanSpeed(int newFanSpeed) {
            return new HvacStatus(temperature, newFanSpeed, isAcOn, isAutoMode);
        }
        
        public HvacStatus withAcStatus(boolean newIsAcOn) {
            return new HvacStatus(temperature, fanSpeed, newIsAcOn, isAutoMode);
        }
        
        public HvacStatus withAutoMode(boolean newIsAutoMode) {
            return new HvacStatus(temperature, fanSpeed, isAcOn, newIsAutoMode);
        }
    }
    
    /**
     * 方法处理器接口
     */
    private interface MethodHandler {
        SomeIpMessage handle(@NonNull SomeIpMessage message);
    }
    
    /**
     * 构造函数
     * @param transport SOME/IP传输层
     */
    public HvacServiceProvider(@NonNull SomeIpTransport transport) {
        this.transport = transport;
    }
    
    /**
     * 启动HVAC服务提供者
     */
    public void start() {
        if (isRunning.get()) {
            Log.w(TAG, "HVAC service is already running");
            return;
        }
        
        try {
            // 创建后台线程
            backgroundThread = new HandlerThread("HvacService");
            backgroundThread.start();
            backgroundHandler = new Handler(backgroundThread.getLooper());
            
            // 注册方法处理器
            registerMethods();
            
            // 注册消息监听器
            transport.addMessageListener("service_" + SERVICE_ID, this::handleIncomingMessage);
            
            // 发送服务可用通知
            sendServiceAvailableNotification();
            
            // 启动状态监控
            startStatusMonitoring();
            
            isRunning.set(true);
            Log.d(TAG, "HVAC service provider started");
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to start HVAC service provider", e);
            cleanup();
            throw e;
        }
    }
    
    /**
     * 停止HVAC服务提供者
     */
    public void stop() {
        if (!isRunning.compareAndSet(true, false)) {
            Log.w(TAG, "HVAC service is not running");
            return;
        }
        
        try {
            // 发送服务不可用通知
            sendServiceUnavailableNotification();
        } catch (Exception e) {
            Log.e(TAG, "Error sending service unavailable notification", e);
        }
        
        // 移除消息监听器
        transport.removeMessageListener("service_" + SERVICE_ID);
        
        cleanup();
        Log.d(TAG, "HVAC service provider stopped");
    }
    
    /**
     * 注册所有HVAC相关的方法处理器
     */
    private void registerMethods() {
        methodHandlers.put(CarServices.HvacMethods.GET_TEMPERATURE, this::handleGetTemperature);
        methodHandlers.put(CarServices.HvacMethods.SET_TEMPERATURE, this::handleSetTemperature);
        methodHandlers.put(CarServices.HvacMethods.GET_FAN_SPEED, this::handleGetFanSpeed);
        methodHandlers.put(CarServices.HvacMethods.SET_FAN_SPEED, this::handleSetFanSpeed);
        methodHandlers.put(CarServices.HvacMethods.GET_AC_STATUS, this::handleGetAcStatus);
        methodHandlers.put(CarServices.HvacMethods.SET_AC_STATUS, this::handleSetAcStatus);
        
        Log.d(TAG, "Registered " + methodHandlers.size() + " method handlers");
    }
    
    /**
     * 处理传入的消息
     * @param message 接收到的消息
     */
    private void handleIncomingMessage(@NonNull SomeIpMessage message) {
        if (message.getServiceId() != SERVICE_ID) {
            return;
        }
        
        backgroundHandler.post(() -> {
            try {
                switch (message.getMessageType()) {
                    case REQUEST:
                        handleRequest(message);
                        break;
                    case REQUEST_NO_RETURN:
                        handleFireAndForgetRequest(message);
                        break;
                    case SUBSCRIBE:
                        handleSubscribe(message);
                        break;
                    case UNSUBSCRIBE:
                        handleUnsubscribe(message);
                        break;
                    default:
                        Log.w(TAG, "Unsupported message type: " + message.getMessageType());
                        break;
                }
            } catch (Exception e) {
                Log.e(TAG, "Error handling message", e);
                try {
                    sendErrorResponse(message, ReturnCode.NOT_OK);
                } catch (Exception ex) {
                    Log.e(TAG, "Failed to send error response", ex);
                }
            }
        });
    }
    
    /**
     * 处理请求消息
     * @param message 请求消息
     */
    private void handleRequest(@NonNull SomeIpMessage message) {
        MethodHandler handler = methodHandlers.get(message.getMethodId());
        
        if (handler == null) {
            Log.w(TAG, "No handler for method 0x" + Integer.toHexString(message.getMethodId() & 0xFFFF));
            sendErrorResponse(message, ReturnCode.UNKNOWN_METHOD);
            return;
        }
        
        try {
            SomeIpMessage response = handler.handle(message);
            if (response != null) {
                sendResponse(response);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in method handler", e);
            sendErrorResponse(message, ReturnCode.NOT_OK);
        }
    }
    
    /**
     * 处理Fire-and-Forget请求
     * @param message 请求消息
     */
    private void handleFireAndForgetRequest(@NonNull SomeIpMessage message) {
        MethodHandler handler = methodHandlers.get(message.getMethodId());
        
        if (handler == null) {
            Log.w(TAG, "No handler for fire-and-forget method 0x" + 
                  Integer.toHexString(message.getMethodId() & 0xFFFF));
            return;
        }
        
        try {
            handler.handle(message);
        } catch (Exception e) {
            Log.e(TAG, "Error in fire-and-forget method handler", e);
        }
    }
    
    /**
     * 处理事件订阅
     * @param message 订阅消息
     */
    private void handleSubscribe(@NonNull SomeIpMessage message) {
        short eventId = message.getMethodId();
        String subscriberKey = message.getClientId() + "_" + message.getSessionId();
        
        eventSubscribers.computeIfAbsent(eventId, k -> new ConcurrentHashMap<>())
                       .put(subscriberKey, "subscribed");
        
        Log.d(TAG, "Event subscription added for event 0x" + Integer.toHexString(eventId & 0xFFFF));
        
        // 发送订阅确认
        SomeIpMessage ackMessage = new SomeIpMessage(
                SERVICE_ID, eventId, message.getClientId(), message.getSessionId(),
                MessageType.SUBSCRIBE_ACK, ReturnCode.OK
        );
        
        sendResponse(ackMessage);
    }
    
    /**
     * 处理事件取消订阅
     * @param message 取消订阅消息
     */
    private void handleUnsubscribe(@NonNull SomeIpMessage message) {
        short eventId = message.getMethodId();
        String subscriberKey = message.getClientId() + "_" + message.getSessionId();
        
        ConcurrentHashMap<String, String> subscribers = eventSubscribers.get(eventId);
        if (subscribers != null) {
            subscribers.remove(subscriberKey);
        }
        
        Log.d(TAG, "Event subscription removed for event 0x" + Integer.toHexString(eventId & 0xFFFF));
        
        // 发送取消订阅确认
        SomeIpMessage ackMessage = new SomeIpMessage(
                SERVICE_ID, eventId, message.getClientId(), message.getSessionId(),
                MessageType.UNSUBSCRIBE_ACK, ReturnCode.OK
        );
        
        sendResponse(ackMessage);
    }
    
    // ================= 方法处理器实现 =================
    
    /**
     * 处理获取温度请求
     */
    private SomeIpMessage handleGetTemperature(@NonNull SomeIpMessage message) {
        Log.d(TAG, "Handle get temperature request");
        
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.order(ByteOrder.BIG_ENDIAN);
        buffer.putFloat(currentStatus.getTemperature());
        
        return message.createResponse(buffer.array());
    }
    
    /**
     * 处理设置温度请求
     */
    private SomeIpMessage handleSetTemperature(@NonNull SomeIpMessage message) {
        Log.d(TAG, "Handle set temperature request");
        
        byte[] payload = message.getPayload();
        if (payload.length < 4) {
            return message.createErrorResponse(ReturnCode.MALFORMED_MESSAGE);
        }
        
        ByteBuffer buffer = ByteBuffer.wrap(payload);
        buffer.order(ByteOrder.BIG_ENDIAN);
        float temperature = buffer.getFloat();
        
        if (temperature < 16.0f || temperature > 30.0f) {
            return message.createErrorResponse(ReturnCode.NOT_OK);
        }
        
        float oldTemperature = currentStatus.getTemperature();
        currentStatus = currentStatus.withTemperature(temperature);
        
        Log.d(TAG, "Temperature changed from " + oldTemperature + " to " + temperature);
        
        // 发送温度变化事件
        sendTemperatureChangedEvent(temperature);
        
        return message.createResponse(null);
    }
    
    /**
     * 处理获取风速请求
     */
    private SomeIpMessage handleGetFanSpeed(@NonNull SomeIpMessage message) {
        Log.d(TAG, "Handle get fan speed request");
        
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.order(ByteOrder.BIG_ENDIAN);
        buffer.putInt(currentStatus.getFanSpeed());
        
        return message.createResponse(buffer.array());
    }
    
    /**
     * 处理设置风速请求
     */
    private SomeIpMessage handleSetFanSpeed(@NonNull SomeIpMessage message) {
        Log.d(TAG, "Handle set fan speed request");
        
        byte[] payload = message.getPayload();
        if (payload.length < 4) {
            return message.createErrorResponse(ReturnCode.MALFORMED_MESSAGE);
        }
        
        ByteBuffer buffer = ByteBuffer.wrap(payload);
        buffer.order(ByteOrder.BIG_ENDIAN);
        int fanSpeed = buffer.getInt();
        
        if (fanSpeed < 0 || fanSpeed > 7) {
            return message.createErrorResponse(ReturnCode.NOT_OK);
        }
        
        int oldFanSpeed = currentStatus.getFanSpeed();
        currentStatus = currentStatus.withFanSpeed(fanSpeed);
        
        Log.d(TAG, "Fan speed changed from " + oldFanSpeed + " to " + fanSpeed);
        
        // 发送风速变化事件
        sendFanSpeedChangedEvent(fanSpeed);
        
        return message.createResponse(null);
    }
    
    /**
     * 处理获取空调状态请求
     */
    private SomeIpMessage handleGetAcStatus(@NonNull SomeIpMessage message) {
        Log.d(TAG, "Handle get AC status request");
        
        String statusJson = gson.toJson(currentStatus);
        byte[] statusBytes = statusJson.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        
        return message.createResponse(statusBytes);
    }
    
    /**
     * 处理设置空调状态请求
     */
    private SomeIpMessage handleSetAcStatus(@NonNull SomeIpMessage message) {
        Log.d(TAG, "Handle set AC status request");
        
        byte[] payload = message.getPayload();
        if (payload.length == 0) {
            return message.createErrorResponse(ReturnCode.MALFORMED_MESSAGE);
        }
        
        try {
            String statusJson = new String(payload, java.nio.charset.StandardCharsets.UTF_8);
            HvacStatus newStatus = gson.fromJson(statusJson, HvacStatus.class);
            
            currentStatus = newStatus;
            Log.d(TAG, "AC status changed: " + statusJson);
            
            // 发送空调状态变化事件
            sendAcStatusChangedEvent(newStatus);
            
            return message.createResponse(null);
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to parse AC status", e);
            return message.createErrorResponse(ReturnCode.MALFORMED_MESSAGE);
        }
    }
    
    // ================= 事件发送方法 =================
    
    /**
     * 发送温度变化事件
     */
    private void sendTemperatureChangedEvent(float temperature) {
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.order(ByteOrder.BIG_ENDIAN);
        buffer.putFloat(temperature);
        
        sendEvent(CarServices.Events.TEMPERATURE_CHANGED, buffer.array());
    }
    
    /**
     * 发送风速变化事件
     */
    private void sendFanSpeedChangedEvent(int fanSpeed) {
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.order(ByteOrder.BIG_ENDIAN);
        buffer.putInt(fanSpeed);
        
        sendEvent(CarServices.Events.FAN_SPEED_CHANGED, buffer.array());
    }
    
    /**
     * 发送空调状态变化事件
     */
    private void sendAcStatusChangedEvent(@NonNull HvacStatus status) {
        String statusJson = gson.toJson(status);
        byte[] statusBytes = statusJson.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        
        sendEvent(CarServices.Events.AC_STATUS_CHANGED, statusBytes);
    }
    
    /**
     * 发送事件通知
     */
    private void sendEvent(short eventId, @NonNull byte[] payload) {
        ConcurrentHashMap<String, String> subscribers = eventSubscribers.get(eventId);
        if (subscribers == null || subscribers.isEmpty()) {
            Log.d(TAG, "No subscribers for event 0x" + Integer.toHexString(eventId & 0xFFFF));
            return;
        }
        
        for (String subscriberKey : subscribers.keySet()) {
            String[] parts = subscriberKey.split("_");
            if (parts.length == 2) {
                try {
                    short clientId = Short.parseShort(parts[0]);
                    short sessionId = Short.parseShort(parts[1]);
                    
                    SomeIpMessage eventMessage = new SomeIpMessage(
                            SERVICE_ID, eventId, clientId, sessionId,
                            MessageType.NOTIFICATION, ReturnCode.OK, payload
                    );
                    
                    sendResponse(eventMessage);
                    Log.d(TAG, "Event notification sent for event 0x" + 
                          Integer.toHexString(eventId & 0xFFFF));
                    
                } catch (NumberFormatException e) {
                    Log.w(TAG, "Invalid subscriber key: " + subscriberKey, e);
                }
            }
        }
    }
    
    // ================= 辅助方法 =================
    
    /**
     * 发送响应消息
     */
    private void sendResponse(@NonNull SomeIpMessage response) {
        try {
            transport.sendMessage(response, transport.getLocalAddress(), transport.getLocalPort());
        } catch (Exception e) {
            Log.e(TAG, "Failed to send response", e);
        }
    }
    
    /**
     * 发送错误响应
     */
    private void sendErrorResponse(@NonNull SomeIpMessage originalMessage, @NonNull ReturnCode returnCode) {
        SomeIpMessage errorResponse = originalMessage.createErrorResponse(returnCode);
        sendResponse(errorResponse);
    }
    
    /**
     * 发送服务可用通知
     */
    private void sendServiceAvailableNotification() {
        try {
            SomeIpMessage notification = new SomeIpMessage(
                    SERVICE_ID, (short) 0xFFFF, (short) 0, 
                    (short) sessionIdGenerator.incrementAndGet(),
                    MessageType.NOTIFICATION, ReturnCode.OK
            );
            
            transport.sendMulticastMessage(notification);
            Log.d(TAG, "Service available notification sent");
        } catch (Exception e) {
            Log.w(TAG, "Failed to send service available notification (multicast not supported): " + e.getMessage());
        }
    }
    
    /**
     * 发送服务不可用通知
     */
    private void sendServiceUnavailableNotification() {
        try {
            SomeIpMessage notification = new SomeIpMessage(
                    SERVICE_ID, (short) 0xFFFE, (short) 0, 
                    (short) sessionIdGenerator.incrementAndGet(),
                    MessageType.NOTIFICATION, ReturnCode.OK
            );
            
            transport.sendMulticastMessage(notification);
            Log.d(TAG, "Service unavailable notification sent");
        } catch (Exception e) {
            Log.w(TAG, "Failed to send service unavailable notification (multicast not supported): " + e.getMessage());
        }
    }
    
    /**
     * 启动状态监控（模拟传感器数据变化）
     */
    private void startStatusMonitoring() {
        backgroundHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isRunning.get()) {
                    return;
                }
                
                // 模拟温度传感器数据变化
                if (currentStatus.isAcOn() && Math.random() < 0.3) {
                    double tempChange = (Math.random() - 0.5) * 2.0; // ±1度变化
                    float newTemp = (float) Math.max(16.0, Math.min(30.0, 
                            currentStatus.getTemperature() + tempChange));
                    
                    if (Math.abs(newTemp - currentStatus.getTemperature()) > 0.1) {
                        currentStatus = currentStatus.withTemperature(newTemp);
                        sendTemperatureChangedEvent(newTemp);
                        Log.d(TAG, "Temperature sensor updated: " + newTemp);
                    }
                }
                
                // 30秒后再次检查
                backgroundHandler.postDelayed(this, 30000);
            }
        }, 30000);
    }
    
    /**
     * 清理资源
     */
    private void cleanup() {
        methodHandlers.clear();
        eventSubscribers.clear();
        
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
    
    /**
     * 检查服务是否正在运行
     * @return true表示正在运行
     */
    public boolean isRunning() {
        return isRunning.get();
    }
} 