#include <jni.h>
#include <string>
#include <vector>
#include <android/log.h>
#include <android/bitmap.h>

// OpenCV头文件
#include <opencv2/opencv.hpp>
#include <opencv2/imgproc.hpp>
#include <opencv2/features2d.hpp>
#include <opencv2/objdetect.hpp>

#define TAG "OpenCV-JNI"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

using namespace cv;

/**
 * 工具函数：将Android Bitmap转换为OpenCV Mat
 */
bool bitmapToMat(JNIEnv *env, jobject bitmap, Mat &dst) {
    AndroidBitmapInfo info;
    void *pixels = nullptr;
    
    try {
        // 获取bitmap信息
        if (AndroidBitmap_getInfo(env, bitmap, &info) < 0) {
            LOGE("AndroidBitmap_getInfo() failed");
            return false;
        }
        
        // 锁定bitmap像素
        if (AndroidBitmap_lockPixels(env, bitmap, &pixels) < 0) {
            LOGE("AndroidBitmap_lockPixels() failed");
            return false;
        }
        
        // 根据bitmap格式创建Mat
        if (info.format == ANDROID_BITMAP_FORMAT_RGBA_8888) {
            Mat tmp(info.height, info.width, CV_8UC4, pixels);
            tmp.copyTo(dst);
        } else if (info.format == ANDROID_BITMAP_FORMAT_RGB_565) {
            Mat tmp(info.height, info.width, CV_8UC2, pixels);
            cvtColor(tmp, dst, COLOR_BGR5652RGBA);
        } else {
            LOGE("Unsupported bitmap format");
            AndroidBitmap_unlockPixels(env, bitmap);
            return false;
        }
        
        // 解锁bitmap像素
        AndroidBitmap_unlockPixels(env, bitmap);
        return true;
        
    } catch (cv::Exception &e) {
        LOGE("OpenCV exception: %s", e.what());
        AndroidBitmap_unlockPixels(env, bitmap);
        return false;
    }
}

/**
 * 工具函数：将OpenCV Mat转换为Android Bitmap
 */
jobject matToBitmap(JNIEnv *env, Mat &src) {
    try {
        // 确保Mat是RGBA格式
        Mat rgba;
        if (src.channels() == 1) {
            cvtColor(src, rgba, COLOR_GRAY2RGBA);
        } else if (src.channels() == 3) {
            cvtColor(src, rgba, COLOR_BGR2RGBA);
        } else if (src.channels() == 4) {
            src.copyTo(rgba);
        } else {
            LOGE("Unsupported number of channels: %d", src.channels());
            return nullptr;
        }
        
        // 创建Bitmap
        jclass bitmapClass = env->FindClass("android/graphics/Bitmap");
        jclass bitmapConfigClass = env->FindClass("android/graphics/Bitmap$Config");
        
        jfieldID argb8888FieldID = env->GetStaticFieldID(bitmapConfigClass, "ARGB_8888", 
                                                        "Landroid/graphics/Bitmap$Config;");
        jobject argb8888Obj = env->GetStaticObjectField(bitmapConfigClass, argb8888FieldID);
        
        jmethodID createBitmapMethodID = env->GetStaticMethodID(bitmapClass, "createBitmap",
                                                               "(IILandroid/graphics/Bitmap$Config;)Landroid/graphics/Bitmap;");
        
        jobject bitmapObj = env->CallStaticObjectMethod(bitmapClass, createBitmapMethodID,
                                                       rgba.cols, rgba.rows, argb8888Obj);
        
        // 复制像素数据
        AndroidBitmapInfo info;
        void *pixels;
        
        if (AndroidBitmap_getInfo(env, bitmapObj, &info) < 0 ||
            AndroidBitmap_lockPixels(env, bitmapObj, &pixels) < 0) {
            LOGE("Failed to lock bitmap pixels");
            return nullptr;
        }
        
        memcpy(pixels, rgba.data, rgba.total() * rgba.elemSize());
        AndroidBitmap_unlockPixels(env, bitmapObj);
        
        return bitmapObj;
        
    } catch (cv::Exception &e) {
        LOGE("OpenCV exception in matToBitmap: %s", e.what());
        return nullptr;
    }
}

