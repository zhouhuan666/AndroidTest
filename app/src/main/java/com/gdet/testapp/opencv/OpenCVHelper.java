package com.gdet.testapp.opencv;

import android.graphics.Bitmap;

/**
 * OpenCV JNI助手类
 * 
 * 演示如何集成OpenCV这个强大的计算机视觉库到Android应用中
 * OpenCV是最受欢迎的开源计算机视觉、机器学习和图像处理库之一
 * 
 * 主要功能包括：
 * - 图像滤波和增强
 * - 特征检测和匹配
 * - 对象识别和跟踪
 * - 机器学习算法
 * - 视频分析
 * 
 * @author Android Developer
 */
public class OpenCVHelper {
    
    // 加载OpenCV本地库
    static {
        try {
            // 首先尝试加载OpenCV库
            System.loadLibrary("opencv_java4"); // OpenCV 4.x版本
            // 然后加载我们自己的JNI库
            System.loadLibrary("opencv-jni");
            isOpenCVLoaded = true;
            System.out.println("OpenCV库加载成功！");
        } catch (UnsatisfiedLinkError e) {
            System.err.println("OpenCV库加载失败: " + e.getMessage());
            System.err.println("OpenCV功能将不可用。如需启用OpenCV功能，请:");
            System.err.println("1. 下载OpenCV Android SDK");
            System.err.println("2. 按照OPENCV_INTEGRATION_GUIDE.md中的说明进行配置");
            isOpenCVLoaded = false;
        }
    }
    
    private static boolean isOpenCVLoaded = false;
    
    /**
     * 检查OpenCV是否成功加载
     */
    public static boolean isOpenCVAvailable() {
        return isOpenCVLoaded;
    }
    
    /**
     * 获取OpenCV版本信息
     */
    public native String getOpenCVVersion();
    
    /**
     * 图像处理功能 - 转换为灰度图
     * 
     * @param inputBitmap 输入的彩色图像
     * @return 处理后的灰度图像
     */
    public native Bitmap convertToGray(Bitmap inputBitmap);
    
    /**
     * 图像处理功能 - 高斯模糊
     * 
     * @param inputBitmap 输入图像
     * @param kernelSize 模糊核大小（必须是奇数）
     * @return 模糊后的图像
     */
    public native Bitmap applyGaussianBlur(Bitmap inputBitmap, int kernelSize);
    
    /**
     * 图像处理功能 - 边缘检测（Canny算法）
     * 
     * @param inputBitmap 输入图像
     * @param threshold1 第一个阈值
     * @param threshold2 第二个阈值
     * @return 边缘检测结果图像
     */
    public native Bitmap detectEdges(Bitmap inputBitmap, double threshold1, double threshold2);
    
    /**
     * 图像处理功能 - 直方图均衡化
     * 增强图像对比度
     * 
     * @param inputBitmap 输入图像
     * @return 对比度增强后的图像
     */
    public native Bitmap equalizeHistogram(Bitmap inputBitmap);
    
    /**
     * 高级功能 - 人脸检测
     * 
     * @param inputBitmap 输入图像
     * @param cascadePath Haar级联分类器文件路径
     * @return 标记了人脸位置的图像
     */
    public native Bitmap detectFaces(Bitmap inputBitmap, String cascadePath);
    
    /**
     * 特征检测 - SIFT特征点检测
     * 
     * @param inputBitmap 输入图像
     * @return 标记了特征点的图像
     */
    public native Bitmap detectSIFTFeatures(Bitmap inputBitmap);
    
    /**
     * 图像变换 - 透视变换
     * 
     * @param inputBitmap 输入图像
     * @param srcPoints 源点坐标数组（8个float值，4个点的x,y坐标）
     * @param dstPoints 目标点坐标数组（8个float值，4个点的x,y坐标）
     * @param outputWidth 输出图像宽度
     * @param outputHeight 输出图像高度
     * @return 透视变换后的图像
     */
    public native Bitmap perspectiveTransform(Bitmap inputBitmap, 
                                            float[] srcPoints, 
                                            float[] dstPoints,
                                            int outputWidth, 
                                            int outputHeight);
    
    /**
     * 图像分割 - 基于颜色的分割
     * 
     * @param inputBitmap 输入图像
     * @param hueMin 色调最小值
     * @param hueMax 色调最大值
     * @param satMin 饱和度最小值
     * @param satMax 饱和度最大值
     * @param valMin 明度最小值
     * @param valMax 明度最大值
     * @return 分割后的二值图像
     */
    public native Bitmap colorSegmentation(Bitmap inputBitmap,
                                         int hueMin, int hueMax,
                                         int satMin, int satMax,
                                         int valMin, int valMax);
    
    /**
     * 形态学操作 - 开运算（先腐蚀后膨胀）
     * 
     * @param inputBitmap 输入二值图像
     * @param kernelSize 形态学核大小
     * @return 处理后的图像
     */
    public native Bitmap morphologyOpen(Bitmap inputBitmap, int kernelSize);
    
    /**
     * 图像拼接 - 将两张图像水平拼接
     * 
     * @param leftBitmap 左侧图像
     * @param rightBitmap 右侧图像
     * @return 拼接后的图像
     */
    public native Bitmap stitchImagesHorizontally(Bitmap leftBitmap, Bitmap rightBitmap);
}
