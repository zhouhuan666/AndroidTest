package com.gdet.testapp.kotlin.coroutine

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

/**
 *
 * @author JNCHOU
 * 版本：1.0
 * 创建日期：2023-12-01
 * 描述：
 *
 */
class CoroutineActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {

        }
    }



}