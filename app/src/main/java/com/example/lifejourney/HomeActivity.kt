package com.example.lifejourney

import RecentNoteAdapter
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lifejournal.data.UserPreferences
import com.example.lifejourney.data.NoteDatabase
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.*

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // --- Fullscreen mode ---
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                )

        setContentView(R.layout.activity_home)

        val preferences = UserPreferences(this)
        val user = preferences.getUser()

        val tvGreeting = findViewById<TextView>(R.id.tvGreeting)
        val imgProfile = findViewById<ImageView>(R.id.imgProfile)

        // Tampilkan nama
        tvGreeting.text = if (user.name.isNotEmpty()) "Hello, ${user.name}" else "Hello!"

        // Tampilkan gambar profil
        if (user.imageUri.isNotEmpty()) {
            try {
                val uri = Uri.parse(user.imageUri)
                contentResolver.openInputStream(uri)?.close() // cek apakah URI masih valid
                imgProfile.setImageURI(uri)
            } catch (e: Exception) {
                e.printStackTrace()
                imgProfile.setImageResource(R.drawable.ic_profile_placeholder)
            }
        } else {
            imgProfile.setImageResource(R.drawable.ic_profile_placeholder)
        }

        // Klik ke halaman profil
        imgProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        // Tanggal dan waktu
        val tvDay = findViewById<TextView>(R.id.tvDay)
        val tvDate = findViewById<TextView>(R.id.tvDate)
        val tvMonth = findViewById<TextView>(R.id.tvMonth)
        val tvTime = findViewById<TextView>(R.id.tvTime)

        val calendar = Calendar.getInstance()
        val dayFormat = SimpleDateFormat("EEEE", Locale.ENGLISH)
        val monthFormat = SimpleDateFormat("MMMM", Locale.ENGLISH)
        val timeFormat = SimpleDateFormat("HH.mm", Locale.ENGLISH)

        tvDay.text = dayFormat.format(calendar.time)
        tvDate.text = calendar.get(Calendar.DAY_OF_MONTH).toString()
        tvMonth.text = monthFormat.format(calendar.time)
        tvTime.text = timeFormat.format(calendar.time)




        val rvRecentlyAdded = findViewById<RecyclerView>(R.id.rvRecentlyAdded)
        val recentAdapter = RecentNoteAdapter(emptyList()) { note ->
            val intent = Intent(this, EditNoteActivity::class.java)
            intent.putExtra("note_id", note.id)
            startActivity(intent)
        }
        rvRecentlyAdded.adapter = recentAdapter
        rvRecentlyAdded.layoutManager = GridLayoutManager(this, 2)

        val db = NoteDatabase.getDatabase(this)
        db.noteDao().getLatestNotes().observe(this) { notes ->
            recentAdapter.updateNotes(notes)
        }




        // Navigasi tombol
        findViewById<MaterialButton>(R.id.btnAddMood).setOnClickListener {
            startActivity(Intent(this, AddmoodActivity::class.java))
        }
        findViewById<MaterialButton>(R.id.btnAddNote).setOnClickListener {
            startActivity(Intent(this, AddnoteActivity::class.java))
        }

        findViewById<ImageView>(R.id.nav_calendar).setOnClickListener {
            startActivity(Intent(this, TrackerActivity::class.java))
        }
        findViewById<ImageView>(R.id.nav_note).setOnClickListener {
            startActivity(Intent(this, NoteActivity::class.java))
        }
        findViewById<ImageView>(R.id.nav_home).setOnClickListener {
            // Sudah di home, tidak pindah
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh user data saat kembali dari ProfileActivity
        val preferences = UserPreferences(this)
        val user = preferences.getUser()

        val imgProfile = findViewById<ImageView>(R.id.imgProfile)
        if (user.imageUri.isNotEmpty()) {
            try {
                val uri = Uri.parse(user.imageUri)
                contentResolver.openInputStream(uri)?.close()
                imgProfile.setImageURI(uri)
            } catch (e: Exception) {
                e.printStackTrace()
                imgProfile.setImageResource(R.drawable.ic_profile_placeholder)
            }
        } else {
            imgProfile.setImageResource(R.drawable.ic_profile_placeholder)
        }

        val tvGreeting = findViewById<TextView>(R.id.tvGreeting)
        tvGreeting.text = if (user.name.isNotEmpty()) "Hello, ${user.name}" else "Hello!"
    }
}
