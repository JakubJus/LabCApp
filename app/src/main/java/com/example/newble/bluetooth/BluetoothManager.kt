package com.example.newble.bluetooth

import android.Manifest
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import java.util.UUID

class BluetoothManager(private val context: Context) {

    private var bluetoothAdapter: BluetoothAdapter? = (context.getSystemService(Context.BLUETOOTH_SERVICE) as android.bluetooth.BluetoothManager).adapter
    private var bluetoothGatt: BluetoothGatt? = null
    private var isBluetooth: Boolean = false
    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 1001
    }

    fun isDeviceConnected(): Boolean {
        return isBluetooth
    }
    fun isBluetoothOn(): Boolean {
        return bluetoothAdapter?.isEnabled == true
    }
    fun turnBluetoothOn() {
        // Check for Bluetooth permissions
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        // If Bluetooth is off, prompt the user to enable it
        if (bluetoothAdapter?.isEnabled == false) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            context.startActivity(enableBtIntent)  // Prompt the user to turn on Bluetooth
        }
    }
    fun toggleBluetooth() {
        if (bluetoothAdapter?.isEnabled == true) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {

                return
            }
            bluetoothAdapter?.disable()
        } else {
            bluetoothAdapter?.enable()
        }
    }

    fun hasPermissions(): Boolean {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
    }

    fun requestPermissions() {
        if (context is android.app.Activity) {
            ActivityCompat.requestPermissions(
                context,
                arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT),
                REQUEST_CODE_PERMISSIONS
            )
        } else {
            Log.e("BLE", "Context is not an Activity, cannot request permissions.")
        }
    }

    fun startScan(onDataReceived: (String) -> Unit) {
        if (!hasPermissions()) {
            Log.e("BLE", "Missing required permissions for scanning.")
            onDataReceived("Missing required permissions for scanning.")
            requestPermissions()
            return
        }

        Log.d("BLE", "Scan started")
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            return
        }
        bluetoothAdapter?.bluetoothLeScanner?.startScan(object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                val device = result.device
                Log.d("BLE", "Device found: ${device.name}")

                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    Log.e("BLE", "BLUETOOTH_CONNECT permission is missing.")
                    onDataReceived("Missing permission: BLUETOOTH_CONNECT.")
                    requestPermissions()
                    return
                }

                if (device.name != null && device.name.contains("IMU")) {
                    Log.d("BLE", "IMU device found: ${device.name}")
                    bluetoothAdapter?.bluetoothLeScanner?.stopScan(this)
                    connectToDevice(device, onDataReceived)
                }
            }

            override fun onScanFailed(errorCode: Int) {
                Log.e("BLE", "Scan failed with error code: $errorCode")
                onDataReceived("Scan failed. Error code: $errorCode")
            }
        })

        Handler(Looper.getMainLooper()).postDelayed({
            bluetoothAdapter?.bluetoothLeScanner?.stopScan(object : ScanCallback() {})
            Log.d("BLE", "Scan stopped after 50 seconds.")
        }, 50000)
    }

    fun stopScan() {
        if (!hasPermissions()) {
            Log.e("BLE", "Missing required permissions to stop scanning.")
            return
        }

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            return
        }
        bluetoothAdapter?.bluetoothLeScanner?.stopScan(object : ScanCallback() {})
        Log.d("BLE", "Scanning stopped.")
    }

    private fun connectToDevice(device: BluetoothDevice, onDataReceived: (String) -> Unit) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Log.e("BLE", "Missing required permissions to connect to device.")
            onDataReceived("Missing permission: BLUETOOTH_CONNECT.")
            requestPermissions()
            return
        }

        try {
            bluetoothGatt = device.connectGatt(context, false, object : BluetoothGattCallback() {
                override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                    if (newState == BluetoothProfile.STATE_CONNECTED) {
                        Log.d("BLE", "Connected to device")
                        isBluetooth=true
                        gatt.discoverServices()
                    } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                        Log.d("BLE", "Disconnected from device")
                        onDataReceived("Disconnected from device.")
                    }
                }

                override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        val imuService = gatt.getService(UUID.fromString(BLEConstants.IMU_SERVICE_UUID))
                        val imuCharacteristic = imuService?.getCharacteristic(UUID.fromString(BLEConstants.IMU_CHARACTERISTIC_UUID))

                        imuCharacteristic?.let {
                            gatt.setCharacteristicNotification(it, true)
                            val descriptor = it.getDescriptor(UUID.fromString(BLEConstants.CLIENT_CHARACTERISTIC_CONFIG))
                            descriptor?.let {
                                it.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                                gatt.writeDescriptor(it)
                            }

                            onDataReceived("Connected to IMU device. Waiting for data...")
                            Log.d("BLE", "Connected to IMU device. Waiting for data...")
                        }
                    } else {
                        Log.e("BLE", "Service discovery failed with status: $status")
                        onDataReceived("Service discovery failed.")
                    }
                }

                override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
                    IMUDataParser.parseData(characteristic.value)?.let { onDataReceived(it.toString()) }
                }
            })
        } catch (e: SecurityException) {
            Log.e("BLE", "SecurityException during connectGatt: ${e.message}")
            onDataReceived("Error connecting to device: Missing permission.")
        }
    }
}