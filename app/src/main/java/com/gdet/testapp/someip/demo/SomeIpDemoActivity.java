package com.gdet.testapp.someip.demo;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.gdet.testapp.R;
import com.gdet.testapp.someip.car.CarServices;
import com.gdet.testapp.someip.client.SomeIpServiceConsumer;
import com.gdet.testapp.someip.network.SomeIpTransport;
import com.gdet.testapp.someip.protocol.SomeIpMessage;
import com.gdet.testapp.someip.service.HvacServiceProvider;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * SOME/IP演示Activity
 * 演示车载SOME/IP通信功能，包括HVAC服务的控制和事件订阅
 * 
 * @author Android Team
 * @version 1.0
 */
public class SomeIpDemoActivity extends AppCompatActivity {
    
    private static final String TAG = "SomeIpDemo";
    
    // UI组件
    private TextView tvStatus;
    private TextView tvTemperature;
    private TextView tvFanSpeed;
    private Button btnStartService;
    private Button btnStopService;
    
    // SOME/IP组件
    private SomeIpTransport transport;
    private HvacServiceProvider hvacServiceProvider;
    private SomeIpServiceConsumer serviceConsumer;
    
    // 后台执行器
    private ExecutorService executorService;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_someip_demo);
        
        initViews();
        setupClickListeners();
        initializeSomeIp();
        
        Log.d(TAG, "SOME/IP Demo Activity created");
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopSomeIp();
        
        if (executorService != null) {
            executorService.shutdown();
        }
    }
    
    /**
     * 初始化视图组件
     */
    private void initViews() {
        tvStatus = findViewById(R.id.tv_status);
        tvTemperature = findViewById(R.id.tv_temperature);
        tvFanSpeed = findViewById(R.id.tv_fan_speed);
        btnStartService = findViewById(R.id.btn_start_service);
        btnStopService = findViewById(R.id.btn_stop_service);
        
        // 初始状态
        btnStopService.setEnabled(false);
    }
    
    /**
     * 设置按钮点击监听器
     */
    private void setupClickListeners() {
        btnStartService.setOnClickListener(v -> startHvacService());
        btnStopService.setOnClickListener(v -> stopHvacService());
        
        findViewById(R.id.btn_discover_service).setOnClickListener(v -> discoverHvacService());
        findViewById(R.id.btn_get_temperature).setOnClickListener(v -> getTemperature());
        findViewById(R.id.btn_set_temperature).setOnClickListener(v -> setTemperature(25.0f));
        findViewById(R.id.btn_get_fan_speed).setOnClickListener(v -> getFanSpeed());
        findViewById(R.id.btn_set_fan_speed).setOnClickListener(v -> setFanSpeed(5));
        findViewById(R.id.btn_subscribe_events).setOnClickListener(v -> subscribeHvacEvents());
    }
    
    /**
     * 初始化SOME/IP组件
     */
    private void initializeSomeIp() {
        executorService = Executors.newCachedThreadPool();
        
        executorService.execute(() -> {
            try {
                transport = new SomeIpTransport();
                transport.start(30502); // 使用不同的端口避免冲突
                
                serviceConsumer = new SomeIpServiceConsumer(transport);
                serviceConsumer.start();
                
                runOnUiThread(() -> updateStatus("SOME/IP传输层已启动"));
                Log.d(TAG, "SOME/IP transport initialized");
                
            } catch (IOException e) {
                Log.e(TAG, "Failed to initialize SOME/IP", e);
                runOnUiThread(() -> {
                    updateStatus("初始化失败: " + e.getMessage());
                    showToast("网络初始化失败，请检查网络权限");
                });
            } catch (Exception e) {
                Log.e(TAG, "Unexpected error in SOME/IP initialization", e);
                runOnUiThread(() -> {
                    updateStatus("初始化异常: " + e.getMessage());
                    showToast("系统异常，请重试");
                });
            }
        });
    }
    
    /**
     * 启动HVAC服务
     */
    private void startHvacService() {
        executorService.execute(() -> {
            try {
                if (transport == null || !transport.isRunning()) {
                    runOnUiThread(() -> showToast("请先初始化传输层"));
                    return;
                }
                
                hvacServiceProvider = new HvacServiceProvider(transport);
                hvacServiceProvider.start();
                
                runOnUiThread(() -> {
                    btnStartService.setEnabled(false);
                    btnStopService.setEnabled(true);
                    updateStatus("HVAC服务已启动");
                    showToast("HVAC服务启动成功");
                });
                
                Log.d(TAG, "HVAC service started successfully");
                
            } catch (Exception e) {
                Log.e(TAG, "Failed to start HVAC service", e);
                runOnUiThread(() -> showToast("启动服务失败: " + e.getMessage()));
            }
        });
    }
    
    /**
     * 停止HVAC服务
     */
    private void stopHvacService() {
        executorService.execute(() -> {
            try {
                if (hvacServiceProvider != null) {
                    hvacServiceProvider.stop();
                    hvacServiceProvider = null;
                }
                
                runOnUiThread(() -> {
                    btnStartService.setEnabled(true);
                    btnStopService.setEnabled(false);
                    updateStatus("HVAC服务已停止");
                });
                
                Log.d(TAG, "HVAC service stopped");
                
            } catch (Exception e) {
                Log.e(TAG, "Failed to stop HVAC service", e);
                runOnUiThread(() -> showToast("停止服务失败: " + e.getMessage()));
            }
        });
    }
    
    /**
     * 发现HVAC服务
     */
    private void discoverHvacService() {
        executorService.execute(() -> {
            try {
                if (serviceConsumer == null) {
                    runOnUiThread(() -> showToast("服务消费者未初始化"));
                    return;
                }
                
                runOnUiThread(() -> updateStatus("正在发现HVAC服务..."));
                
                SomeIpServiceConsumer.ServiceInfo serviceInfo = 
                    serviceConsumer.discoverService(CarServices.HVAC_SERVICE_ID, 10);
                
                if (serviceInfo != null) {
                    String message = "发现HVAC服务: " + serviceInfo.getAddress() + 
                                   ":" + serviceInfo.getPort();
                    runOnUiThread(() -> {
                        updateStatus(message);
                        showToast("服务发现成功");
                    });
                    Log.d(TAG, "HVAC service discovered: " + serviceInfo);
                } else {
                    runOnUiThread(() -> {
                        updateStatus("未发现HVAC服务");
                        showToast("服务发现失败，请确保服务已启动");
                    });
                    Log.w(TAG, "HVAC service not found");
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Service discovery failed", e);
                runOnUiThread(() -> {
                    updateStatus("服务发现异常");
                    showToast("服务发现失败: " + e.getMessage());
                });
            }
        });
    }
    
    /**
     * 获取温度
     */
    private void getTemperature() {
        executorService.execute(() -> {
            try {
                SomeIpMessage response = serviceConsumer.callMethod(
                        CarServices.HVAC_SERVICE_ID,
                        CarServices.HvacMethods.GET_TEMPERATURE
                );
                
                if (response != null && response.getPayload().length >= 4) {
                    ByteBuffer buffer = ByteBuffer.wrap(response.getPayload());
                    buffer.order(ByteOrder.BIG_ENDIAN);
                    float temperature = buffer.getFloat();
                    
                    runOnUiThread(() -> updateTemperature(temperature));
                    Log.d(TAG, "Temperature received: " + temperature);
                } else {
                    runOnUiThread(() -> showToast("获取温度失败"));
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Failed to get temperature", e);
                runOnUiThread(() -> showToast("获取温度失败: " + e.getMessage()));
            }
        });
    }
    
    /**
     * 设置温度
     * @param temperature 目标温度
     */
    private void setTemperature(float temperature) {
        executorService.execute(() -> {
            try {
                ByteBuffer buffer = ByteBuffer.allocate(4);
                buffer.order(ByteOrder.BIG_ENDIAN);
                buffer.putFloat(temperature);
                byte[] payload = buffer.array();
                
                SomeIpMessage response = serviceConsumer.callMethod(
                        CarServices.HVAC_SERVICE_ID,
                        CarServices.HvacMethods.SET_TEMPERATURE,
                        payload
                );
                
                if (response != null) {
                    runOnUiThread(() -> showToast("温度设置成功"));
                    Log.d(TAG, "Temperature set to: " + temperature);
                    
                    // 延迟后获取最新温度
                    new Handler(Looper.getMainLooper()).postDelayed(() -> getTemperature(), 1000);
                } else {
                    runOnUiThread(() -> showToast("设置温度失败"));
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Failed to set temperature", e);
                runOnUiThread(() -> showToast("设置温度失败: " + e.getMessage()));
            }
        });
    }
    
    /**
     * 获取风速
     */
    private void getFanSpeed() {
        executorService.execute(() -> {
            try {
                SomeIpMessage response = serviceConsumer.callMethod(
                        CarServices.HVAC_SERVICE_ID,
                        CarServices.HvacMethods.GET_FAN_SPEED
                );
                
                if (response != null && response.getPayload().length >= 4) {
                    ByteBuffer buffer = ByteBuffer.wrap(response.getPayload());
                    buffer.order(ByteOrder.BIG_ENDIAN);
                    int fanSpeed = buffer.getInt();
                    
                    runOnUiThread(() -> updateFanSpeed(fanSpeed));
                    Log.d(TAG, "Fan speed received: " + fanSpeed);
                } else {
                    runOnUiThread(() -> showToast("获取风速失败"));
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Failed to get fan speed", e);
                runOnUiThread(() -> showToast("获取风速失败: " + e.getMessage()));
            }
        });
    }
    
    /**
     * 设置风速
     * @param fanSpeed 目标风速
     */
    private void setFanSpeed(int fanSpeed) {
        executorService.execute(() -> {
            try {
                ByteBuffer buffer = ByteBuffer.allocate(4);
                buffer.order(ByteOrder.BIG_ENDIAN);
                buffer.putInt(fanSpeed);
                byte[] payload = buffer.array();
                
                SomeIpMessage response = serviceConsumer.callMethod(
                        CarServices.HVAC_SERVICE_ID,
                        CarServices.HvacMethods.SET_FAN_SPEED,
                        payload
                );
                
                if (response != null) {
                    runOnUiThread(() -> showToast("风速设置成功"));
                    Log.d(TAG, "Fan speed set to: " + fanSpeed);
                    
                    // 延迟后获取最新风速
                    new Handler(Looper.getMainLooper()).postDelayed(() -> getFanSpeed(), 1000);
                } else {
                    runOnUiThread(() -> showToast("设置风速失败"));
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Failed to set fan speed", e);
                runOnUiThread(() -> showToast("设置风速失败: " + e.getMessage()));
            }
        });
    }
    
    /**
     * 订阅HVAC事件
     */
    private void subscribeHvacEvents() {
        executorService.execute(() -> {
            try {
                // 订阅温度变化事件
                boolean temperatureSubscribed = serviceConsumer.subscribeEvent(
                        CarServices.HVAC_SERVICE_ID,
                        CarServices.Events.TEMPERATURE_CHANGED,
                        this::handleTemperatureChangedEvent
                );
                
                // 订阅风速变化事件
                boolean fanSpeedSubscribed = serviceConsumer.subscribeEvent(
                        CarServices.HVAC_SERVICE_ID,
                        CarServices.Events.FAN_SPEED_CHANGED,
                        this::handleFanSpeedChangedEvent
                );
                
                if (temperatureSubscribed && fanSpeedSubscribed) {
                    runOnUiThread(() -> updateStatus("已订阅HVAC事件"));
                    Log.d(TAG, "HVAC events subscribed");
                } else {
                    runOnUiThread(() -> showToast("事件订阅失败"));
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Failed to subscribe events", e);
                runOnUiThread(() -> showToast("订阅事件失败: " + e.getMessage()));
            }
        });
    }
    
    /**
     * 处理温度变化事件
     * @param message 事件消息
     */
    private void handleTemperatureChangedEvent(@NonNull SomeIpMessage message) {
        if (message.getPayload().length >= 4) {
            ByteBuffer buffer = ByteBuffer.wrap(message.getPayload());
            buffer.order(ByteOrder.BIG_ENDIAN);
            float temperature = buffer.getFloat();
            
            runOnUiThread(() -> {
                updateTemperature(temperature);
                showToast("温度变化事件: " + String.format("%.1f", temperature) + "°C");
            });
        }
    }
    
    /**
     * 处理风速变化事件
     * @param message 事件消息
     */
    private void handleFanSpeedChangedEvent(@NonNull SomeIpMessage message) {
        if (message.getPayload().length >= 4) {
            ByteBuffer buffer = ByteBuffer.wrap(message.getPayload());
            buffer.order(ByteOrder.BIG_ENDIAN);
            int fanSpeed = buffer.getInt();
            
            runOnUiThread(() -> {
                updateFanSpeed(fanSpeed);
                showToast("风速变化事件: " + fanSpeed);
            });
        }
    }
    
    /**
     * 停止SOME/IP
     */
    private void stopSomeIp() {
        executorService.execute(() -> {
            try {
                if (hvacServiceProvider != null) {
                    hvacServiceProvider.stop();
                }
                
                if (serviceConsumer != null) {
                    serviceConsumer.stop();
                }
                
                if (transport != null) {
                    transport.stop();
                }
                
                Log.d(TAG, "SOME/IP stopped");
                
            } catch (Exception e) {
                Log.e(TAG, "Failed to stop SOME/IP", e);
            }
        });
    }
    
    /**
     * 更新状态显示
     * @param status 状态信息
     */
    private void updateStatus(@NonNull String status) {
        tvStatus.setText("状态: " + status);
    }
    
    /**
     * 更新温度显示
     * @param temperature 温度值
     */
    private void updateTemperature(float temperature) {
        tvTemperature.setText("温度: " + String.format("%.1f", temperature) + "°C");
    }
    
    /**
     * 更新风速显示
     * @param fanSpeed 风速值
     */
    private void updateFanSpeed(int fanSpeed) {
        tvFanSpeed.setText("风速: " + fanSpeed);
    }
    
    /**
     * 显示Toast消息
     * @param message 消息内容
     */
    private void showToast(@NonNull String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
} 