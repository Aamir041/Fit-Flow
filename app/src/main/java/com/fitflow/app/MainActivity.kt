package com.fitflow.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.rememberNavController
import com.fitflow.app.ui.components.FitFlowBottomNav
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
            val accentColor by themePreferences.accentColor.collectAsState()

            FitFlowTheme(
                themeMode = themeMode,
                accentColor = accentColor
            ) {
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
                        themePreferences = themePreferences,
                        innerPadding = innerPadding
                    )
                }
            }
        }
    }
}
