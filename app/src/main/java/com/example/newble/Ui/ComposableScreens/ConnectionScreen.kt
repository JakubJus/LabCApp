package com.example.newble.Ui.ComposableScreens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.newble.bluetooth.BluetoothManager
import kotlinx.coroutines.delay
import com.example.newble.bluetooth.Helper

@Composable
fun ConnectionScreen(bluetoothManager: BluetoothManager, isScanning: MutableState<Boolean>) {
    // Track Bluetooth states
    var isBluetoothOn by remember { mutableStateOf(bluetoothManager.isBluetoothOn()) }
    var isConnected by remember { mutableStateOf(bluetoothManager.isDeviceConnected()) }

    // Check for Bluetooth state changes
    LaunchedEffect(Unit) {
        // Continuously check Bluetooth status and connection state every time they change
        while (true) {
            isBluetoothOn = bluetoothManager.isBluetoothOn()
            isConnected = bluetoothManager.isDeviceConnected()
            delay(1)  // Delay to avoid excessive checks, adjust as needed
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Connect to device",
                style = TextStyle(fontSize = 24.sp, color = MaterialTheme.colorScheme.primary)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Bluetooth Status: ${if (isBluetoothOn) "On" else "Off"}",
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (isBluetoothOn) {
                Text(
                    text = "Device Status: ${if (isConnected) "Connected" else "Disconnected"}",
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            if(!isConnected) {
                // Bluetooth control buttons
                if (!isBluetoothOn) {
                    ElevatedButton(
                        onClick = {
                            bluetoothManager.turnBluetoothOn()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 32.dp)
                    ) {
                        Text("Turn Bluetooth On")
                    }
                } else {
                    ElevatedButton(
                        onClick = {
                            isScanning.value = true

                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 32.dp)
                    ) {
                        if(isScanning.value) {
                            Text("Stop Scan")
                        }else{
                            Text("Scan for Device")

                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    ElevatedButton(
                        onClick = {
                            bluetoothManager.toggleBluetooth()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 32.dp)
                    ) {
                        Text("Toggle Bluetooth")
                    }
                }

                if (isScanning.value) {
                    CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                }
            }
        }
    }
}


