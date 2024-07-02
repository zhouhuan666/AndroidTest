package com.gdet.testapp.jetpack.viewmodel;

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class ViewModelActivity : AppCompatActivity() {
    val myViewModel: MyViewModel by viewModels<MyViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
         myViewModel.viewModelScope.launch {

         }
    }
}
