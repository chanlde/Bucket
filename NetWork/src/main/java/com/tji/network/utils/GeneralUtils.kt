package com.dji.network.utils

import android.view.MotionEvent
import android.view.View

class GeneralUtils {
    companion object {

        const val productId = "1"

        const val wifiUrl = "http://192.168.5.1/index.html"

        private var flightSerialNumber = ""

        fun setSn(tempSn: String){
            flightSerialNumber=tempSn
        }

        fun getSn():String{
            return flightSerialNumber
        }

        fun View.setPressScaleEffect(scale: Float = 0.9f, duration: Long = 50) { // 减少动画时间
            this.setOnTouchListener { v, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        v.animate().scaleX(scale).scaleY(scale).setDuration(duration).start()
                    }

                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        v.animate().scaleX(1f).scaleY(1f).setDuration(duration).start()
                        // 立即执行点击，不等动画结束
                        v.performClick()
                    }
                }
                true
            }
        }

    }
}