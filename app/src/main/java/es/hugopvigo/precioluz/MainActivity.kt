package es.hugopvigo.precioluz

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import es.hugopvigo.precioluz.data.datastore.AppTheme
import es.hugopvigo.precioluz.ui.components.AuroraBackground
import es.hugopvigo.precioluz.ui.components.FloatingTabBar
import es.hugopvigo.precioluz.ui.components.GlassCard
import es.hugopvigo.precioluz.ui.screen.TodayScreen
import es.hugopvigo.precioluz.ui.screen.TomorrowScreen
import es.hugopvigo.precioluz.ui.theme.BrandOrangeEnd
import es.hugopvigo.precioluz.ui.theme.BrandOrangeStart
import es.hugopvigo.precioluz.ui.theme.PrecioLuzTheme
import es.hugopvigo.precioluz.ui.viewmodel.PrecioLuzViewModel

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

            PrecioLuzTheme(appTheme = theme) {
                PrecioLuzApp(
                    uiState     = uiState,
                    appTheme    = theme,
                    onCycleTheme = vm::cycleTheme,
                    onRefresh    = vm::refresh,
                )
            }
        }
    }
}

@Composable
fun PrecioLuzApp(
    uiState: es.hugopvigo.precioluz.ui.viewmodel.PrecioLuzUiState,
    appTheme: AppTheme,
    onCycleTheme: () -> Unit,
    onRefresh: () -> Unit,
) {
    val isDark = when (appTheme) {
        AppTheme.AUTO  -> isSystemInDarkTheme()
        AppTheme.LIGHT -> false
        AppTheme.DARK  -> true
    }
    var selectedTab by remember { mutableIntStateOf(0) }

    Box(modifier = Modifier.fillMaxSize()) {
        // Aurora de fondo
        AuroraBackground(isDark = isDark, modifier = Modifier.matchParentSize())

        // Contenido principal
        if (uiState.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                // AppBar
                AppHeader(
                    appTheme     = appTheme,
                    onCycleTheme = onCycleTheme,
                    isDark       = isDark,
                    modifier     = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                )

                // Pantalla activa
                Box(modifier = Modifier.weight(1f)) {
                    when (selectedTab) {
                        0 -> uiState.today?.let { TodayScreen(it, isDark) }
                        1 -> TomorrowScreen(uiState.tomorrow, isDark)
                    }
                }
            }
        }

        // Tab bar flotante anclada al fondo
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
}

@Composable
fun AppHeader(
    appTheme: AppTheme,
    onCycleTheme: () -> Unit,
    isDark: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        // Logo + nombre
        Box(
            modifier        = Modifier
                .size(38.dp)
                .padding(end = 4.dp),
            contentAlignment = Alignment.Center,
        ) {
            // TODO: icono rayo con gradiente naranja (BrandOrangeStart → BrandOrangeEnd)
            Icon(Icons.Rounded.AutoAwesome, contentDescription = null,
                 tint = BrandOrangeEnd)
        }
        Spacer(Modifier.width(7.dp))
        Column {
            Text("PrecioLuz", style = MaterialTheme.typography.titleLarge)
            Text("Precio de la luz · PVPC",
                 style = MaterialTheme.typography.labelMedium,
                 color = MaterialTheme.colorScheme.onSurface.copy(alpha = .44f))
        }
        Spacer(Modifier.weight(1f))

        // Botón tema
        GlassCard(isDark = isDark) {
            IconButton(onClick = onCycleTheme, modifier = Modifier.size(40.dp)) {
                Icon(
                    imageVector = when (appTheme) {
                        AppTheme.AUTO  -> Icons.Rounded.AutoAwesome
                        AppTheme.LIGHT -> Icons.Rounded.LightMode
                        AppTheme.DARK  -> Icons.Rounded.NightsStay
                    },
                    contentDescription = "Cambiar tema",
                )
            }
        }
    }
}
