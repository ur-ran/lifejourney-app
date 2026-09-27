package com.example.lifejourney

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.lifejournal.data.User
import com.example.lifejournal.data.UserPreferences

class ProfileActivity : AppCompatActivity() {

    private lateinit var preferences: UserPreferences
    private lateinit var imgProfile: ImageView
    private lateinit var tvName: TextView

    // Gunakan OpenDocument agar bisa mengambil URI yang persistable
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            // Ambil izin persistable agar bisa dipakai jangka panjang
            contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )

            imgProfile.setImageURI(uri)
            preferences.saveUser(User(tvName.text.toString(), uri.toString()))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        preferences = UserPreferences(this)
        imgProfile = findViewById(R.id.imgProfile)
        tvName = findViewById(R.id.tvName)
        val btnEditProfile = findViewById<Button>(R.id.btnEditProfile)

        // Load user data
        val user = preferences.getUser()
        tvName.text = user.name

        val btnBack = findViewById<ImageView>(R.id.back_button)
        btnBack.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }

        if (user.imageUri.isNotEmpty()) {
            try {
                val uri = Uri.parse(user.imageUri)
                contentResolver.openInputStream(uri)?.close() // cek apakah bisa dibuka
                imgProfile.setImageURI(uri)
            } catch (e: Exception) {
                e.printStackTrace()
                imgProfile.setImageResource(R.drawable.ic_profile_placeholder)
            }
        } else {
            imgProfile.setImageResource(R.drawable.ic_profile_placeholder)
        }

        // Klik gambar untuk ganti
        imgProfile.setOnClickListener {
            imagePickerLauncher.launch(arrayOf("image/*"))
        }

        // Klik tombol untuk edit nama
        btnEditProfile.setOnClickListener {
            showEditDialog()
        }

        // Optional: untuk padding edge-to-edge
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }


    private fun showEditDialog() {
        val editText = EditText(this)
        editText.setText(tvName.text)

        AlertDialog.Builder(this)
            .setTitle("Edit Name")
            .setView(editText)
            .setPositiveButton("Save") { _, _ ->
                val newName = editText.text.toString()
                tvName.text = newName
                val uri = preferences.getUser().imageUri
                preferences.saveUser(User(newName, uri))
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
