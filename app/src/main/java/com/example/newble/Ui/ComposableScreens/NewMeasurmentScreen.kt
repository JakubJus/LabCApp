package com.example.newble.Ui.ComposableScreens

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.newble.bluetooth.BluetoothManager
import com.github.mikephil.charting.data.Entry
import java.io.File
import com.example.newble.firebase.FirebaseHelper.writeToFirebase
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.LocalTime

@Composable
fun NewMeasurmentScreen(
    bluetoothManager: BluetoothManager, TempMode: Boolean, value: Float, angleRaw: Float,
    accelData: List<Entry>, tempData2: List<Entry>,
    timeStamp: Float,
    timeStampTemp:  Float,
    isRecording: MutableState<Boolean>,
    isScanning: MutableState<Boolean>
) {
    val context = LocalContext.current
    var isConnected by remember { mutableStateOf(bluetoothManager.isDeviceConnected()) }
    val filePath = context.filesDir
    var showDialog by remember { mutableStateOf(false) }
    var username by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        while (true) {
            isConnected = bluetoothManager.isDeviceConnected()
            delay(1000) // Adjust the delay as needed
        }
    }
    // Delete the files when the app starts (only if they exist)
    DisposableEffect(Unit) {
        val accelFile = File(filePath, "accel.txt")
        val gyroFile = File(filePath, "gyro.txt")
        val angleFile = File(filePath, "angle.txt")

        // Delete the files if they exist
        if (accelFile.exists()) accelFile.delete()
        if (gyroFile.exists()) gyroFile.delete()
        if (angleFile.exists()) angleFile.delete()

        onDispose { }
    }

    // Function to calculate elevation angle from x, y, z values

    fun saveDataToFirebaseAndDeleteFiles(context: Context, filePath: File, username: String) {
        // Define paths for both gyro and accel files
        val gyroFile = File(filePath, "gyro.txt")
        val accelFile = File(filePath, "accel.txt")
        val date = LocalDate.now().toString()
        val time = LocalTime.now().toString()
        // Check if either gyro or accel file exists
        if (!gyroFile.exists() && !accelFile.exists()) {
            Toast.makeText(context, "No data to save. Both gyro and accel files are missing.", Toast.LENGTH_SHORT).show()
            return
        }

        // Process Gyro File
        if (gyroFile.exists()) {
            try {
                // Read the content of the gyro file
                val gyroData = gyroFile.readText()

                // Get the current date and time


                // Prepare the data map for gyro
                val gyroDataMap = mapOf(
                    "timestamp" to "$date $time",
                    "content" to gyroData
                )

                // Save gyro data to Firestore
                writeToFirebase(
                    username = username,
                    date = date,
                    time = time,
                    type = "GYRO", // Specify the type as Gyro
                    data = gyroDataMap,
                    onSuccess = {
                        // Delete the gyro file after successful upload
                        gyroFile.delete()
                        Toast.makeText(context, "Gyro data saved to Firebase and file deleted.", Toast.LENGTH_SHORT).show()
                    },
                    onFailure = { e ->
                        Log.e("NewMeasurementScreen", "Error saving gyro data", e)
                        Toast.makeText(context, "Error saving gyro data. Please try again.", Toast.LENGTH_SHORT).show()
                    }
                )
            } catch (e: Exception) {
                Log.e("NewMeasurementScreen", "Error processing gyro file", e)
                Toast.makeText(context, "Error processing gyro file. Ensure data is correct.", Toast.LENGTH_SHORT).show()
            }
        }

        // Process Accel File
        if (accelFile.exists()) {
            try {
                // Read the content of the accel file
                val accelData = accelFile.readText()
                // Get the current date and time

                // Prepare the data map for accel
                val accelDataMap = mapOf(
                    "timestamp" to "$date $time",
                    "content" to accelData
                )

                // Save accel data to Firestore
                writeToFirebase(
                    username = username,
                    date = date,
                    time = time,
                    type = "ACCEL", // Specify the type as Accel
                    data = accelDataMap,
                    onSuccess = {
                        // Delete the accel file after successful upload
                        accelFile.delete()
                        Toast.makeText(context, "Accel data saved to Firebase and file deleted.", Toast.LENGTH_SHORT).show()
                    },
                    onFailure = { e ->
                        Log.e("NewMeasurementScreen", "Error saving accel data", e)
                        Toast.makeText(context, "Error saving accel data. Please try again.", Toast.LENGTH_SHORT).show()
                    }
                )
            } catch (e: Exception) {
                Log.e("NewMeasurementScreen", "Error processing accel file", e)
                Toast.makeText(context, "Error processing accel file. Ensure data is correct.", Toast.LENGTH_SHORT).show()
            }
        }
    }





    if (isConnected) {
        val rawData = if (TempMode) tempData2 else accelData
        GraphScreen(
            temperatureRaw = value,
            rawData = rawData,
            isRecording = isRecording.value,
            onSaveData = {
                showDialog = true // Trigger the dialog
            },
            toggleRecording = { isRecording.value = !isRecording.value },
            isTemp = TempMode,
            angleRaw = angleRaw
        )

        if (showDialog) {
            UsernameDialog(
                onDismiss = { showDialog = false },
                onConfirm = { inputUsername ->
                    username = inputUsername
                    showDialog = false
                    saveDataToFirebaseAndDeleteFiles(
                        context = context,
                        filePath = context.filesDir,
                        username = username
                    )
                }
            )
        }

    }else{
        ConnectionScreen(bluetoothManager = bluetoothManager,isScanning = isScanning)
    }

}
@Composable
fun UsernameDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var usernameInput by remember { mutableStateOf("") }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Enter Username") },
        text = {
            Column {
                BasicTextField(
                    value = usernameInput,
                    onValueChange = { usernameInput = it },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(usernameInput) }) {
                Text("Save")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}