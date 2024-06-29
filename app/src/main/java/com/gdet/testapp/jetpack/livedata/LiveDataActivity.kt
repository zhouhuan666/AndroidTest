package com.gdet.testapp.jetpack.livedata;

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.liveData
import androidx.lifecycle.map

import com.gdet.testapp.R;
import kotlinx.coroutines.delay

class LiveDataActivity : AppCompatActivity() {
//    private var liveDataUser: LiveData<UserModel>? = null


    private val userLiveData = MutableLiveData<UserModel>()
    val userNameLiveData: LiveData<String> = userLiveData.map {
        "${it.age} 岁"
    }

    private val TAG = "LiveDataActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        liveDataUser = liveData {
//            delay(5000)
//            val userModel = UserModel("张三", 18)
//            emit(userModel)
//        }
//
//        liveDataUser?.observe(this) { it ->
//            Log.e(TAG, "onCreate: ${it.toString()} ")
//        }

//        StockLiveData.get()?.observe(this){
//            Log.i(TAG, "onCreate:StockLiveData  observe 限价${it.gain} 涨幅:${it.price}")
//        }
        
        userNameLiveData.observe(this){
            Log.i(TAG, "onCreate: $it")
        }


        getUserLiveData()
    }

    private fun getUserLiveData() {
        Handler(Looper.getMainLooper()).postDelayed({
            val userModel = UserModel(
                "张三", 24
            )
            userLiveData.postValue(userModel)
        }, 2000)
    }

}
