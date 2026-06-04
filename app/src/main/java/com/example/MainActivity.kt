package com.example

import android.os.Bundle
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.example.data.database.AppDatabase
import com.example.data.repository.TimeKeeperRepository
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.WelcomeScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.TimeKeeperViewModel
import com.example.ui.viewmodel.TimeKeeperViewModelFactory

class MainActivity : ComponentActivity() {

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        // Handle permissions results gracefully if needed
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Automatically request critical storage & notifications permissions on start to support active features
        val permissionsToRequest = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
            permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if (permissionsToRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionsToRequest.toTypedArray())
        }

        // Initialize Local Persistence Layer
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = TimeKeeperRepository(database.timeKeeperDao())
        
        // Instantiate ViewModel
        val factory = TimeKeeperViewModelFactory(application, repository)
        val viewModel = ViewModelProvider(this, factory)[TimeKeeperViewModel::class.java]

        setContent {
            val isDarkTheme by viewModel.settingsDarkMode.collectAsState()
            
            MyApplicationTheme(darkTheme = isDarkTheme) {
                val currentScreen by viewModel.currentScreen.collectAsState()

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        when (currentScreen) {
                            "welcome" -> WelcomeScreen(
                                viewModel = viewModel
                            )
                            else -> DashboardScreen(
                                viewModel = viewModel
                            )
                        }
                    }
                }
            }
        }
    }
}
