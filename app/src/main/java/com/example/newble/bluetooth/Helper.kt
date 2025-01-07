package com.example.newble.bluetooth

class Helper {

    companion object {
    fun connectToDev(bluetoothManager: BluetoothManager, onDataReceived: (String) -> Unit) {
        return bluetoothManager.startScan(onDataReceived)
    }
    }


}