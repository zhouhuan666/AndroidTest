package com.gdet.testapp.someip.protocol;

/**
 * SOME/IP返回码枚举
 * 遵循AUTOSAR SOME/IP协议规范
 * 
 * @author Android Team
 * @version 1.0
 */
public enum ReturnCode {
    OK((byte) 0x00),                         // 成功
    NOT_OK((byte) 0x01),                     // 失败
    UNKNOWN_SERVICE((byte) 0x02),            // 未知服务
    UNKNOWN_METHOD((byte) 0x03),             // 未知方法
    NOT_READY((byte) 0x04),                  // 服务未就绪
    NOT_REACHABLE((byte) 0x05),              // 服务不可达
    TIMEOUT((byte) 0x06),                    // 超时
    WRONG_PROTOCOL_VERSION((byte) 0x07),     // 协议版本错误
    WRONG_INTERFACE_VERSION((byte) 0x08),    // 接口版本错误
    MALFORMED_MESSAGE((byte) 0x09),          // 消息格式错误
    WRONG_MESSAGE_TYPE((byte) 0x0A),         // 消息类型错误
    E_TOO_MANY_MESSAGES((byte) 0x0B),       // 消息过多
    E_INVALID_SESSION((byte) 0x0C),          // 无效会话
    E_INVALID_SESSION_HANDLE((byte) 0x0D),   // 无效会话句柄
    E_INVALID_SESSION_ID((byte) 0x0E),       // 无效会话ID
    E_INVALID_CLIENT_ID((byte) 0x0F),        // 无效客户端ID
    E_INVALID_SERVICE_ID((byte) 0x10),       // 无效服务ID
    E_INVALID_METHOD_ID((byte) 0x11),        // 无效方法ID
    E_INVALID_INSTANCE_ID((byte) 0x12),      // 无效实例ID
    E_INVALID_EVENT_ID((byte) 0x13),         // 无效事件ID
    E_INVALID_EVENTGROUP_ID((byte) 0x14),    // 无效事件组ID
    E_INVALID_MAJOR_VERSION((byte) 0x15),    // 无效主版本
    E_INVALID_MINOR_VERSION((byte) 0x16),    // 无效次版本
    E_INVALID_TTL((byte) 0x17),              // 无效TTL
    E_INVALID_PROTOCOL_VERSION((byte) 0x18), // 无效协议版本
    E_INVALID_INTERFACE_VERSION((byte) 0x19), // 无效接口版本
    E_INVALID_MESSAGE_TYPE((byte) 0x1A),     // 无效消息类型
    E_INVALID_RETURN_CODE((byte) 0x1B),      // 无效返回码
    E_INVALID_ADDRESS((byte) 0x1C),          // 无效地址
    E_INVALID_PORT((byte) 0x1D),             // 无效端口
    E_INVALID_TRANSPORT((byte) 0x1E),        // 无效传输协议
    E_INVALID_PROTOCOL((byte) 0x1F),         // 无效协议
    E_INVALID_HEADER((byte) 0x20),           // 无效头部
    E_INVALID_PAYLOAD((byte) 0x21),          // 无效负载
    E_INVALID_CRC((byte) 0x22),              // 无效CRC
    E_INVALID_LENGTH((byte) 0x23),           // 无效长度
    E_INVALID_OFFSET((byte) 0x24),           // 无效偏移
    E_INVALID_SIZE((byte) 0x25),             // 无效大小
    E_INVALID_FORMAT((byte) 0x26),           // 无效格式
    E_INVALID_VALUE((byte) 0x27),            // 无效值
    E_INVALID_RANGE((byte) 0x28),            // 无效范围
    E_INVALID_STATE((byte) 0x29),            // 无效状态
    E_INVALID_TRANSITION((byte) 0x2A);       // 无效转换
    
    private final byte value;
    
    /**
     * 构造函数
     * @param value 返回码值
     */
    ReturnCode(byte value) {
        this.value = value;
    }
    
    /**
     * 获取返回码值
     * @return 字节值
     */
    public byte getValue() {
        return value;
    }
    
    /**
     * 根据字节值获取返回码
     * @param value 字节值
     * @return 返回码，如果找不到则返回NOT_OK
     */
    public static ReturnCode fromValue(byte value) {
        for (ReturnCode code : values()) {
            if (code.value == value) {
                return code;
            }
        }
        return NOT_OK;
    }
    
    /**
     * 检查是否为成功状态
     * @return true表示成功，false表示失败
     */
    public boolean isSuccess() {
        return this == OK;
    }
} 