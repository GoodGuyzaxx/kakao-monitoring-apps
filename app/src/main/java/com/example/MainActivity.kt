package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.AppThemeMode
import com.example.ui.CacaoViewModel
import com.example.ui.components.CacaoDashboardScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  private val viewModel: CacaoViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val uiState by viewModel.uiState.collectAsStateWithLifecycle()
      val systemInDark = isSystemInDarkTheme()
      val isDarkTheme = when (uiState.themeMode) {
        AppThemeMode.SYSTEM -> systemInDark
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
      }

      MyApplicationTheme(darkTheme = isDarkTheme) {
        CacaoDashboardScreen(
          uiState = uiState,
          isDarkTheme = isDarkTheme,
          onToggleTheme = { viewModel.toggleThemeMode() },
          onThemeModeSelect = { mode -> viewModel.setThemeMode(mode) },
          onModeChange = { newMode -> viewModel.setControlMode(newMode) },
          onManualPumpToggle = { turnOn -> viewModel.setManualPump(turnOn) },
          onRefresh = { viewModel.refreshNow() },
          onSaveSettings = { newSettings -> viewModel.saveSettings(newSettings) },
          onDismissNotice = { viewModel.dismissNotice() },
          onSimulateMoistureChange = { delta -> viewModel.simulateMoistureChange(delta) },
          onSimulateDryTrigger = { viewModel.simulateDryTrigger() },
          modifier = Modifier.fillMaxSize(),
        )
      }
    }
  }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
  Text(text = "Monitoring Kakao $name", modifier = modifier)
}
