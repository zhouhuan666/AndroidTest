# CMakeLists.txt - CMake构建脚本
# CMake是跨平台的构建工具，用于编译C/C++代码

# 设置CMake最低版本要求
cmake_minimum_required(VERSION 3.22.1)

# 声明项目名称
project("native-lib")

# 添加库定义
# add_library() 用于创建一个库
# SHARED 表示创建动态链接库（.so文件）
# native-lib 是库的名称，最终生成 libnative-lib.so
# native-lib.cpp 是源文件
add_library(
        # 库名称 - 对应Java中System.loadLibrary("native-lib")
        native-lib

        # 库类型 - SHARED表示动态库，STATIC表示静态库
        SHARED

        # 源文件列表 - 可以添加多个.cpp文件
        native-lib.cpp
)

# 查找并链接Android NDK提供的库
# find_library() 用于查找系统预编译的库
find_library(
        # 变量名，用于存储库的路径
        log-lib

        # 要查找的库名称（不包含lib前缀和.so后缀）
        # log库提供Android日志功能(__android_log_print等函数)
        log
)

# 链接库
# target_link_libraries() 将找到的库链接到我们的库
target_link_libraries(
        # 目标库（我们创建的库）
        native-lib

        # 要链接的库列表
        # ${log-lib} 引用上面find_library找到的log库
        ${log-lib}
)

# 其他常用的Android NDK库：
# android - 提供Android特定的API
# EGL - OpenGL ES渲染上下文
# GLESv2 - OpenGL ES 2.0
# jnigraphics - 处理Bitmap的JNI API
# OpenSLES - 音频处理
# z - zlib压缩库

# 示例：如果需要链接更多库
# find_library(android-lib android)
# target_link_libraries(native-lib ${log-lib} ${android-lib})

# 编译选项（可选）
# 设置C++标准
set(CMAKE_CXX_STANDARD 17)

# 添加编译标志（可选）
# target_compile_options(native-lib PRIVATE -Wall -Wextra)

# 包含头文件目录（如果有自定义头文件）
# target_include_directories(native-lib PRIVATE include/)
