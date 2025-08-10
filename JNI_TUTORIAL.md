# JNI (Java Native Interface) 完整教程

## 📝 什么是JNI？

JNI（Java Native Interface）是Java平台的标准机制，允许Java代码与其他语言（如C/C++）编写的本地代码进行互操作。在Android开发中，JNI常用于：

- **性能优化**：C/C++在数学计算、图像处理等方面性能更好
- **调用现有C/C++库**：复用已有的本地库
- **访问底层系统功能**：直接调用系统API
- **安全性**：关键算法可以用C/C++实现，增加逆向难度

## 🏗️ 项目结构

我们创建的JNI示例项目包含以下文件：

```
app/
├── src/main/
│   ├── java/com/gdet/testapp/jni/
│   │   ├── JniHelper.java           # Java类，声明native方法
│   │   └── JniTestActivity.java     # 测试Activity
│   ├── cpp/
│   │   ├── native-lib.cpp          # C++实现文件
│   │   └── CMakeLists.txt          # CMake构建脚本
│   └── res/layout/
│       └── activity_jni_test.xml   # UI布局文件
└── build.gradle                    # 配置NDK支持
```

## 🔧 实现步骤详解

### 第1步：创建Java类并声明native方法

```java
public class JniHelper {
    // 加载本地库
    static {
        System.loadLibrary("native-lib");
    }
    
    // 声明native方法
    public native String getGreetingFromNative();
    public native int addTwoNumbers(int a, int b);
    public native String processString(String name);
    public native String getSystemInfo();
}
```

**关键要点：**
- `static` 块用于加载本地库
- `native` 关键字声明方法在本地代码中实现
- 方法只需要声明，不需要在Java中实现

### 第2步：实现C++代码

```cpp
#include <jni.h>
#include <string>

// JNI函数命名规则：Java_包名_类名_方法名
extern "C" {
    JNIEXPORT jstring JNICALL
    Java_com_gdet_testapp_jni_JniHelper_getGreetingFromNative(
        JNIEnv *env, jobject thiz) {
        
        std::string hello = "Hello from C++!";
        return env->NewStringUTF(hello.c_str());
    }
}
```

**命名规则详解：**
- 包名中的`.`替换为`_`
- 如果类名或方法名包含下划线，需要特殊处理
- 必须使用 `extern "C"` 防止C++名称修饰

### 第3步：配置CMakeLists.txt

```cmake
cmake_minimum_required(VERSION 3.22.1)
project("native-lib")

# 创建共享库
add_library(native-lib SHARED native-lib.cpp)

# 链接Android日志库
find_library(log-lib log)
target_link_libraries(native-lib ${log-lib})
```

### 第4步：配置build.gradle

```gradle
android {
    defaultConfig {
        // NDK配置
        ndk {
            abiFilters 'arm64-v8a', 'armeabi-v7a', 'x86', 'x86_64'
        }
        
        externalNativeBuild {
            cmake {
                cppFlags "-std=c++17"
            }
        }
    }
    
    // 指定CMake路径
    externalNativeBuild {
        cmake {
            path "src/main/cpp/CMakeLists.txt"
            version "3.22.1"
        }
    }
}
```

## 📚 JNI数据类型对照表

| Java类型 | JNI类型 | C/C++类型 | 说明 |
|---------|---------|-----------|------|
| boolean | jboolean | unsigned char | 布尔值 |
| byte | jbyte | signed char | 字节 |
| char | jchar | unsigned short | 字符 |
| short | jshort | short | 短整型 |
| int | jint | int | 整型 |
| long | jlong | long long | 长整型 |
| float | jfloat | float | 浮点数 |
| double | jdouble | double | 双精度浮点数 |
| String | jstring | const char* | 字符串（需转换） |
| Object | jobject | jobject | 对象引用 |

## 🔄 字符串处理详解

### Java字符串 → C字符串
```cpp
// 获取Java字符串的C字符串表示
const char *nativeString = env->GetStringUTFChars(javaString, 0);

// 使用完后必须释放
env->ReleaseStringUTFChars(javaString, nativeString);
```

### C字符串 → Java字符串
```cpp
// 创建Java字符串
std::string cppString = "Hello";
jstring javaString = env->NewStringUTF(cppString.c_str());
```

## 📋 JNI函数签名说明

每个JNI函数都有固定的参数：

