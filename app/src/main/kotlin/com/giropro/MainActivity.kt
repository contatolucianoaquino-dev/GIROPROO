package com.giropro

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.giropro.core.ui.theme.GiroProTheme
import com.giropro.feature.calculator.AnalyzeScreen
import com.giropro.feature.config.ConfigScreen
import com.giropro.feature.dashboard.DashboardScreen
import com.giropro.feature.history.HistoryScreen
import com.giropro.feature.overlay.OverlayService
import com.giropro.feature.vehicle.VehicleScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GiroProTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppRoot()
                }
            }
        }
    }
}

@androidx.compose.runtime.Composable
private fun AppRoot() {
    val nav = rememberNavController()
    val context = LocalContext.current
    var monitorando by remember { mutableStateOf(false) }

    fun pedirPermissaoOverlay() {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:" + context.packageName),
        )
        context.startActivity(intent)
    }

    fun iniciarServico() {
        val i = Intent(context, OverlayService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) context.startForegroundService(i)
        else context.startService(i)
    }

    fun toggleMonitor() {
        if (monitorando) {
            context.stopService(Intent(context, OverlayService::class.java))
            monitorando = false
        } else {
            if (!OverlayService.temPermissao(context)) {
                pedirPermissaoOverlay()
                return // volta e tenta de novo após conceder
            }
            iniciarServico()
            monitorando = true
        }
    }

    Scaffold { padding ->
        NavHost(
            navController = nav,
            startDestination = "dashboard",
            modifier = Modifier.padding(padding),
        ) {
            composable("dashboard") {
                DashboardScreen(
                    monitorando = monitorando,
                    onToggleMonitor = { toggleMonitor() },
                    onNavigate = { rota -> nav.navigate(rota) },
                )
            }
            composable("analisar") {
                AnalyzeScreen(onMostrarNaJanela = { analise, plataforma ->
                    if (OverlayService.temPermissao(context)) {
                        val i = OverlayService.intentComAnalise(context, analise, plataforma)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) context.startForegroundService(i)
                        else context.startService(i)
                        monitorando = true
                    } else {
                        pedirPermissaoOverlay()
                    }
                })
            }
            composable("veiculo") { VehicleScreen() }
            composable("config") { ConfigScreen() }
            composable("historico") { HistoryScreen() }
        }
    }
}
