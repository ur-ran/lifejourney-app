// MoodDao.kt
package com.example.lifejourney.data

import androidx.room.*

@Dao
interface MoodDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMood(entry: MoodEntry)

    @Query("SELECT * FROM mood_table")
    suspend fun getAllMoods(): List<MoodEntry>
}