1. **JNIEnv *env**：JNI环境指针，提供JNI函数接口
2. **jobject thiz**：Java对象的引用（相当于this指针）
3. **其他参数**：根据Java方法声明的参数

## ⚠️ 重要注意事项

### 1. 内存管理
```cpp
// ❌ 错误：忘记释放字符串
const char *str = env->GetStringUTFChars(javaStr, 0);
// 使用str...
// 忘记调用 ReleaseStringUTFChars

// ✅ 正确：及时释放
const char *str = env->GetStringUTFChars(javaStr, 0);
// 使用str...
env->ReleaseStringUTFChars(javaStr, str);
```

### 2. 异常处理
```cpp
// 检查JNI调用是否产生异常
if (env->ExceptionCheck()) {
    env->ExceptionClear();
    // 处理异常
}
```

### 3. 线程安全
- JNIEnv指针只在当前线程有效
- 不能跨线程使用JNIEnv
- 对象引用默认是局部引用，需要时要创建全局引用

## 🎯 编译和调试

### 编译
1. 确保安装了NDK
2. 在Android Studio中构建项目
3. 检查 `app/build/intermediates/cmake/` 目录下的构建输出

### 调试
```cpp
// 使用Android Log输出调试信息
#include <android/log.h>
#define TAG "NativeLib"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)

LOGI("Debug message: %s", someString);
```

### 查看日志
```bash
adb logcat | grep NativeLib
```

## 🚀 运行示例

1. **构建项目**：在Android Studio中构建项目
2. **安装应用**：在设备或模拟器上安装
3. **打开应用**：启动应用，点击"JNI测试示例"
4. **测试功能**：
   - 点击"获取问候语"测试字符串返回
   - 输入数字点击"在C++中计算"测试参数传递
   - 输入名字测试字符串处理
   - 点击"获取系统信息"查看系统信息

## 🔍 常见问题解决

### 1. UnsatisfiedLinkError
**原因**：找不到本地库
**解决**：
- 检查库名是否正确（System.loadLibrary("native-lib")）
- 确认CMakeLists.txt中的库名匹配
- 验证编译是否成功

### 2. 函数找不到
**原因**：JNI函数命名不正确
**解决**：
- 检查包名、类名、方法名是否正确
- 使用 `javah` 工具生成正确的函数签名（可选）

### 3. 编译错误
**原因**：NDK配置问题
**解决**：
- 确认安装了NDK
- 检查CMakeLists.txt语法
- 验证build.gradle配置

## 📈 进阶主题

### 1. 回调Java方法
```cpp
// 从C++调用Java方法
jclass clazz = env->GetObjectClass(thiz);
jmethodID methodID = env->GetMethodID(clazz, "methodName", "()V");
env->CallVoidMethod(thiz, methodID);
```

### 2. 处理数组
```cpp
// 处理Java数组
jintArray javaArray = /* 从参数获取 */;
jint *nativeArray = env->GetIntArrayElements(javaArray, 0);
// 处理数组...
env->ReleaseIntArrayElements(javaArray, nativeArray, 0);
```

### 3. 多线程考虑
```cpp
// 在新线程中调用Java方法需要先attach
JavaVM *jvm;
JNIEnv *env;
jvm->AttachCurrentThread(&env, NULL);
// 使用env...
jvm->DetachCurrentThread();
```

## 🎯 最佳实践

1. **最小化JNI调用**：频繁的JNI调用会影响性能
2. **批量处理数据**：一次传递大块数据而不是多次小数据
3. **缓存Java类和方法ID**：避免重复查找
4. **正确的内存管理**：及时释放本地引用
5. **异常安全**：检查和处理JNI异常
6. **使用合适的数据类型**：选择最适合的JNI数据类型

## 📚 参考资源

- [Oracle JNI文档](https://docs.oracle.com/javase/8/docs/technotes/guides/jni/)
- [Android NDK指南](https://developer.android.com/ndk/guides)
- [CMake官方文档](https://cmake.org/documentation/)

---

## 🎉 恭喜！

你现在已经掌握了JNI的基础知识和实际应用。这个示例提供了一个完整的JNI项目模板，你可以在此基础上开发更复杂的本地功能。

记住：JNI是一个强大的工具，但也要谨慎使用。只在确实需要本地代码的情况下使用JNI，并始终注意内存管理和线程安全。