extern "C" {

/**
 * 获取OpenCV版本信息
 */
JNIEXPORT jstring JNICALL
Java_com_gdet_testapp_opencv_OpenCVHelper_getOpenCVVersion(JNIEnv *env, jobject thiz) {
    std::string version = "OpenCV version: " + cv::getVersionString();
    LOGI("OpenCV version: %s", cv::getVersionString().c_str());
    return env->NewStringUTF(version.c_str());
}

/**
 * 转换为灰度图
 */
JNIEXPORT jobject JNICALL
Java_com_gdet_testapp_opencv_OpenCVHelper_convertToGray(JNIEnv *env, jobject thiz, jobject inputBitmap) {
    LOGI("Converting image to grayscale");
    
    Mat src, gray;
    if (!bitmapToMat(env, inputBitmap, src)) {
        LOGE("Failed to convert bitmap to mat");
        return nullptr;
    }
    
    try {
        cvtColor(src, gray, COLOR_RGBA2GRAY);
        return matToBitmap(env, gray);
    } catch (cv::Exception &e) {
        LOGE("OpenCV exception in convertToGray: %s", e.what());
        return nullptr;
    }
}

/**
 * 高斯模糊
 */
JNIEXPORT jobject JNICALL
Java_com_gdet_testapp_opencv_OpenCVHelper_applyGaussianBlur(JNIEnv *env, jobject thiz, 
                                                           jobject inputBitmap, jint kernelSize) {
    LOGI("Applying Gaussian blur with kernel size: %d", kernelSize);
    
    Mat src, blurred;
    if (!bitmapToMat(env, inputBitmap, src)) {
        return nullptr;
    }
    
    try {
        // 确保核大小是奇数
        int kSize = kernelSize;
        if (kSize % 2 == 0) kSize++;
        if (kSize < 3) kSize = 3;
        
        GaussianBlur(src, blurred, Size(kSize, kSize), 0, 0);
        return matToBitmap(env, blurred);
    } catch (cv::Exception &e) {
        LOGE("OpenCV exception in applyGaussianBlur: %s", e.what());
        return nullptr;
    }
}

/**
 * Canny边缘检测
 */
JNIEXPORT jobject JNICALL
Java_com_gdet_testapp_opencv_OpenCVHelper_detectEdges(JNIEnv *env, jobject thiz, 
                                                     jobject inputBitmap, jdouble threshold1, jdouble threshold2) {
    LOGI("Detecting edges with thresholds: %.2f, %.2f", threshold1, threshold2);
    
    Mat src, gray, edges;
    if (!bitmapToMat(env, inputBitmap, src)) {
        return nullptr;
    }
    
    try {
        // 转换为灰度图
        if (src.channels() > 1) {
            cvtColor(src, gray, COLOR_RGBA2GRAY);
        } else {
            gray = src;
        }
        
        // 应用Canny边缘检测
        Canny(gray, edges, threshold1, threshold2);
        
        return matToBitmap(env, edges);
    } catch (cv::Exception &e) {
        LOGE("OpenCV exception in detectEdges: %s", e.what());
        return nullptr;
    }
}

/**
 * 直方图均衡化
 */
JNIEXPORT jobject JNICALL
Java_com_gdet_testapp_opencv_OpenCVHelper_equalizeHistogram(JNIEnv *env, jobject thiz, jobject inputBitmap) {
    LOGI("Equalizing histogram");
    
    Mat src, gray, equalized;
    if (!bitmapToMat(env, inputBitmap, src)) {
        return nullptr;
    }
    
    try {
        // 转换为灰度图
        if (src.channels() > 1) {
            cvtColor(src, gray, COLOR_RGBA2GRAY);
        } else {
            gray = src;
        }
        
        // 直方图均衡化
        equalizeHist(gray, equalized);
        
        return matToBitmap(env, equalized);
    } catch (cv::Exception &e) {
        LOGE("OpenCV exception in equalizeHistogram: %s", e.what());
        return nullptr;
    }
}

/**
 * 颜色分割
 */
JNIEXPORT jobject JNICALL
Java_com_gdet_testapp_opencv_OpenCVHelper_colorSegmentation(JNIEnv *env, jobject thiz, 
                                                           jobject inputBitmap,
                                                           jint hueMin, jint hueMax,
                                                           jint satMin, jint satMax,
                                                           jint valMin, jint valMax) {
    LOGI("Color segmentation: H[%d,%d] S[%d,%d] V[%d,%d]", hueMin, hueMax, satMin, satMax, valMin, valMax);
    
    Mat src, hsv, mask;
    if (!bitmapToMat(env, inputBitmap, src)) {
        return nullptr;
    }
    
    try {
        // 转换到HSV色彩空间
        cvtColor(src, hsv, COLOR_RGBA2HSV);
        
        // 定义颜色范围
        Scalar lower(hueMin, satMin, valMin);
        Scalar upper(hueMax, satMax, valMax);
        
        // 创建掩码
        inRange(hsv, lower, upper, mask);
        
        return matToBitmap(env, mask);
    } catch (cv::Exception &e) {
        LOGE("OpenCV exception in colorSegmentation: %s", e.what());
        return nullptr;
    }
}

/**
 * 透视变换
 */
JNIEXPORT jobject JNICALL
Java_com_gdet_testapp_opencv_OpenCVHelper_perspectiveTransform(JNIEnv *env, jobject thiz,
                                                              jobject inputBitmap,
                                                              jfloatArray srcPoints,
                                                              jfloatArray dstPoints,
                                                              jint outputWidth,
                                                              jint outputHeight) {
    LOGI("Performing perspective transform to %dx%d", outputWidth, outputHeight);
    
    Mat src, dst;
    if (!bitmapToMat(env, inputBitmap, src)) {
        return nullptr;
    }
    
    try {
        // 获取点坐标
        jfloat *srcPts = env->GetFloatArrayElements(srcPoints, nullptr);
        jfloat *dstPts = env->GetFloatArrayElements(dstPoints, nullptr);
        
        // 创建Point2f数组
        std::vector<Point2f> srcCorners(4);
        std::vector<Point2f> dstCorners(4);
        
        for (int i = 0; i < 4; i++) {
            srcCorners[i] = Point2f(srcPts[i * 2], srcPts[i * 2 + 1]);
            dstCorners[i] = Point2f(dstPts[i * 2], dstPts[i * 2 + 1]);
        }
        
        // 计算透视变换矩阵
        Mat transformMatrix = getPerspectiveTransform(srcCorners, dstCorners);
        
        // 应用透视变换
        warpPerspective(src, dst, transformMatrix, Size(outputWidth, outputHeight));
        
        // 释放数组
        env->ReleaseFloatArrayElements(srcPoints, srcPts, 0);
        env->ReleaseFloatArrayElements(dstPoints, dstPts, 0);
        
        return matToBitmap(env, dst);
    } catch (cv::Exception &e) {
        LOGE("OpenCV exception in perspectiveTransform: %s", e.what());
        return nullptr;
    }
}

/**
 * 形态学开运算
 */
JNIEXPORT jobject JNICALL
Java_com_gdet_testapp_opencv_OpenCVHelper_morphologyOpen(JNIEnv *env, jobject thiz,
                                                        jobject inputBitmap, jint kernelSize) {
    LOGI("Performing morphology open operation with kernel size: %d", kernelSize);
    
    Mat src, dst;
    if (!bitmapToMat(env, inputBitmap, src)) {
        return nullptr;
    }
    
    try {
        // 创建形态学核
        Mat kernel = getStructuringElement(MORPH_ELLIPSE, Size(kernelSize, kernelSize));
        
        // 执行开运算（先腐蚀后膨胀）
        morphologyEx(src, dst, MORPH_OPEN, kernel);
        
        return matToBitmap(env, dst);
    } catch (cv::Exception &e) {
        LOGE("OpenCV exception in morphologyOpen: %s", e.what());
        return nullptr;
    }
}

/**
 * 水平拼接图像
 */
JNIEXPORT jobject JNICALL
Java_com_gdet_testapp_opencv_OpenCVHelper_stitchImagesHorizontally(JNIEnv *env, jobject thiz,
                                                                  jobject leftBitmap, jobject rightBitmap) {
    LOGI("Stitching images horizontally");
    
    Mat left, right, result;
    if (!bitmapToMat(env, leftBitmap, left) || !bitmapToMat(env, rightBitmap, right)) {
        return nullptr;
    }
    
    try {
        // 确保两张图像高度相同
        if (left.rows != right.rows) {
            // 调整右图像的高度以匹配左图像
            resize(right, right, Size(right.cols, left.rows));
        }
        
        // 水平拼接
        hconcat(left, right, result);
        
        return matToBitmap(env, result);
    } catch (cv::Exception &e) {
        LOGE("OpenCV exception in stitchImagesHorizontally: %s", e.what());
        return nullptr;
    }
}

} // extern "C"























































