package com.example.newble

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import com.example.newble.Ui.ComposableScreens.*
import com.example.newble.bluetooth.BluetoothManager
import com.example.newble.Ui.theme.NewbleTheme
import com.google.firebase.FirebaseApp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.newble.Ui.ComposableScreens.NewMeasurmentScreen
import com.example.newble.bluetooth.IMUDataParser
import com.github.mikephil.charting.data.Entry
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class MainActivity : ComponentActivity() {

    private lateinit var bluetoothManager: BluetoothManager

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 1001
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Firebase explicitly
        FirebaseApp.initializeApp(this)

        // Initialize BluetoothManager
        bluetoothManager = BluetoothManager(this)

        // Permissions Launcher
        val permissionsLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val allGranted = permissions.values.all { it }
            if (allGranted) {
                setContent { MainScreen(bluetoothManager) }
            } else {
                setContent { PermissionDeniedScreen() }
            }
        }

        // List of required permissions
        val requiredPermissions = mutableListOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT
        )

        // Add notification permission if needed for Android Tiramisu or above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requiredPermissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        // Check if permissions are granted
        val hasAllPermissions = requiredPermissions.all {
            checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED
        }

        // If permissions not granted, request them
        if (!hasAllPermissions) {
            permissionsLauncher.launch(requiredPermissions.toTypedArray())
        } else {
            setContent { MainScreen(bluetoothManager) }
        }
    }
}

