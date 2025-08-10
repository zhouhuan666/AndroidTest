package com.gdet.testapp.jni;

/**
 * JNI助手类 - 最简单的JNI示例
 * 
 * JNI (Java Native Interface) 是Java平台的标准机制，用于Java和其他语言（如C/C++）的互操作
 * 
 * @author Android Developer
 */
public class JniHelper {
    
    // 静态块 - 在类加载时执行，用于加载本地库
    static {
        // 加载名为 "native-lib" 的本地库
        // 实际的库文件名是 libnative-lib.so (在Android上)
        System.loadLibrary("native-lib");
    }
    
    /**
     * 声明native方法 - 从C++获取问候字符串
     * 
     * native关键字表示这个方法的实现在本地代码（C/C++）中
     * 不需要在Java中提供实现，只需要声明
     * 
     * @return C++返回的问候字符串
     */
    public native String getGreetingFromNative();
    
    /**
     * 声明native方法 - 在C++中进行两个数字的加法运算
     * 
     * 演示Java向C++传递参数，并获取返回值
     * 
     * @param a 第一个整数
     * @param b 第二个整数
     * @return a + b 的结果
     */
    public native int addTwoNumbers(int a, int b);
    
    /**
     * 声明native方法 - 在C++中处理字符串
     * 
     * 演示字符串在Java和C++之间的传递
     * 
     * @param name 输入的名字
     * @return 处理后的字符串
     */
    public native String processString(String name);
    
    /**
     * 声明native方法 - 获取系统信息
     * 
     * 演示C++可以访问系统底层信息
     * 
     * @return 系统信息字符串
     */
    public native String getSystemInfo();
}
