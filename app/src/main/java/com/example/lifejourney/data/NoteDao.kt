package com.example.lifejourney.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface NoteDao {

    @Insert
    suspend fun insert(note: Note): Long

    @Insert
    suspend fun insertImages(images: List<NoteImage>)

    @Transaction
    @Query("SELECT * FROM note ORDER BY timestamp DESC")
    fun getAllNotes(): LiveData<List<NoteWithImages>>



    @Transaction
    @Query("SELECT * FROM note WHERE id = :id")
    fun getNoteById(id: Int): LiveData<NoteWithImages>


    @Update
    suspend fun update(note: Note)

    @Delete
    suspend fun deleteImage(noteImage: NoteImage)

    @Delete
    suspend fun deleteNote(note: Note)

    @Query("SELECT * FROM note ORDER BY timestamp DESC LIMIT 2")
    fun getLatestNotes(): LiveData<List<Note>>



}
