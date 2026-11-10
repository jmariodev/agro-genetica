package com.jmariodev.agrodigital

import android.app.Application
import com.jmariodev.agrodigital.di.ServiceLocator

class AgroApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ServiceLocator.init(this)
    }
}
