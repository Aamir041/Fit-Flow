package com.fitflow.app

import android.app.Application
import com.fitflow.app.di.AppContainer
import com.fitflow.app.di.DefaultAppContainer

class FitFlowApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }
}
