package com.virologia.app.data

import kotlinx.browser.window

actual object PlatformDatabase {
    private const val STORAGE_KEY = "virologia_sqlite_web_store_v1"
    private var memoryFallback: String? = null

    actual fun loadRecordsJson(): String? {
        return try {
            window.localStorage.getItem(STORAGE_KEY) ?: memoryFallback
        } catch (_: Exception) {
            memoryFallback
        }
    }

    actual fun saveRecordsJson(jsonPayload: String) {
        memoryFallback = jsonPayload
        try {
            window.localStorage.setItem(STORAGE_KEY, jsonPayload)
        } catch (_: Exception) {
            // Fallback in memory
        }
    }

    actual fun getDatabaseStatusLabel(): String {
        return "Persistencia Local Web (WebStorage / Wasm DB)"
    }
}
