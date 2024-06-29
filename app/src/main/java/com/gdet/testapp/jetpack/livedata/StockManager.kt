package com.gdet.testapp.jetpack.livedata

import android.util.Log
import androidx.lifecycle.LiveData

data class StockInfo(
    val price: String,
    val gain: String
)

interface StockInfoListener {
    fun sendStockInfo(stockInfo: StockInfo)
}

object StockManager {
    private var stockInfoListener: StockInfoListener? = null
    private var flag: Boolean = false
    private var gain: Int = 0

    fun stockInfoUpdate(stockInfoListener: StockInfoListener?) {
        this.stockInfoListener = stockInfoListener
        flag = true
        updateStockInfo()
    }

    fun removeUpdates() {
        flag = false
        this.stockInfoListener = null
    }


    private fun updateStockInfo() {
        Thread {
            while (flag) {
                Thread.sleep(2000)
                gain++
                stockInfoListener?.sendStockInfo(StockInfo("20.$gain", "$gain%"))
            }
        }.start()
    }

}

class StockLiveData : LiveData<StockInfo>() {
    private val TAG = "StockLiveData"

    private val stockInfoListener = object : StockInfoListener {
        override fun sendStockInfo(stockInfo: StockInfo) {
            Log.i(TAG, "sendStockInfo: ${stockInfo.toString()}")
            postValue(stockInfo)
        }

    }

    override fun onActive() {
        super.onActive()
        Log.i(TAG, "onActive: ")
        StockManager.stockInfoUpdate(stockInfoListener)
    }

    override fun onInactive() {
        super.onInactive()
        StockManager.removeUpdates()
        Log.i(TAG, "onInactive: ")
    }

    companion object {
        private var instance: StockLiveData? = null

        fun get(): StockLiveData? {
            if (instance == null) {
                synchronized(StockLiveData::class.java) {
                    if (instance == null) {
                        instance = StockLiveData()
                    }
                }
            }
            return instance
        }
    }
}