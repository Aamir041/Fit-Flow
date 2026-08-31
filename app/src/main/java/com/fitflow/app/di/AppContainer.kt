package com.fitflow.app.di

import android.content.Context
import com.fitflow.app.data.local.FitFlowDatabase
import com.fitflow.app.data.repository.FitFlowRepository
import com.fitflow.app.data.repository.FitFlowRepositoryImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

interface AppContainer {
    val repository: FitFlowRepository
    val themePreferences: com.fitflow.app.data.local.ThemePreferences
    val applicationScope: CoroutineScope
}

class DefaultAppContainer(private val context: Context) : AppContainer {

    override val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val database: FitFlowDatabase by lazy {
        FitFlowDatabase.getDatabase(context, applicationScope)
    }

    override val repository: FitFlowRepository by lazy {
        FitFlowRepositoryImpl(database)
    }

    override val themePreferences: com.fitflow.app.data.local.ThemePreferences by lazy {
        com.fitflow.app.data.local.ThemePreferences(context)
    }

    init {
        applicationScope.launch {
            repository.ensureSeeded()
        }
    }
}
