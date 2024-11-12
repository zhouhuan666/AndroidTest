package com.gdet.testapp.coil

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import coil3.load
import com.gdet.testapp.R


class CoilActivity:AppCompatActivity() {
    private val TAG = "CoilActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_coil)
        Log.i(TAG, "onCreate: ")
        findViewById<ImageView>(R.id.img).load("https://cdn.pixabay.com/photo/2016/11/29/04/54/photographer-1867417_1280.jpg")
    }
}