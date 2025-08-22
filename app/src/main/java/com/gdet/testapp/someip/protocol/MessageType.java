package com.gdet.testapp.someip.protocol;

/**
 * SOME/IP消息类型枚举
 * 遵循AUTOSAR SOME/IP协议规范
 * 
 * @author Android Team
 * @version 1.0
 */
public enum MessageType {
    REQUEST((byte) 0x00),                    // 请求消息
    REQUEST_NO_RETURN((byte) 0x01),         // 无返回请求消息
    NOTIFICATION((byte) 0x02),              // 通知消息
    REQUEST_ACK((byte) 0x40),               // 请求确认消息
    REQUEST_NACK((byte) 0x41),              // 请求否定确认消息
    NOTIFICATION_ACK((byte) 0x42),          // 通知确认消息
    NOTIFICATION_NACK((byte) 0x43),         // 通知否定确认消息
    RESPONSE((byte) 0x80),                  // 响应消息
    ERROR((byte) 0x81),                     // 错误响应消息
    RESPONSE_ACK((byte) 0xC0),              // 响应确认消息
    RESPONSE_NACK((byte) 0xC1),             // 响应否定确认消息
    SUBSCRIBE((byte) 0x06),                 // 订阅消息
    SUBSCRIBE_ACK((byte) 0x07),             // 订阅确认消息
    SUBSCRIBE_NACK((byte) 0x08),            // 订阅否定确认消息
    UNSUBSCRIBE((byte) 0x09),               // 取消订阅消息
    UNSUBSCRIBE_ACK((byte) 0x0A),           // 取消订阅确认消息
    UNSUBSCRIBE_NACK((byte) 0x0B);          // 取消订阅否定确认消息
    
    private final byte value;
    
    /**
     * 构造函数
     * @param value 消息类型值
     */
    MessageType(byte value) {
        this.value = value;
    }
    
    /**
     * 获取消息类型值
     * @return 字节值
     */
    public byte getValue() {
        return value;
    }
    
    /**
     * 根据字节值获取消息类型
     * @param value 字节值
     * @return 消息类型，如果找不到则返回REQUEST
     */
    public static MessageType fromValue(byte value) {
        for (MessageType type : values()) {
            if (type.value == value) {
                return type;
            }
        }
        return REQUEST;
    }
} 