package com.example.lifejourney.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey

@Entity(
    tableName = "note_image",
    foreignKeys = [ForeignKey(
        entity = Note::class,
        parentColumns = ["id"],
        childColumns = ["noteId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class NoteImage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val noteId: Int,
    val imageUri: String
)
