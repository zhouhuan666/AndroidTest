package com.gdet.testapp.compose

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class ComposeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // 使用Material3主题
            MaterialTheme {
                // 设置界面背景色
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // 显示我们的主要内容
                    HelloWorldScreen()
                }
            }
        }
    }
}

/**
 * 主要的Compose界面
 * 这是一个最基础的Compose示例，展示了：
 * 1. 基本的文本组件
 * 2. 简单的布局
 * 3. 按钮交互
 * 4. 状态管理
 */
@Composable
fun HelloWorldScreen() {
    // 使用remember创建可变状态
    // 这是Compose中管理状态的基础方式
    var clickCount by remember { mutableStateOf(0) }
    var showMessage by remember { mutableStateOf(false) }
    
    // Column是垂直排列的布局容器
    Column(
        modifier = Modifier
            .fillMaxSize()  // 填满整个屏幕
            .padding(16.dp), // 添加内边距
        horizontalAlignment = Alignment.CenterHorizontally, // 水平居中
        verticalArrangement = Arrangement.Center // 垂直居中
    ) {
        
        // 标题文本
        Text(
            text = "欢迎学习 Jetpack Compose!",
            fontSize = 24.sp, // 字体大小
            fontWeight = FontWeight.Bold, // 字体粗细
            color = MaterialTheme.colorScheme.primary, // 使用主题颜色
            textAlign = TextAlign.Center
        )
        
        // 添加垂直间距
        Spacer(modifier = Modifier.height(32.dp))
        
        // 说明文本
        Text(
            text = "这是一个最简单的Compose示例\n点击下面的按钮开始体验！",
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 主要交互按钮
        Button(
            onClick = { 
                clickCount++
                showMessage = true
            },
            modifier = Modifier
                .fillMaxWidth(0.8f) // 宽度占父容器的80%
                .height(48.dp)
        ) {
            Text(
                text = "点击我！",
                fontSize = 18.sp
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 显示点击次数
        if (clickCount > 0) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🎉 太棒了！",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "你已经点击了 $clickCount 次",
                        fontSize = 16.sp
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 重置按钮
        if (clickCount > 0) {
            OutlinedButton(
                onClick = { 
                    clickCount = 0
                    showMessage = false
                }
            ) {
                Text("重置计数")
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // 学习提示
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "💡 Compose 基础概念：",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "• @Composable: 标记可组合函数\n" +
                           "• remember: 记住状态值\n" +
                           "• Column/Row: 垂直/水平布局\n" +
                           "• Modifier: 修改组件外观和行为\n" +
                           "• State: 响应式状态管理",
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

/**
 * 预览函数
 * 在Android Studio中可以实时预览Compose界面
 * 不需要运行应用就能看到效果
 */
@Preview(showBackground = true)
@Composable
fun PreviewHelloWorldScreen() {
    MaterialTheme {
        HelloWorldScreen()
    }
}

// 移除未使用的代码以避免编译警告