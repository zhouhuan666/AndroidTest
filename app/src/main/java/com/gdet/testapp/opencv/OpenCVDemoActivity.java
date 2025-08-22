package com.gdet.testapp.opencv;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.gdet.testapp.R;

import java.io.IOException;

/**
 * OpenCV演示Activity
 * 
 * 这个Activity演示了如何使用OpenCV进行各种图像处理操作
 * 展示了成熟C++库集成到Android应用的完整流程
 * 
 * 主要功能：
 * 1. 加载和显示图像
 * 2. 各种OpenCV图像处理算法
 * 3. 实时参数调整
 * 4. 处理结果展示
 * 
 * @author Android Developer
 */
public class OpenCVDemoActivity extends AppCompatActivity {
    
    private static final String TAG = "OpenCVDemo";
    private static final int PICK_IMAGE_REQUEST = 1001;
    
    // OpenCV助手类实例
    private OpenCVHelper openCVHelper;
    
    // UI组件
    private ImageView ivOriginal, ivProcessed;
    private TextView tvOpenCVVersion, tvImageInfo, tvProcessingResult;
    private Button btnLoadImage, btnGrayscale, btnBlur, btnEdges, btnHistEq;
    private Button btnColorSeg, btnMorphology, btnPerspective, btnStitch;
    private SeekBar seekBlurKernel, seekEdgeThreshold1, seekEdgeThreshold2;
    private TextView tvBlurKernel, tvEdgeThreshold1, tvEdgeThreshold2;
    
