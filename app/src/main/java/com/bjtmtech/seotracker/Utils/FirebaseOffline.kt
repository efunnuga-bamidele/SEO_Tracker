package com.bjtmtech.seotracker.Utils

import android.app.Application
import com.google.firebase.database.FirebaseDatabase

class FirebaseOffline : Application() {

    override fun onCreate() {
        super.onCreate()
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
    }
}