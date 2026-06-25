package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.data.BoxingDatabase
import com.example.data.BoxingRepository
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.timer.TimerScreen
import com.example.ui.timer.TimerViewModel

class MainActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Setup edge-to-edge immersive viewports
        enableEdgeToEdge()
        
        // Instantiate the database & repository
        val database = BoxingDatabase.getDatabase(applicationContext, lifecycleScope)
        val repository = BoxingRepository(database.boxingDao())
        
        // Retrieve our timer state machine via standard Factory provider
        val viewModel: TimerViewModel by viewModels {
            TimerViewModel.Factory(application, repository)
        }
        
        setContent {
            MyApplicationTheme(
                darkTheme = true, // We lock to an eye-safe Dark Theme perfect for active gyms
                dynamicColor = false // Keep our custom high-intensity red/amber/emerald branding
            ) {
                TimerScreen(viewModel = viewModel)
            }
        }
    }
}
