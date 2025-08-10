#include <jni.h>
#include <string>
#include <android/log.h>
#include <sys/utsname.h>

// 定义LOG标签，用于Android日志输出
#define TAG "NativeLib"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)

/**
 * JNI函数命名规则：
 * Java_[包名(用下划线替换点)]_[类名]_[方法名]
 * 
 * 例如：com.gdet.testapp.jni.JniHelper.getGreetingFromNative()
 * 对应的JNI函数名：Java_com_gdet_testapp_jni_JniHelper_getGreetingFromNative
 */

extern "C" {

/**
 * 实现 getGreetingFromNative() 方法
 * 
 * @param env JNI环境指针，提供JNI函数接口
 * @param thiz Java对象的引用（this指针）
 * @return jstring Java字符串对象
 */
JNIEXPORT jstring JNICALL
Java_com_gdet_testapp_jni_JniHelper_getGreetingFromNative(JNIEnv *env, jobject thiz) {
    // 在C++中创建字符串
    std::string hello = "Hello from C++! JNI is working perfectly!";
    
    // 记录日志到Android Logcat
    LOGI("getGreetingFromNative called from Java");
    
    // 将C++字符串转换为Java字符串（jstring）
    // NewStringUTF创建一个UTF-8编码的Java字符串
    return env->NewStringUTF(hello.c_str());
}

/**
 * 实现 addTwoNumbers() 方法
 * 演示基本数据类型的传递
 * 
 * @param env JNI环境指针
 * @param thiz Java对象引用
 * @param a 第一个整数参数
 * @param b 第二个整数参数
 * @return jint 计算结果
 */
JNIEXPORT jint JNICALL
Java_com_gdet_testapp_jni_JniHelper_addTwoNumbers(JNIEnv *env, jobject thiz, jint a, jint b) {
    // 记录传入的参数
    LOGI("addTwoNumbers called with a=%d, b=%d", a, b);
    
    // 执行加法运算
    jint result = a + b;
    
    // 记录结果
    LOGI("addTwoNumbers result: %d", result);
    
    return result;
}

/**
 * 实现 processString() 方法
 * 演示字符串在Java和C++之间的转换
 * 
 * @param env JNI环境指针
 * @param thiz Java对象引用
 * @param name Java字符串参数
 * @return jstring 处理后的字符串
 */
JNIEXPORT jstring JNICALL
Java_com_gdet_testapp_jni_JniHelper_processString(JNIEnv *env, jobject thiz, jstring name) {
    // 将Java字符串转换为C字符串
    const char *nativeString = env->GetStringUTFChars(name, 0);
    
    // 记录接收到的字符串
    LOGI("processString called with: %s", nativeString);
    
    // 在C++中处理字符串
    std::string processedString = "Processed in C++: ";
    processedString += nativeString;
    processedString += " [Length: " + std::to_string(strlen(nativeString)) + "]";
    
    // 释放Java字符串资源（重要！防止内存泄漏）
    env->ReleaseStringUTFChars(name, nativeString);
    
    // 返回处理后的字符串
    return env->NewStringUTF(processedString.c_str());
}

/**
 * 实现 getSystemInfo() 方法
 * 演示C++访问系统信息
 * 
 * @param env JNI环境指针
 * @param thiz Java对象引用
 * @return jstring 系统信息字符串
 */
JNIEXPORT jstring JNICALL
Java_com_gdet_testapp_jni_JniHelper_getSystemInfo(JNIEnv *env, jobject thiz) {
    // 获取系统信息
    struct utsname systemInfo;
    uname(&systemInfo);
    
    // 构建系统信息字符串
    std::string info = "System Information from C++:\n";
    info += "System Name: " + std::string(systemInfo.sysname) + "\n";
    info += "Node Name: " + std::string(systemInfo.nodename) + "\n";
    info += "Release: " + std::string(systemInfo.release) + "\n";
    info += "Version: " + std::string(systemInfo.version) + "\n";
    info += "Machine: " + std::string(systemInfo.machine);
    
    LOGI("getSystemInfo called");
    
    return env->NewStringUTF(info.c_str());
}

} // extern "C"

/*
 * JNI数据类型对照表：
 * 
 * Java类型     JNI类型      C/C++类型
 * boolean      jboolean     unsigned char
 * byte         jbyte        signed char
 * char         jchar        unsigned short
 * short        jshort       short
 * int          jint         int
 * long         jlong        long long
 * float        jfloat       float
 * double       jdouble      double
 * String       jstring      const char*（需要转换）
 * Object       jobject      jobject
 * 
 * 重要提示：
 * 1. 所有JNI函数必须以JNIEXPORT开头，以JNICALL结尾
 * 2. 函数名必须严格按照命名规则：Java_包名_类名_方法名
 * 3. 处理字符串时要注意内存管理，使用GetStringUTFChars后必须调用ReleaseStringUTFChars
 * 4. 使用extern "C"确保C++编译器不会修改函数名（名称修饰）
 */
