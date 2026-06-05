package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.data.AppDatabase
import com.example.data.AppRepository
import com.example.ui.AppViewModel
import com.example.ui.MainAppScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Force edge-to-edge layout styling
        enableEdgeToEdge()

        val database = AppDatabase.getDatabase(applicationContext)
        val repository = AppRepository(database.appDao(), applicationContext)
        
        @Suppress("UNCHECKED_CAST")
        val viewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AppViewModel(repository) as T
            }
        })[AppViewModel::class.java]

        setContent {
            MainAppScreen(viewModel)
        }
    }
}
