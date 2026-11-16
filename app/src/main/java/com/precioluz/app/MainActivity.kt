package com.precioluz.app

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import com.precioluz.app.data.datastore.AppTheme
import com.precioluz.app.ui.components.AuroraBackground
import com.precioluz.app.ui.components.FloatingTabBar
import com.precioluz.app.ui.components.GlassCard
import com.precioluz.app.ui.screen.TodayScreen
import com.precioluz.app.ui.screen.TomorrowScreen
import com.precioluz.app.ui.settings.SettingsSheet
import com.precioluz.app.ui.settings.SettingsViewModel
import com.precioluz.app.ui.theme.BrandOrangeEnd
import com.precioluz.app.ui.theme.BrandOrangeStart
import com.precioluz.app.ui.theme.PrecioLuzTheme
import com.precioluz.app.ui.viewmodel.PrecioLuzUiState
import com.precioluz.app.ui.viewmodel.PrecioLuzViewModel
import androidx.activity.compose.rememberLauncherForActivityResult

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val vm: PrecioLuzViewModel = hiltViewModel()
            val settingsVm: SettingsViewModel = hiltViewModel()
            val uiState by vm.uiState.collectAsStateWithLifecycle()
            val theme by vm.theme.collectAsStateWithLifecycle()

            PrecioLuzTheme(appTheme = theme) {
                PrecioLuzApp(
                    uiState       = uiState,
                    appTheme      = theme,
                    onCycleTheme  = vm::cycleTheme,
                    onRefresh     = vm::refresh,
                    settingsVm    = settingsVm,
                )
            }
        }
    }
}

@Composable
fun PrecioLuzApp(
    uiState: PrecioLuzUiState,
    appTheme: AppTheme,
    onCycleTheme: () -> Unit,
    onRefresh: () -> Unit,
    settingsVm: SettingsViewModel,
) {
    val isDark = when (appTheme) {
        AppTheme.AUTO  -> isSystemInDarkTheme()
        AppTheme.LIGHT -> false
        AppTheme.DARK  -> true
    }
    var selectedTab by remember { mutableIntStateOf(0) }
    var showSettings by remember { mutableStateOf(false) }
    val settingsState by settingsVm.uiState.collectAsStateWithLifecycle()

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { /* si se deniega, los ajustes siguen guardados y se puede reintentar */ }

    LaunchedEffect(Unit) { settingsVm.sync() }

    Box(modifier = Modifier.fillMaxSize()) {
        AuroraBackground(isDark = isDark, modifier = Modifier.fillMaxSize())

        if (uiState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = BrandOrangeEnd,
            )
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                AppHeader(
                    appTheme       = appTheme,
                    onCycleTheme   = onCycleTheme,
                    onOpenSettings = { showSettings = true },
                    isDark         = isDark,
                    modifier       = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                )

                uiState.error?.let { error ->
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
                .padding(bottom = 20.dp),
        )
    }

    if (showSettings) {
        SettingsSheet(
            uiState               = settingsState,
            isDark                = isDark,
            onDismiss             = { showSettings = false },
            onNotifyTomorrowChanged = settingsVm::setNotifyTomorrow,
            onNotifyDailyChanged    = settingsVm::setNotifyDaily,
            onEnableNotifications = {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            },
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
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(BrandOrangeStart, BrandOrangeEnd),
                        start  = Offset(0f, 0f),
                        end    = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
                    )
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Rounded.Bolt, contentDescription = null, tint = Color.White, modifier = Modifier.size(19.dp))
        }
        Spacer(Modifier.width(10.dp))
        Column {
            Text("PrecioLuz", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
            Text(
                "Precio de la luz · PVPC",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = .60f),
            )
        }
        Spacer(Modifier.weight(1f))

        val buttonBg = if (isDark) Color(0xFF151C29) else Color(0xFFF7F9FC)
        IconButton(
            onClick = onCycleTheme,
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(buttonBg),
        ) {
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
        Spacer(Modifier.width(6.dp))
        IconButton(
            onClick = onOpenSettings,
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(buttonBg),
        ) {
            Icon(Icons.Rounded.Settings, contentDescription = "Ajustes", tint = MaterialTheme.colorScheme.onSurface)
        }
    }
}
