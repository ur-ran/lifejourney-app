package com.example.lifejourney.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Note::class, NoteImage::class], version = 2)
abstract class NoteDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao

    companion object {
        @Volatile private var INSTANCE: NoteDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // cek apakah kolom 'timestamp' sudah ada
                val cursor = database.query("PRAGMA table_info(note)")
                var columnExists = false
                while (cursor.moveToNext()) {
                    val columnName = cursor.getString(cursor.getColumnIndexOrThrow("name"))
                    if (columnName == "timestamp") {
                        columnExists = true
                        break
                    }
                }
                cursor.close()

                if (!columnExists) {
                    database.execSQL("ALTER TABLE note ADD COLUMN timestamp TEXT NOT NULL DEFAULT ''")
                }
            }
        }


        fun getDatabase(context: Context): NoteDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    NoteDatabase::class.java, "note_db"
                )
                    .addMigrations(MIGRATION_1_2) // pakai migration
                    .build().also { INSTANCE = it }
            }
        }
    }
}
