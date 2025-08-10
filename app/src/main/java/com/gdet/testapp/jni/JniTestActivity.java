package com.gdet.testapp.jni;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.gdet.testapp.R;

/**
 * JNI测试Activity
 * 
 * 这个Activity演示了如何使用JNI调用C++代码
 * 包含多种数据类型的传递和处理示例
 * 
 * @author Android Developer
 */
public class JniTestActivity extends AppCompatActivity {
    
    private static final String TAG = "JniTestActivity";
    
    // JNI助手实例
    private JniHelper jniHelper;
    
    // UI组件
    private TextView tvResult;
    private EditText etNumber1, etNumber2, etName;
    private Button btnGreeting, btnAdd, btnProcessString, btnSystemInfo;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jni_test);
        
        // 初始化JNI助手
        initJniHelper();
        
        // 初始化UI组件
        initViews();
        
        // 设置按钮点击事件
        setupClickListeners();
        
        Log.i(TAG, "JniTestActivity created successfully");
    }
    
    /**
     * 初始化JNI助手
     */
    private void initJniHelper() {
        try {
            jniHelper = new JniHelper();
            Log.i(TAG, "JniHelper initialized successfully");
            showToast("JNI库加载成功！");
        } catch (UnsatisfiedLinkError e) {
            Log.e(TAG, "Failed to load JNI library", e);
            showToast("JNI库加载失败: " + e.getMessage());
        }
    }
    
    /**
     * 初始化UI组件
     */
    private void initViews() {
        tvResult = findViewById(R.id.tv_result);
        etNumber1 = findViewById(R.id.et_number1);
        etNumber2 = findViewById(R.id.et_number2);
        etName = findViewById(R.id.et_name);
        btnGreeting = findViewById(R.id.btn_greeting);
        btnAdd = findViewById(R.id.btn_add);
        btnProcessString = findViewById(R.id.btn_process_string);
        btnSystemInfo = findViewById(R.id.btn_system_info);
        
        // 设置默认值
        etNumber1.setText("10");
        etNumber2.setText("20");
        etName.setText("Android开发者");
    }
    
    /**
     * 设置按钮点击事件
     */
    private void setupClickListeners() {
        // 获取问候语按钮
        btnGreeting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                testGreeting();
            }
        });
        
        // 加法运算按钮
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                testAddition();
            }
        });
        
        // 字符串处理按钮
        btnProcessString.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                testStringProcessing();
            }
        });
        
        // 系统信息按钮
        btnSystemInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                testSystemInfo();
            }
        });
    }
    
    /**
     * 测试获取问候语功能
     */
    private void testGreeting() {
        try {
            Log.i(TAG, "Testing greeting from native...");
            String greeting = jniHelper.getGreetingFromNative();
            tvResult.setText("问候语测试:\n" + greeting);
            Log.i(TAG, "Greeting received: " + greeting);
        } catch (Exception e) {
            Log.e(TAG, "Error getting greeting", e);
            tvResult.setText("错误: " + e.getMessage());
        }
    }
    
    /**
     * 测试加法运算功能
     */
    private void testAddition() {
        try {
            // 获取输入的数字
            String num1Str = etNumber1.getText().toString().trim();
            String num2Str = etNumber2.getText().toString().trim();
            
            if (num1Str.isEmpty() || num2Str.isEmpty()) {
                showToast("请输入两个数字");
                return;
            }
            
            int num1 = Integer.parseInt(num1Str);
            int num2 = Integer.parseInt(num2Str);
            
            Log.i(TAG, "Testing addition: " + num1 + " + " + num2);
            
            // 调用JNI方法进行加法运算
            int result = jniHelper.addTwoNumbers(num1, num2);
            
            String resultText = String.format("加法运算测试:\n%d + %d = %d\n\n计算在C++中完成", 
                                            num1, num2, result);
            tvResult.setText(resultText);
            
            Log.i(TAG, "Addition result: " + result);
            
        } catch (NumberFormatException e) {
            Log.e(TAG, "Invalid number format", e);
            showToast("请输入有效的数字");
        } catch (Exception e) {
            Log.e(TAG, "Error in addition", e);
            tvResult.setText("错误: " + e.getMessage());
        }
    }
    
    /**
     * 测试字符串处理功能
     */
    private void testStringProcessing() {
        try {
            String name = etName.getText().toString().trim();
            
            if (name.isEmpty()) {
                showToast("请输入名字");
                return;
            }
            
            Log.i(TAG, "Testing string processing with: " + name);
            
            // 调用JNI方法处理字符串
            String processedString = jniHelper.processString(name);
            
            String resultText = "字符串处理测试:\n输入: " + name + "\n输出: " + processedString;
            tvResult.setText(resultText);
            
            Log.i(TAG, "Processed string: " + processedString);
            
        } catch (Exception e) {
            Log.e(TAG, "Error processing string", e);
            tvResult.setText("错误: " + e.getMessage());
        }
    }
    
    /**
     * 测试获取系统信息功能
     */
    private void testSystemInfo() {
        try {
            Log.i(TAG, "Testing system info retrieval...");
            
            // 调用JNI方法获取系统信息
            String systemInfo = jniHelper.getSystemInfo();
            
            tvResult.setText("系统信息测试:\n" + systemInfo);
            
            Log.i(TAG, "System info retrieved successfully");
            
        } catch (Exception e) {
            Log.e(TAG, "Error getting system info", e);
            tvResult.setText("错误: " + e.getMessage());
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
        Log.i(TAG, "JniTestActivity destroyed");
    }
}
