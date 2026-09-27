// MoodEntry.kt
package com.example.lifejourney.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mood_table")
data class MoodEntry(
    @PrimaryKey val date: String, // format: "yyyy-MM-dd"
    val mood: Int // 0 = GOOD, 1 = NOT BAD, 2 = BAD
)
