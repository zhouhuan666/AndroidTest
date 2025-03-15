package com.gdet.testapp.view;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.gdet.testapp.R;
import com.gdet.testapp.databinding.ActivityViewBinding

import java.util.Arrays;

/**
 * @author JNCHOU
 * 版本：1.0
 * 创建日期：2025-01-19
 * 描述：
 */
class ViewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityViewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toggleGroup.apply {
            setButtons(listOf("OFF", "图标1", "图标2", "AUTO"))
            setOnButtonCheckedListener(object : ToggleButtonGroup.OnButtonCheckedListener {
                override fun onButtonChecked(position: Int, text: String) {

                }

            })
        }
    }
}
