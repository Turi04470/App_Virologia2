package com.virologia.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.virologia.app.data.PlatformDatabase

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        PlatformDatabase.init(applicationContext)
        ViralRepositoryReloadHelper.ensureLoaded()
        setContent {
            App(isMobile = true)
        }
    }
}

private object ViralRepositoryReloadHelper {
    fun ensureLoaded() {
        com.virologia.app.data.ViralRepository.loadOrSeed()
    }
}
