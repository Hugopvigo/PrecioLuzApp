package com.precioluz.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.background
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import com.precioluz.app.data.datastore.AppTheme
import com.precioluz.app.ui.components.AuroraBackground
import com.precioluz.app.ui.components.FloatingTabBar
import com.precioluz.app.ui.components.GlassCard
import com.precioluz.app.ui.components.SettingsDialog
import com.precioluz.app.ui.screen.TodayScreen
import com.precioluz.app.ui.screen.TomorrowScreen
import com.precioluz.app.ui.theme.BrandOrangeEnd
import com.precioluz.app.ui.theme.BrandOrangeStart
import com.precioluz.app.ui.theme.PrecioLuzTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import com.precioluz.app.ui.viewmodel.PrecioLuzViewModel

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val vm: PrecioLuzViewModel = hiltViewModel()
            val uiState by vm.uiState.collectAsStateWithLifecycle()
            val theme   by vm.theme.collectAsStateWithLifecycle()
            val apiKey  by vm.apiKey.collectAsStateWithLifecycle()

            PrecioLuzTheme(appTheme = theme) {
                PrecioLuzApp(
                    uiState      = uiState,
                    appTheme     = theme,
                    apiKey       = apiKey,
                    onCycleTheme = vm::cycleTheme,
                    onRefresh    = vm::refresh,
                    onSaveApiKey = vm::saveApiKey,
                    onClearApiKey = vm::clearApiKey,
                )
            }
        }
    }
}

@Composable
fun PrecioLuzApp(
    uiState: com.precioluz.app.ui.viewmodel.PrecioLuzUiState,
    appTheme: AppTheme,
    apiKey: String?,
    onCycleTheme: () -> Unit,
    onRefresh: () -> Unit,
    onSaveApiKey: (String) -> Unit,
    onClearApiKey: () -> Unit,
) {
    val isDark = when (appTheme) {
        AppTheme.AUTO  -> isSystemInDarkTheme()
        AppTheme.LIGHT -> false
        AppTheme.DARK  -> true
    }
    var selectedTab by remember { mutableIntStateOf(0) }
    var showSettings by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        AuroraBackground(isDark = isDark, modifier = Modifier.matchParentSize())

        if (uiState.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                AppHeader(
                    appTheme      = appTheme,
                    onCycleTheme  = onCycleTheme,
                    onOpenSettings = { showSettings = true },
                    isDark        = isDark,
                    modifier      = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                )

                if (uiState.noApiKey) {
                    GlassCard(
                        isDark = isDark,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                    ) {
                        Text(
                            text = "Configura tu API Key de REE en Ajustes para ver los precios",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        )
                    }
                }

                uiState.error?.let { error ->
                    if (!uiState.noApiKey) {
                        GlassCard(
                            isDark = isDark,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                        ) {
                            Text(
                                text = error,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f)) {
                    when (selectedTab) {
                        0 -> uiState.today?.let { TodayScreen(it, isDark) }
                        1 -> TomorrowScreen(uiState.tomorrow, isDark)
                    }
                }
            }
        }

        FloatingTabBar(
            selectedTab   = selectedTab,
            onTabSelected = { selectedTab = it },
            isDark        = isDark,
            modifier      = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 24.dp),
        )
    }

    if (showSettings) {
        SettingsDialog(
            currentApiKey = apiKey,
            isDark        = isDark,
            onDismiss     = { showSettings = false },
            onSave        = { onSaveApiKey(it); showSettings = false },
            onClear       = { onClearApiKey(); showSettings = false },
        )
    }
}

@Composable
fun AppHeader(
    appTheme: AppTheme,
    onCycleTheme: () -> Unit,
    onOpenSettings: () -> Unit,
    isDark: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        // Brand mark — caja gradiente naranja con bolt (equivale a .brand-mark del HTML)
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(BrandOrangeStart, BrandOrangeEnd),
                        start  = Offset(0f, 0f),
                        end    = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
                    )
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector        = Icons.Rounded.Bolt,
                contentDescription = null,
                tint               = androidx.compose.ui.graphics.Color.White,
                modifier           = Modifier.size(20.dp),
            )
        }
        Spacer(Modifier.width(11.dp))
        Column {
            Text(
                text  = "PrecioLuz",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text  = "Precio de la luz · PVPC",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = .44f),
            )
        }
        Spacer(Modifier.weight(1f))

        GlassCard(isDark = isDark) {
            IconButton(onClick = onCycleTheme, modifier = Modifier.size(40.dp)) {
                Icon(
                    imageVector = when (appTheme) {
                        AppTheme.AUTO  -> Icons.Rounded.AutoAwesome
                        AppTheme.LIGHT -> Icons.Rounded.LightMode
                        AppTheme.DARK  -> Icons.Rounded.NightsStay
                    },
                    contentDescription = "Cambiar tema",
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
        }

        Spacer(Modifier.width(8.dp))

        GlassCard(isDark = isDark) {
            IconButton(onClick = onOpenSettings, modifier = Modifier.size(40.dp)) {
                Icon(
                    imageVector = Icons.Rounded.Settings,
                    contentDescription = "Ajustes",
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}
