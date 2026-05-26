package com.beszel.android

import android.app.Application
import com.beszel.android.di.AppContainer

class BeszelApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(applicationContext)
    }
}
