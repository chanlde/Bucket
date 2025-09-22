package com.dji.network.utils

import com.dji.network.utils.GeneralUtils.Companion.getSn
import com.tji.network.TcpConnection
import org.json.JSONObject

class NetWorkUtils {
    companion object {
        /**
         * 服务器ip
         */
        const val serverIp = "146.56.250.203"

        /**
         * 鉴权和登录端口
         */
        const val port1 = 8011

        /**
         * 查看产品信息端口
         */
        const val port2 = 8012

        /**
         * 连接tcp服务器端口
         */
        const val port3 = 8013


        /**
         * 更新apk文件
         */
        const val port4 = 8014
        /**
         * 发送时间数据的通用函数
         *
         * @param timeType 时间类型（如 "takeoff_time"）
         * @param sn 产品序列号，默认为 "SN123456"
         *
         * @sample
         * ```kotlin
         * Utils.sendTimeData("takeoff_time", "SN999888")
         * Utils.sendTimeData("landing_time") // 使用默认SN
         * ```
         */

        const val updateUrl = "http://146.56.250.203:8012/productManager/pyloadFireBucket/servoControlRequest"

        fun sendTimeData(timeType: String) {
            // 构建 Payload 数据
            val payload = mapOf(
                timeType to System.currentTimeMillis(),
                "sn" to getSn()
            )

            val jsonStr = JSONObject(payload).toString()
            TcpConnection.getInstance().sendMessage(jsonStr)
        }
    }
}