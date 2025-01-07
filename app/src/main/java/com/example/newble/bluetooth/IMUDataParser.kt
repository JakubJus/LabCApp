package com.example.newble.bluetooth

import android.util.Log

object IMUDataParser {
    fun parseData(data: ByteArray): Pair<String, String>? {
        val rawData = String(data, Charsets.UTF_8).trim()
        Log.d("BLE", "Raw data received: $rawData")
        val imuDataList = rawData.split(",").map { it.trim('(', ')', ' ') }

        return when (imuDataList.getOrNull(0)) {
            "A" -> {
                if (imuDataList.size >= 4) "A" to "${imuDataList[1]},${imuDataList[2]},${imuDataList[3]}"
                else null
            }
            "G" -> {
                if (imuDataList.size >= 4) "G" to "${imuDataList[1]},${imuDataList[2]},${imuDataList[3]}"
                else null
            }
            "M" -> {
                if (imuDataList.size >= 4) "M" to "${imuDataList[1]},${imuDataList[2]},${imuDataList[3]}"
                else null
            }

            else -> {
                Log.e("BLE", "Invalid type identifier: ${imuDataList.getOrNull(0)}")
                null
            }
        }
    }
}