@Composable
fun MainScreen(bluetoothManager: BluetoothManager) {
    // Track the selected colors
    var primaryColor by remember { mutableStateOf<Color>(Color(0xFF93EE00)) } // Default color
    var secondaryColor by remember { mutableStateOf<Color>(Color(0xFF03DAC6)) }
    var backgroundColor by remember { mutableStateOf<Color>(Color.White) }
    var surfaceColor by remember { mutableStateOf<Color>(Color.White) }
    var onPrimaryColor by remember { mutableStateOf<Color>(Color.White) }
    var onSecondaryColor by remember { mutableStateOf<Color>(Color.Black) }
    var onBackgroundColor by remember { mutableStateOf<Color>(Color.Black) }
    var onSurfaceColor by remember { mutableStateOf<Color>(Color.Black) }
    var isDarkMode by remember { mutableStateOf(false) } // Track dark mode state
    var Custom by remember { mutableStateOf(false) }
    var TempMode by rememberSaveable { mutableStateOf(false) }
    var currentScreen by remember { mutableStateOf("Home") }
    val context = LocalContext.current

    var value by remember { mutableStateOf(0f) }
    var angleRaw by remember { mutableStateOf(0f) }
    val accelData = remember { mutableStateListOf<Entry>() }
    val tempData2 = remember { mutableStateListOf<Entry>() }
    var timeStamp by remember { mutableStateOf(0f) }
    var timeStampTemp by remember { mutableStateOf(0f) }
    val isRecording = remember { mutableStateOf(false) }
    val isScanning = remember { mutableStateOf(false) }

    fun calculateElevationAngle(x: Float, y: Float, z: Float): Float {
        val magnitude = Math.sqrt((x * x + y * y + z * z).toDouble())
        return if (magnitude > 0) {
            Math.acos(z / magnitude).toFloat() * (180 / Math.PI).toFloat()
        } else {
            0f
        }
    }
    // Write to file (accel.txt, gyro.txt, angle.txt)
    fun writeToFile(nameOfFile: String, data: String) {
        try {
            val file = File(context.filesDir, "${nameOfFile}.txt")
            val fileOutputStream = FileOutputStream(file, true)
            fileOutputStream.write((data + "\n").toByteArray())
            fileOutputStream.close()
        } catch (e: IOException) {
            Log.e("MainScreen", "Error writing to file", e)
            Toast.makeText(context, "Error saving data", Toast.LENGTH_SHORT).show()
        }
    }

    //var isConnected by remember { mutableStateOf(bluetoothManager.isDeviceConnected()) }

    // Monitor Bluetooth connection status
//    LaunchedEffect(Unit) {
//        while (true) {
//            isConnected = bluetoothManager.isDeviceConnected()
//            delay(1000) // Adjust the delay as needed
//        }
//    }
    // DisposableEffect for Bluetooth scanning
    DisposableEffect(isScanning.value) {
        if(isScanning.value) {
            bluetoothManager.startScan { data ->
                IMUDataParser.parseData(data.toByteArray())?.let { parsedData ->
                    when (parsedData.first) {
                        "A" -> {
                            val values =
                                parsedData.second.split(",").mapNotNull { it.toFloatOrNull() }
                            if (values.size == 3) {
                                val (x, y, z) = values
                                if (isRecording.value) {
                                    val elevationAngle = calculateElevationAngle(x, y, z)
                                    accelData.add(Entry(timeStamp, elevationAngle))
                                    angleRaw=elevationAngle

                                    // Write to files during recording
                                    writeToFile("accel", "$values")
                                    writeToFile("angle", "$elevationAngle")

                                    timeStamp += 1f
                                }
                            }
                        }

                        "G" -> {
                            val values =
                                parsedData.second.split(",").mapNotNull { it.toFloatOrNull() }
                            if (isRecording.value && values.size == 3) {
                                val (x, y, z) = values
                                writeToFile("gyro", "gyro,$x,$y,$z,")
                            }
                        }

                        "M" -> {
                            val values =
                                parsedData.second.split(",").mapNotNull { it.toFloatOrNull() }
                            if (isRecording.value && values.size == 3) {
                                val (MLValue, y, z) = values
                                value=MLValue
                                tempData2.add(Entry(timeStampTemp, MLValue))
                                timeStampTemp+=1f

                            }
                        }
                    }
                }
            }

        }
        onDispose { bluetoothManager.stopScan() }

    }
    // Apply the theme with the selected colors and dark mode
    NewbleTheme(
        primaryColor = primaryColor,
        secondaryColor = secondaryColor,
        backgroundColor = backgroundColor,
        surfaceColor = surfaceColor,
        onPrimaryColor = onPrimaryColor,
        onSecondaryColor = onSecondaryColor,
        onBackgroundColor = onBackgroundColor,
        onSurfaceColor = onSurfaceColor,
        isDarkTheme = isDarkMode,
        isCustom = Custom
    ) {
        Scaffold(
            bottomBar = {
                BottomNavigationBar(currentScreen) { selectedScreen ->
                    currentScreen = selectedScreen
                }
            }
        ) { paddingValues ->
            Surface(modifier = Modifier.padding(paddingValues)) {
                when (currentScreen) {
                    "Home" -> HomeScreen()
                    "Settings" -> {
                        SettingsScreen(
                            onTempToggle = { isTemp ->
                                TempMode = isTemp // Update the state when toggled
                            },
                            onCustomToggle = { darkMode ->
                                isDarkMode = darkMode // Update dark mode state
                            },
                            onDarkModeToggle = { iCustom ->
                                Custom = iCustom // Update dark mode state
                            },
                            onThresholdUpdate = { threshold -> /* Handle threshold update */ },
                            onColorsChange = { primary, secondary, background, surface, onPrimary, onSecondary, onBackground, onSurface ->
                                // Update colors from Settings screen
                                primaryColor = primary
                                secondaryColor = secondary
                                backgroundColor = background
                                surfaceColor = surface
                                onPrimaryColor = onPrimary
                                onSecondaryColor = onSecondary
                                onBackgroundColor = onBackground
                                onSurfaceColor = onSurface
                            }
                        )
                    }
                    "Overview" -> OverviewScreen()
                    "NewMeasurment" -> {
                        //if (!isConnected) {
                        NewMeasurmentScreen(bluetoothManager,TempMode,
                            value, angleRaw, accelData,tempData2 ,
                            timeStamp , timeStampTemp , isRecording,isScanning)
                        //} else {
                        //    ConnectionScreen(bluetoothManager)
                       // }
                    }
                }
            }
        }
    }
}



@Composable
fun BottomNavigationBar(currentScreen: String, onScreenSelected: (String) -> Unit) {
    NavigationBar {
        NavigationBarItem(
            selected = currentScreen == "Home",
            onClick = { onScreenSelected("Home") },
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home") }
        )
        NavigationBarItem(
            selected = currentScreen == "Settings",
            onClick = { onScreenSelected("Settings") },
            icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
            label = { Text("Settings") }
        )
        NavigationBarItem(
            selected = currentScreen == "Overview",
            onClick = { onScreenSelected("Overview") },
            icon = { Icon(Icons.Default.Info, contentDescription = "Overview") },
            label = { Text("Overview") }
        )
        NavigationBarItem(
            selected = currentScreen == "NewMeasurment",
            onClick = { onScreenSelected("NewMeasurment") },
            icon = { Icon(Icons.Default.PlayArrow, contentDescription = "New Measurement") },
            label = { Text("Measure") }
        )
    }
}
