package com.fitflow.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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

        setContent {
            FitFlowTheme {
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
                        innerPadding = innerPadding
                    )
                }
            }
        }
    }
}
