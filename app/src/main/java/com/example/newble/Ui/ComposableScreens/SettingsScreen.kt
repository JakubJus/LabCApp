package com.example.newble.Ui.ComposableScreens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
@Composable
fun SettingsScreen(
    onCustomToggle: (Boolean) -> Unit = {},
    onTempToggle: (Boolean) -> Unit = {},
    onDarkModeToggle: (Boolean) -> Unit = {}, // Callback for dark mode toggle
    onThresholdUpdate: (Float) -> Unit = {},  // Callback for threshold update
    onColorsChange: (Color, Color, Color, Color, Color, Color, Color, Color) -> Unit = { _, _, _, _, _, _, _, _ -> }// Callback for color updates
) {
    var isDarkMode by remember { mutableStateOf(false) }
    var isCustom by remember { mutableStateOf(false) }// Track dark mode state
    var isTemp by remember { mutableStateOf(false) }// Track dark mode state

    var threshold by remember { mutableStateOf(50f) }   // Track threshold value
    val context = LocalContext.current

    // Colors for the color picker
    val availableColors = listOf(
        Color(0xFFBB86FC), Color(0xFF6200EE), Color(0xFF03DAC6),
        Color(0xFFFF5722), Color(0xFFFFC107), Color(0xFF4CAF50),
        Color(0xFF2196F3), Color(0xFFF44336), Color(0xFF9C27B0)
    )
    var selectedColor by remember { mutableStateOf(availableColors.first()) }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(16.dp)
        ) {
            // Title
            Text(
                text = "Settings",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Dark Mode Toggle with custom styling
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text("Custom Settings", style = MaterialTheme.typography.bodyLarge)
                Switch(
                    checked = isDarkMode,
                    onCheckedChange = { isChecked ->
                        isDarkMode = isChecked
                        onDarkModeToggle(isChecked)
                        Toast.makeText(
                            context,
                            if (isChecked) "Custom enabled" else "Custom disabled",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text("Dark Mode", style = MaterialTheme.typography.bodyLarge)
                Switch(
                    checked = isCustom,
                    onCheckedChange = { isChecked ->
                        isCustom = isChecked
                        onCustomToggle(isChecked)
                        Toast.makeText(
                            context,
                            if (isChecked) "Dark mode enabled" else "Dark mode disabled",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text("Temperature Measurment", style = MaterialTheme.typography.bodyLarge)
                Switch(
                    checked = isTemp, // Correct state variable
                    onCheckedChange = { isChecked -> // Rename to avoid confusion
                        isTemp = isChecked // Update the correct state
                        onTempToggle(isChecked) // Correct callback name
                        Toast.makeText(
                            context,
                            if (isChecked) "Measuring Temperature" else "Measuring Angle",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Threshold Slider with custom styling
            Text("Threshold: ${threshold.toInt()}")

            Slider(
                value = threshold,
                onValueChange = { newValue -> threshold = newValue },
                onValueChangeFinished = {
                    onThresholdUpdate(threshold)
                    Toast.makeText(
                        context,
                        "Threshold updated to ${threshold.toInt()}",
                        Toast.LENGTH_SHORT
                    ).show()
                },
                valueRange = 0f..100f,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Color Picker Section with a grid layout for color swatches
            Text("Choose Primary Color:", style = MaterialTheme.typography.bodyLarge)

            ColorPicker(
                colors = availableColors,
                selectedColor = selectedColor,
                onColorSelected = { color ->
                    selectedColor = color
                    onColorsChange(
                        color, // primary
                        color, // secondary (For simplicity, using the same color for secondary)
                        Color.White, // background
                        Color.White, // surface
                        Color.Black, // onPrimary
                        Color.Black, // onSecondary
                        Color.Black, // onBackground
                        Color.Black  // onSurface
                    )
                    Toast.makeText(context, "Color updated!", Toast.LENGTH_SHORT).show()
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Save Button with custom styling
            Button(
                onClick = {
                    // Simulate saving settings
                    Toast.makeText(context, "Settings saved", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
            ) {
                Text("Save Settings", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}



@Composable
fun ColorPicker(
    colors: List<Color>,
    selectedColor: Color,
    onColorSelected: (Color) -> Unit
) {
    // Improved color picker with a grid layout
    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(8.dp)
    ) {
        items(colors) { color ->
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .padding(4.dp)
                    .background(color, shape = CircleShape)
                    .border(
                        width = if (color == selectedColor) 4.dp else 2.dp,
                        color = if (color == selectedColor) Color.Black else Color.Gray,
                        shape = CircleShape
                    )
                    .clickable {
                        onColorSelected(color)
                    }
            )
        }
    }
}
