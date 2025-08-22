package com.gdet.testapp.someip.protocol;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

/**
 * SOME/IP消息的基础数据结构
 * 遵循AUTOSAR SOME/IP协议规范
 * 
 * @author Android Team
 * @version 1.0
 */
public class SomeIpMessage {
    
    // 协议常量
    public static final int HEADER_SIZE = 16;
    public static final byte PROTOCOL_VERSION = 0x01;
    public static final byte INTERFACE_VERSION = 0x01;
    
    // 消息字段
    private final short serviceId;           // 服务ID
    private final short methodId;            // 方法ID
    private final short clientId;            // 客户端ID
    private final short sessionId;           // 会话ID
    private final byte protocolVersion;      // 协议版本
    private final byte interfaceVersion;     // 接口版本
    private final MessageType messageType;  // 消息类型
    private final ReturnCode returnCode;     // 返回码
    private final byte[] payload;            // 消息负载
    
    /**
     * 构造函数
     * @param serviceId 服务ID
     * @param methodId 方法ID
     * @param clientId 客户端ID
     * @param sessionId 会话ID
     * @param protocolVersion 协议版本
     * @param interfaceVersion 接口版本
     * @param messageType 消息类型
     * @param returnCode 返回码
     * @param payload 消息负载
     */
    public SomeIpMessage(short serviceId, short methodId, short clientId, short sessionId,
                        byte protocolVersion, byte interfaceVersion, @NonNull MessageType messageType,
                        @NonNull ReturnCode returnCode, @Nullable byte[] payload) {
        this.serviceId = serviceId;
        this.methodId = methodId;
        this.clientId = clientId;
        this.sessionId = sessionId;
        this.protocolVersion = protocolVersion;
        this.interfaceVersion = interfaceVersion;
        this.messageType = messageType;
        this.returnCode = returnCode;
        this.payload = payload != null ? payload.clone() : new byte[0];
    }
    
    /**
     * 构造函数（使用默认协议和接口版本）
     * @param serviceId 服务ID
     * @param methodId 方法ID
     * @param clientId 客户端ID
     * @param sessionId 会话ID
     * @param messageType 消息类型
     * @param returnCode 返回码
     * @param payload 消息负载
     */
    public SomeIpMessage(short serviceId, short methodId, short clientId, short sessionId,
                        @NonNull MessageType messageType, @NonNull ReturnCode returnCode,
                        @Nullable byte[] payload) {
        this(serviceId, methodId, clientId, sessionId, PROTOCOL_VERSION, INTERFACE_VERSION,
             messageType, returnCode, payload);
    }
    
    /**
     * 构造函数（无负载）
     * @param serviceId 服务ID
     * @param methodId 方法ID
     * @param clientId 客户端ID
     * @param sessionId 会话ID
     * @param messageType 消息类型
     * @param returnCode 返回码
     */
    public SomeIpMessage(short serviceId, short methodId, short clientId, short sessionId,
                        @NonNull MessageType messageType, @NonNull ReturnCode returnCode) {
        this(serviceId, methodId, clientId, sessionId, messageType, returnCode, null);
    }
    
    // Getter方法
    public short getServiceId() { return serviceId; }
    public short getMethodId() { return methodId; }
    public short getClientId() { return clientId; }
    public short getSessionId() { return sessionId; }
    public byte getProtocolVersion() { return protocolVersion; }
    public byte getInterfaceVersion() { return interfaceVersion; }
    public MessageType getMessageType() { return messageType; }
    public ReturnCode getReturnCode() { return returnCode; }
    
    @NonNull
    public byte[] getPayload() {
        return payload.clone();
    }
    
    /**
     * 将消息序列化为字节数组
     * @return 序列化后的字节数组
     */
    @NonNull
    public byte[] toByteArray() {
        ByteBuffer buffer = ByteBuffer.allocate(HEADER_SIZE + payload.length);
        buffer.order(ByteOrder.BIG_ENDIAN);
        
        // 写入消息长度 (不包括长度字段本身)
        buffer.putInt(HEADER_SIZE + payload.length - 4);
        
        // 写入服务ID
        buffer.putShort(serviceId);
        
        // 写入方法ID
        buffer.putShort(methodId);
        
        // 写入客户端ID
        buffer.putShort(clientId);
        
        // 写入会话ID
        buffer.putShort(sessionId);
        
        // 写入协议版本和接口版本
        buffer.put(protocolVersion);
        buffer.put(interfaceVersion);
        
        // 写入消息类型
        buffer.put(messageType.getValue());
        
        // 写入返回码
        buffer.put(returnCode.getValue());
        
        // 写入负载
        if (payload.length > 0) {
            buffer.put(payload);
        }
        
        return buffer.array();
    }
    