    // 图像数据
    private Bitmap originalBitmap;
    private Bitmap processedBitmap;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opencv_demo);
        
        // 初始化OpenCV
        initOpenCV();
        
        // 初始化UI组件
        initViews();
        
        // 设置事件监听器
        setupListeners();
        
        // 加载默认图像
        loadDefaultImage();
        
        Log.i(TAG, "OpenCVDemoActivity created successfully");
    }
    
    /**
     * 初始化OpenCV
     */
    private void initOpenCV() {
        try {
            if (OpenCVHelper.isOpenCVAvailable()) {
                openCVHelper = new OpenCVHelper();
                String version = openCVHelper.getOpenCVVersion();
                Log.i(TAG, "OpenCV initialized: " + version);
                showToast("OpenCV初始化成功！");
            } else {
                Log.e(TAG, "OpenCV not available");
                showToast("OpenCV不可用，请检查库文件");
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize OpenCV", e);
            showToast("OpenCV初始化失败: " + e.getMessage());
        }
    }
    
    /**
     * 初始化UI组件
     */
    private void initViews() {
        // 图像显示
        ivOriginal = findViewById(R.id.iv_original);
        ivProcessed = findViewById(R.id.iv_processed);
        
        // 信息显示
        tvOpenCVVersion = findViewById(R.id.tv_opencv_version);
        tvImageInfo = findViewById(R.id.tv_image_info);
        tvProcessingResult = findViewById(R.id.tv_processing_result);
        
        // 操作按钮
        btnLoadImage = findViewById(R.id.btn_load_image);
        btnGrayscale = findViewById(R.id.btn_grayscale);
        btnBlur = findViewById(R.id.btn_blur);
        btnEdges = findViewById(R.id.btn_edges);
        btnHistEq = findViewById(R.id.btn_hist_eq);
        btnColorSeg = findViewById(R.id.btn_color_seg);
        btnMorphology = findViewById(R.id.btn_morphology);
        btnPerspective = findViewById(R.id.btn_perspective);
        btnStitch = findViewById(R.id.btn_stitch);
        
        // 参数调节
        seekBlurKernel = findViewById(R.id.seek_blur_kernel);
        seekEdgeThreshold1 = findViewById(R.id.seek_edge_threshold1);
        seekEdgeThreshold2 = findViewById(R.id.seek_edge_threshold2);
        
        tvBlurKernel = findViewById(R.id.tv_blur_kernel);
        tvEdgeThreshold1 = findViewById(R.id.tv_edge_threshold1);
        tvEdgeThreshold2 = findViewById(R.id.tv_edge_threshold2);
        
        // 设置初始值
        seekBlurKernel.setMax(49);
        seekBlurKernel.setProgress(9);
        seekEdgeThreshold1.setMax(255);
        seekEdgeThreshold1.setProgress(50);
        seekEdgeThreshold2.setMax(255);
        seekEdgeThreshold2.setProgress(150);
        
        updateParameterLabels();
        
        // 显示OpenCV版本
        if (openCVHelper != null) {
            tvOpenCVVersion.setText(openCVHelper.getOpenCVVersion());
        }
    }
    
    /**
     * 设置事件监听器
     */
    private void setupListeners() {
        // 加载图像按钮
        btnLoadImage.setOnClickListener(v -> loadImageFromGallery());
        
        // 图像处理按钮
        btnGrayscale.setOnClickListener(v -> processGrayscale());
        btnBlur.setOnClickListener(v -> processBlur());
        btnEdges.setOnClickListener(v -> processEdgeDetection());
        btnHistEq.setOnClickListener(v -> processHistogramEqualization());
        btnColorSeg.setOnClickListener(v -> processColorSegmentation());
        btnMorphology.setOnClickListener(v -> processMorphology());
        btnPerspective.setOnClickListener(v -> processPerspectiveTransform());
        btnStitch.setOnClickListener(v -> processImageStitching());
        
        // 参数调节滑块
        seekBlurKernel.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateParameterLabels();
                if (fromUser && originalBitmap != null) {
                    processBlur(); // 实时更新
                }
            }
            
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        
        seekEdgeThreshold1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateParameterLabels();
                if (fromUser && originalBitmap != null) {
                    processEdgeDetection(); // 实时更新
                }
            }
            
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        
        seekEdgeThreshold2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateParameterLabels();
                if (fromUser && originalBitmap != null) {
                    processEdgeDetection(); // 实时更新
                }
            }
            
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }
    
    /**
     * 更新参数标签
     */
    private void updateParameterLabels() {
        int blurKernel = seekBlurKernel.getProgress();
        if (blurKernel % 2 == 0) blurKernel++; // 确保是奇数
        tvBlurKernel.setText("模糊核大小: " + blurKernel);
        
        tvEdgeThreshold1.setText("边缘阈值1: " + seekEdgeThreshold1.getProgress());
        tvEdgeThreshold2.setText("边缘阈值2: " + seekEdgeThreshold2.getProgress());
    }
    
    /**
     * 从相册加载图像
     */
    private void loadImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }
    
    /**
     * 加载默认图像
     */
    private void loadDefaultImage() {
        try {
            // 从资源中加载一张示例图片
            originalBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.sample_image);
            if (originalBitmap != null) {
                ivOriginal.setImageBitmap(originalBitmap);
                updateImageInfo(originalBitmap);
                Log.i(TAG, "Default image loaded successfully");
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to load default image", e);
            // 如果没有示例图片，创建一个简单的渐变图片
            createTestImage();
        }
    }
    
    /**
     * 创建测试图像
     */
    private void createTestImage() {
        int width = 400;
        int height = 300;
        originalBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        
        // 创建简单的渐变图案
        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int index = y * width + x;
                int red = (x * 255) / width;
                int green = (y * 255) / height;
                int blue = 128;
                pixels[index] = (255 << 24) | (red << 16) | (green << 8) | blue;
            }
        }
        
        originalBitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        ivOriginal.setImageBitmap(originalBitmap);
        updateImageInfo(originalBitmap);
        Log.i(TAG, "Test image created");
    }
    
    /**
     * 更新图像信息显示
     */
    private void updateImageInfo(Bitmap bitmap) {
        if (bitmap != null) {
            String info = String.format("图像尺寸: %d x %d\n配置: %s\n大小: %.2f KB",
                    bitmap.getWidth(), bitmap.getHeight(),
                    bitmap.getConfig().toString(),
                    bitmap.getByteCount() / 1024.0f);
            tvImageInfo.setText(info);
        }
    }
    
    /**
     * 灰度转换处理
     */
    private void processGrayscale() {
        if (originalBitmap == null || openCVHelper == null) {
            showToast("请先加载图像");
            return;
        }
        
        try {
            long startTime = System.currentTimeMillis();
            processedBitmap = openCVHelper.convertToGray(originalBitmap);
            long endTime = System.currentTimeMillis();
            
            if (processedBitmap != null) {
                ivProcessed.setImageBitmap(processedBitmap);
                tvProcessingResult.setText("灰度转换完成\n处理时间: " + (endTime - startTime) + "ms");
                Log.i(TAG, "Grayscale conversion completed in " + (endTime - startTime) + "ms");
            } else {
                showToast("灰度转换失败");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in grayscale conversion", e);
            showToast("灰度转换出错: " + e.getMessage());
        }
    }
    
    /**
     * 高斯模糊处理
     */
    private void processBlur() {
        if (originalBitmap == null || openCVHelper == null) {
            showToast("请先加载图像");
            return;
        }
        
        try {
            int kernelSize = seekBlurKernel.getProgress();
            if (kernelSize % 2 == 0) kernelSize++; // 确保是奇数
            if (kernelSize < 3) kernelSize = 3;
            
            long startTime = System.currentTimeMillis();
            processedBitmap = openCVHelper.applyGaussianBlur(originalBitmap, kernelSize);
            long endTime = System.currentTimeMillis();
            
            if (processedBitmap != null) {
                ivProcessed.setImageBitmap(processedBitmap);
                tvProcessingResult.setText("高斯模糊完成\n核大小: " + kernelSize + 
                                         "\n处理时间: " + (endTime - startTime) + "ms");
                Log.i(TAG, "Gaussian blur completed with kernel size " + kernelSize);
            } else {
                showToast("高斯模糊失败");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in Gaussian blur", e);
            showToast("高斯模糊出错: " + e.getMessage());
        }
    }
    
    /**
     * 边缘检测处理
     */
    private void processEdgeDetection() {
        if (originalBitmap == null || openCVHelper == null) {
            showToast("请先加载图像");
            return;
        }
        
        try {
            double threshold1 = seekEdgeThreshold1.getProgress();
            double threshold2 = seekEdgeThreshold2.getProgress();
            
            long startTime = System.currentTimeMillis();
            processedBitmap = openCVHelper.detectEdges(originalBitmap, threshold1, threshold2);
            long endTime = System.currentTimeMillis();
            
            if (processedBitmap != null) {
                ivProcessed.setImageBitmap(processedBitmap);
                tvProcessingResult.setText("边缘检测完成\n阈值1: " + threshold1 + 
                                         "\n阈值2: " + threshold2 +
                                         "\n处理时间: " + (endTime - startTime) + "ms");
                Log.i(TAG, "Edge detection completed with thresholds " + threshold1 + ", " + threshold2);
            } else {
                showToast("边缘检测失败");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in edge detection", e);
            showToast("边缘检测出错: " + e.getMessage());
        }
    }
    
    /**
     * 直方图均衡化处理
     */
    private void processHistogramEqualization() {
        if (originalBitmap == null || openCVHelper == null) {
            showToast("请先加载图像");
            return;
        }
        
        try {
            long startTime = System.currentTimeMillis();
            processedBitmap = openCVHelper.equalizeHistogram(originalBitmap);
            long endTime = System.currentTimeMillis();
            
            if (processedBitmap != null) {
                ivProcessed.setImageBitmap(processedBitmap);
                tvProcessingResult.setText("直方图均衡化完成\n处理时间: " + (endTime - startTime) + "ms");
                Log.i(TAG, "Histogram equalization completed");
            } else {
                showToast("直方图均衡化失败");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in histogram equalization", e);
            showToast("直方图均衡化出错: " + e.getMessage());
        }
    }
    
    /**
     * 颜色分割处理
     */
    private void processColorSegmentation() {
        if (originalBitmap == null || openCVHelper == null) {
            showToast("请先加载图像");
            return;
        }
        
        try {
            // 分割蓝色区域 (HSV空间中的蓝色范围)
            long startTime = System.currentTimeMillis();
            processedBitmap = openCVHelper.colorSegmentation(originalBitmap, 
                    100, 130,  // 色调范围 (蓝色)
                    50, 255,   // 饱和度范围
                    50, 255);  // 明度范围
            long endTime = System.currentTimeMillis();
            
            if (processedBitmap != null) {
                ivProcessed.setImageBitmap(processedBitmap);
                tvProcessingResult.setText("颜色分割完成\n目标颜色: 蓝色\n处理时间: " + (endTime - startTime) + "ms");
                Log.i(TAG, "Color segmentation completed");
            } else {
                showToast("颜色分割失败");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in color segmentation", e);
            showToast("颜色分割出错: " + e.getMessage());
        }
    }
    
    /**
     * 形态学处理
     */
    private void processMorphology() {
        if (originalBitmap == null || openCVHelper == null) {
            showToast("请先加载图像");
            return;
        }
        
        try {
            long startTime = System.currentTimeMillis();
            processedBitmap = openCVHelper.morphologyOpen(originalBitmap, 5);
            long endTime = System.currentTimeMillis();
            
            if (processedBitmap != null) {
                ivProcessed.setImageBitmap(processedBitmap);
                tvProcessingResult.setText("形态学开运算完成\n核大小: 5\n处理时间: " + (endTime - startTime) + "ms");
                Log.i(TAG, "Morphology operation completed");
            } else {
                showToast("形态学处理失败");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in morphology operation", e);
            showToast("形态学处理出错: " + e.getMessage());
        }
    }
    
    /**
     * 透视变换处理
     */
    private void processPerspectiveTransform() {
        if (originalBitmap == null || openCVHelper == null) {
            showToast("请先加载图像");
            return;
        }
        
        try {
            int width = originalBitmap.getWidth();
            int height = originalBitmap.getHeight();
            
            // 定义源点和目标点
            float[] srcPoints = {
                    width * 0.1f, height * 0.1f,    // 左上角
                    width * 0.9f, height * 0.2f,    // 右上角
                    width * 0.8f, height * 0.9f,    // 右下角
                    width * 0.2f, height * 0.8f     // 左下角
            };
            
            float[] dstPoints = {
                    0, 0,                            // 左上角
                    width, 0,                        // 右上角
                    width, height,                   // 右下角
                    0, height                        // 左下角
            };
            
            long startTime = System.currentTimeMillis();
            processedBitmap = openCVHelper.perspectiveTransform(originalBitmap, 
                    srcPoints, dstPoints, width, height);
            long endTime = System.currentTimeMillis();
            
            if (processedBitmap != null) {
                ivProcessed.setImageBitmap(processedBitmap);
                tvProcessingResult.setText("透视变换完成\n处理时间: " + (endTime - startTime) + "ms");
                Log.i(TAG, "Perspective transform completed");
            } else {
                showToast("透视变换失败");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in perspective transform", e);
            showToast("透视变换出错: " + e.getMessage());
        }
    }
    
    /**
     * 图像拼接处理
     */
    private void processImageStitching() {
        if (originalBitmap == null || openCVHelper == null) {
            showToast("请先加载图像");
            return;
        }
        
        try {
            // 使用同一张图像进行拼接演示
            long startTime = System.currentTimeMillis();
            processedBitmap = openCVHelper.stitchImagesHorizontally(originalBitmap, originalBitmap);
            long endTime = System.currentTimeMillis();
            
            if (processedBitmap != null) {
                ivProcessed.setImageBitmap(processedBitmap);
                tvProcessingResult.setText("图像拼接完成\n处理时间: " + (endTime - startTime) + "ms");
                Log.i(TAG, "Image stitching completed");
            } else {
                showToast("图像拼接失败");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in image stitching", e);
            showToast("图像拼接出错: " + e.getMessage());
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            try {
                originalBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                ivOriginal.setImageBitmap(originalBitmap);
                updateImageInfo(originalBitmap);
                
                // 清除之前的处理结果
                ivProcessed.setImageBitmap(null);
                tvProcessingResult.setText("图像加载成功，请选择处理操作");
                
                Log.i(TAG, "Image loaded from gallery successfully");
            } catch (IOException e) {
                Log.e(TAG, "Failed to load image from gallery", e);
                showToast("加载图像失败: " + e.getMessage());
            }
        }
    }
    
    /**
     * 显示Toast消息
     */
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        // 释放Bitmap资源
        if (originalBitmap != null && !originalBitmap.isRecycled()) {
            originalBitmap.recycle();
        }
        if (processedBitmap != null && !processedBitmap.isRecycled()) {
            processedBitmap.recycle();
        }
        
        Log.i(TAG, "OpenCVDemoActivity destroyed");
    }
}























































