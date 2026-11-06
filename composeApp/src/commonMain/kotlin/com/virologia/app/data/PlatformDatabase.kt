package com.virologia.app.data

expect object PlatformDatabase {
    fun loadRecordsJson(): String?
    fun saveRecordsJson(jsonPayload: String)
    fun getDatabaseStatusLabel(): String
}
