package com.precioluz.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.precioluz.app.ui.home.HomeScreen
import com.precioluz.app.ui.home.HomeViewModel
import com.precioluz.app.ui.settings.SettingsScreen
import com.precioluz.app.ui.settings.SettingsViewModel
import com.precioluz.app.ui.theme.PrecioLuzTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PrecioLuzTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    PrecioLuzNavHost()
                }
            }
        }
    }
}

@androidx.compose.runtime.Composable
private fun PrecioLuzNavHost() {
    val navController = androidx.navigation.compose.rememberNavController()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            val viewModel: HomeViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsState()

            HomeScreen(
                uiState = uiState,
                onTabSelected = viewModel::selectTab,
                onHourSelected = viewModel::selectHour,
                onRefresh = viewModel::loadPrices,
                onSettingsClick = { navController.navigate("settings") }
            )
        }
        composable("settings") {
            val viewModel: SettingsViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsState()

            SettingsScreen(
                uiState = uiState,
                onBack = { navController.popBackStack() },
                onNotifyTomorrowChanged = viewModel::setNotifyTomorrow,
                onNotifyDailyChanged = viewModel::setNotifyDaily,
                onDarkThemeChanged = viewModel::setDarkTheme
            )
        }
    }
}
