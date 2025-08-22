# CMakeLists.txt - CMake构建脚本（支持OpenCV集成）
# CMake是跨平台的构建工具，用于编译C/C++代码

# 设置CMake最低版本要求
cmake_minimum_required(VERSION 3.22.1)

# 声明项目名称
project("opencv-integration")

# =================================================================
# OpenCV集成配置 - 可选功能
# =================================================================

# 检查OpenCV是否可用
option(ENABLE_OPENCV "Enable OpenCV integration" OFF)

# 如果用户明确启用OpenCV，或者存在OpenCV目录，则尝试查找
if(ENABLE_OPENCV OR EXISTS "${CMAKE_SOURCE_DIR}/../../../libs/opencv")
    # 设置OpenCV路径
    set(OpenCV_DIR ${CMAKE_SOURCE_DIR}/../../../libs/opencv/sdk/native/jni)
    
    # 尝试查找OpenCV包（不是必需的）
    find_package(OpenCV QUIET)
    
    if(OpenCV_FOUND)
        message(STATUS "OpenCV found and enabled!")
        message(STATUS "    version: ${OpenCV_VERSION}")
        message(STATUS "    libraries: ${OpenCV_LIBS}")
        message(STATUS "    include path: ${OpenCV_INCLUDE_DIRS}")
        set(OPENCV_AVAILABLE TRUE)
    else()
        message(WARNING "OpenCV requested but not found. OpenCV features will be disabled.")
        message(STATUS "To enable OpenCV:")
        message(STATUS "  1. Download OpenCV Android SDK from https://opencv.org/releases/")
        message(STATUS "  2. Extract to: ${CMAKE_SOURCE_DIR}/../../../libs/opencv/")
        message(STATUS "  3. Set ENABLE_OPENCV=ON or ensure the directory exists")
        set(OPENCV_AVAILABLE FALSE)
    endif()
else()
    message(STATUS "OpenCV integration disabled. To enable:")
    message(STATUS "  - Set ENABLE_OPENCV=ON, or")
    message(STATUS "  - Install OpenCV Android SDK to: ${CMAKE_SOURCE_DIR}/../../../libs/opencv/")
    set(OPENCV_AVAILABLE FALSE)
endif()

# 添加原有的JNI库
add_library(
        # 库名称 - 对应Java中System.loadLibrary("native-lib")
        native-lib

        # 库类型 - SHARED表示动态库，STATIC表示静态库
        SHARED

        # 源文件列表 - 原有的JNI示例
        native-lib.cpp
)

# 查找Android NDK提供的库
find_library(log-lib log)              # Android日志库

# 添加16KB页面大小对齐的链接器选项
# 这是为了支持Google Play从2025年11月1日开始要求的16KB页面大小
set(CMAKE_SHARED_LINKER_FLAGS "${CMAKE_SHARED_LINKER_FLAGS} -Wl,-z,max-page-size=0x4000")
set(CMAKE_SHARED_LINKER_FLAGS "${CMAKE_SHARED_LINKER_FLAGS} -Wl,-z,common-page-size=0x4000")

# 为native-lib库设置16KB页面对齐（使用target-specific属性）
set_target_properties(native-lib PROPERTIES
    LINK_FLAGS "-Wl,-z,max-page-size=0x4000 -Wl,-z,common-page-size=0x4000"
)

# 链接原有的JNI库
target_link_libraries(
        native-lib
        ${log-lib}
)

# =================================================================
# OpenCV相关库配置 - 仅在OpenCV可用时编译
# =================================================================

if(OPENCV_AVAILABLE)
    # 查找额外的Android库
    find_library(jnigraphics-lib jnigraphics)  # Bitmap处理库
    find_library(android-lib android)          # Android系统库
    
    # 添加OpenCV JNI库
    add_library(
            # OpenCV JNI库名称
            opencv-jni
            # 库类型
            SHARED
            # OpenCV相关的源文件
            opencv-jni.cpp
    )
    
    # 包含OpenCV头文件目录
    target_include_directories(opencv-jni PRIVATE ${OpenCV_INCLUDE_DIRS})
    
    # 为OpenCV JNI库设置16KB页面对齐（使用target-specific属性）
    set_target_properties(opencv-jni PROPERTIES
        LINK_FLAGS "-Wl,-z,max-page-size=0x4000 -Wl,-z,common-page-size=0x4000"
    )
    
    # 链接OpenCV JNI库
    target_link_libraries(
            opencv-jni
            ${OpenCV_LIBS}          # OpenCV库
            ${log-lib}              # Android日志库
            ${jnigraphics-lib}      # 用于Bitmap和Mat转换
            ${android-lib}          # Android系统库
    )
    
    message(STATUS "OpenCV JNI library configured successfully")
else()
    message(STATUS "OpenCV JNI library skipped (OpenCV not available)")
endif()

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
