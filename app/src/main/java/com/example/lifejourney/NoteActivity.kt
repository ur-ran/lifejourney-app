package com.example.lifejourney

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.lifejournal.data.UserPreferences
import com.example.lifejourney.adapter.NoteAdapter
import com.example.lifejourney.data.Note
import com.example.lifejourney.data.NoteDatabase
import com.google.android.material.floatingactionbutton.FloatingActionButton
import android.util.Log

class NoteActivity : AppCompatActivity() {
    private lateinit var imgProfile: ImageView
    private lateinit var noteAdapter: NoteAdapter
    private lateinit var db: NoteDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Fullscreen immersive
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                )

        setContentView(R.layout.activity_note)

        // UI insets padding
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        db = NoteDatabase.getDatabase(this)

        val rvNotes = findViewById<RecyclerView>(R.id.rvNotes)
        rvNotes.layoutManager = GridLayoutManager(this, 2)

        // 🔥 Tambahkan adapter ke RecyclerView
        noteAdapter = NoteAdapter(
            emptyList(),
            onNoteClick = { note ->
                val intent = Intent(this, EditNoteActivity::class.java).apply {
                    putExtra("note_id", note.id)
                }
                startActivity(intent)
            },
            onNoteLongClick = { note ->
                showDeleteDialog(note)
            }
        )
        rvNotes.adapter = noteAdapter




        // Observe LiveData dari Room
        db.noteDao().getAllNotes().observe(this) { noteWithImagesList ->
            Log.d("NoteActivity", "Jumlah note: ${noteWithImagesList.size}") // Debug log

            val notes = noteWithImagesList.map { it.note } // Ambil Note dari NoteWithImages
            noteAdapter.updateNotes(notes)
        }

        // Profile image
        imgProfile = findViewById(R.id.imgProfile)
        imgProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        val preferences = UserPreferences(this)
        val user = preferences.getUser()

        if (!user.imageUri.isNullOrEmpty()) {
            Glide.with(this)
                .load(Uri.parse(user.imageUri))
                .placeholder(R.drawable.ic_profile_placeholder)
                .error(R.drawable.ic_profile_placeholder)
                .into(imgProfile)
        } else {
            imgProfile.setImageResource(R.drawable.ic_profile_placeholder)
        }

        // FAB tambah note
        findViewById<FloatingActionButton>(R.id.btnAddNote).setOnClickListener {
            startActivity(Intent(this, AddnoteActivity::class.java))
        }

        // Bottom navigation
        findViewById<ImageView>(R.id.nav_home).setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
        }

        findViewById<ImageView>(R.id.nav_note).setOnClickListener {
            // Already in note page
        }

        findViewById<ImageView>(R.id.nav_calendar).setOnClickListener {
            startActivity(Intent(this, TrackerActivity::class.java))
        }
    }

    private fun onNoteClicked(note: Note) {
        val intent = Intent(this, EditNoteActivity::class.java).apply {
            putExtra("note_id", note.id)
        }
        startActivity(intent)
    }

    private fun showDeleteDialog(note: Note) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Hapus Catatan")
            .setMessage("Apakah Anda yakin ingin menghapus catatan ini?")
            .setPositiveButton("Hapus") { _, _ ->
                lifecycleScope.launchWhenStarted {
                    db.noteDao().deleteNote(note)
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }
}
