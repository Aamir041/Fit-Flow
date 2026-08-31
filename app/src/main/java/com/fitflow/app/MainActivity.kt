package com.fitflow.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.fitflow.app.ui.components.FitFlowBottomNav
import com.fitflow.app.ui.components.ThemeSelectionDialog
import com.fitflow.app.ui.navigation.FitFlowNavGraph
import com.fitflow.app.ui.theme.FitFlowTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val appContainer = (application as FitFlowApplication).container
        val repository = appContainer.repository
        val themePreferences = appContainer.themePreferences

        setContent {
            val themeMode by themePreferences.themeMode.collectAsState()
            var showThemeDialog by remember { mutableStateOf(false) }

            FitFlowTheme(themeMode = themeMode) {
                val navController = rememberNavController()

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        FitFlowBottomNav(navController = navController)
                    }
                ) { innerPadding ->
                    FitFlowNavGraph(
                        navController = navController,
                        repository = repository,
                        innerPadding = innerPadding,
                        onOpenThemeDialog = { showThemeDialog = true }
                    )
                }

                if (showThemeDialog) {
                    ThemeSelectionDialog(
                        currentTheme = themeMode,
                        onSelectTheme = { selectedMode ->
                            themePreferences.setThemeMode(selectedMode)
                        },
                        onDismiss = { showThemeDialog = false }
                    )
                }
            }
        }
    }
}
