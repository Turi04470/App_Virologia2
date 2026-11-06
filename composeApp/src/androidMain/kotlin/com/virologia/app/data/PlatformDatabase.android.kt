package com.virologia.app.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

private class VirologySqliteHelper(context: Context) :
    SQLiteOpenHelper(context, "virologia_lab.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS viral_expedientes_store (
                store_id INTEGER PRIMARY KEY,
                json_payload TEXT NOT NULL,
                updated_at INTEGER NOT NULL
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS viral_expedientes_store")
        onCreate(db)
    }
}

actual object PlatformDatabase {
    private var sqliteHelper: VirologySqliteHelper? = null
    private var memoryFallback: String? = null

    fun init(context: Context) {
        if (sqliteHelper == null) {
            sqliteHelper = VirologySqliteHelper(context.applicationContext)
        }
    }

    actual fun loadRecordsJson(): String? {
        val helper = sqliteHelper ?: return memoryFallback
        return try {
            val db = helper.readableDatabase
            val cursor = db.rawQuery(
                "SELECT json_payload FROM viral_expedientes_store WHERE store_id = 1",
                null
            )
            cursor.use {
                if (it.moveToFirst()) it.getString(0) else null
            }
        } catch (_: Exception) {
            memoryFallback
        }
    }

    actual fun saveRecordsJson(jsonPayload: String) {
        memoryFallback = jsonPayload
        val helper = sqliteHelper ?: return
        try {
            val db = helper.writableDatabase
            val values = ContentValues().apply {
                put("store_id", 1)
                put("json_payload", jsonPayload)
                put("updated_at", System.currentTimeMillis())
            }
            db.insertWithOnConflict(
                "viral_expedientes_store",
                null,
                values,
                SQLiteDatabase.CONFLICT_REPLACE
            )
        } catch (_: Exception) {
            // Ignored fallback
        }
    }

    actual fun getDatabaseStatusLabel(): String {
        return "SQLite Nativo Android (virologia_lab.db)"
    }
}