    /**
     * 从字节数组反序列化消息
     * @param data 字节数组
     * @return SOME/IP消息
     * @throws IllegalArgumentException 如果数据格式不正确
     */
    @NonNull
    public static SomeIpMessage fromByteArray(@NonNull byte[] data) {
        if (data.length < HEADER_SIZE) {
            throw new IllegalArgumentException("Data too short for SOME/IP message header");
        }
        
        ByteBuffer buffer = ByteBuffer.wrap(data);
        buffer.order(ByteOrder.BIG_ENDIAN);
        
        int messageLength = buffer.getInt();
        short serviceId = buffer.getShort();
        short methodId = buffer.getShort();
        short clientId = buffer.getShort();
        short sessionId = buffer.getShort();
        byte protocolVersion = buffer.get();
        byte interfaceVersion = buffer.get();
        MessageType messageType = MessageType.fromValue(buffer.get());
        ReturnCode returnCode = ReturnCode.fromValue(buffer.get());
        
        byte[] payload = new byte[data.length - HEADER_SIZE];
        if (payload.length > 0) {
            buffer.get(payload);
        }
        
        return new SomeIpMessage(serviceId, methodId, clientId, sessionId,
                               protocolVersion, interfaceVersion, messageType, returnCode, payload);
    }
    
    /**
     * 创建消息的副本，但使用新的消息类型和返回码
     * @param newMessageType 新的消息类型
     * @param newReturnCode 新的返回码
     * @return 新的消息实例
     */
    @NonNull
    public SomeIpMessage createResponse(@NonNull MessageType newMessageType, @NonNull ReturnCode newReturnCode) {
        return new SomeIpMessage(serviceId, methodId, clientId, sessionId,
                               protocolVersion, interfaceVersion, newMessageType, newReturnCode, payload);
    }
    
    /**
     * 创建响应消息
     * @param newPayload 响应负载
     * @return 响应消息
     */
    @NonNull
    public SomeIpMessage createResponse(@Nullable byte[] newPayload) {
        return new SomeIpMessage(serviceId, methodId, clientId, sessionId,
                               protocolVersion, interfaceVersion, MessageType.RESPONSE, ReturnCode.OK, newPayload);
    }
    
    /**
     * 创建错误响应消息
     * @param errorCode 错误码
     * @return 错误响应消息
     */
    @NonNull
    public SomeIpMessage createErrorResponse(@NonNull ReturnCode errorCode) {
        return new SomeIpMessage(serviceId, methodId, clientId, sessionId,
                               protocolVersion, interfaceVersion, MessageType.ERROR, errorCode, null);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        SomeIpMessage that = (SomeIpMessage) o;
        
        return serviceId == that.serviceId &&
               methodId == that.methodId &&
               clientId == that.clientId &&
               sessionId == that.sessionId &&
               protocolVersion == that.protocolVersion &&
               interfaceVersion == that.interfaceVersion &&
               messageType == that.messageType &&
               returnCode == that.returnCode &&
               Arrays.equals(payload, that.payload);
    }
    
    @Override
    public int hashCode() {
        int result = serviceId;
        result = 31 * result + methodId;
        result = 31 * result + clientId;
        result = 31 * result + sessionId;
        result = 31 * result + protocolVersion;
        result = 31 * result + interfaceVersion;
        result = 31 * result + messageType.hashCode();
        result = 31 * result + returnCode.hashCode();
        result = 31 * result + Arrays.hashCode(payload);
        return result;
    }
    
    @Override
    @NonNull
    public String toString() {
        return "SomeIpMessage{" +
                "serviceId=0x" + Integer.toHexString(serviceId & 0xFFFF) +
                ", methodId=0x" + Integer.toHexString(methodId & 0xFFFF) +
                ", clientId=0x" + Integer.toHexString(clientId & 0xFFFF) +
                ", sessionId=0x" + Integer.toHexString(sessionId & 0xFFFF) +
                ", protocolVersion=" + protocolVersion +
                ", interfaceVersion=" + interfaceVersion +
                ", messageType=" + messageType +
                ", returnCode=" + returnCode +
                ", payloadLength=" + payload.length +
                '}';
    }
} 