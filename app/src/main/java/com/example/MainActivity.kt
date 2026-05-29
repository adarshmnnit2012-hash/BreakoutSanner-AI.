package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.example.data.TradingRepository
import com.example.data.local.TradingDatabase
import com.example.ui.TradingViewModel
import com.example.ui.TradingViewModelFactory
import com.example.ui.screens.MainTradingScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Edge-to-edge layout enabled
        enableEdgeToEdge()

        // Initialize local persistence database
        val database = TradingDatabase.getDatabase(applicationContext)
        val repository = TradingRepository(database.tradingDao())
        
        // Instantiate ViewModel via constructor injection
        val viewModelFactory = TradingViewModelFactory(application, repository)
        val viewModel = ViewModelProvider(this, viewModelFactory)[TradingViewModel::class.java]

        setContent {
            MyApplicationTheme {
                MainTradingScreen(
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
