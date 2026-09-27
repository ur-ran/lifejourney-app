package com.example.lifejourney

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.lifejourney.data.Note
import com.example.lifejourney.data.NoteDatabase
import com.example.lifejourney.data.NoteImage
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class EditNoteActivity : AppCompatActivity() {
    private lateinit var db: NoteDatabase
    private lateinit var ivAddImage: ImageView
    private lateinit var imageContainer: LinearLayout
    private var noteId: Int = -1
    private val newImageUris = mutableListOf<Uri>()

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            val savedUri = copyUriToInternalStorage(it)
            if (savedUri != null) {
                newImageUris.add(savedUri)
                addImageToContainer(savedUri, null)
            } else {
                Toast.makeText(this, "Gagal menyimpan gambar", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_note)

        db = NoteDatabase.getDatabase(this)

        val etTitle = findViewById<EditText>(R.id.etEditTitle)
        val etContent = findViewById<EditText>(R.id.etEditContent)
        ivAddImage = findViewById(R.id.ivNoteImage) // tombol untuk menambah gambar
        imageContainer = findViewById(R.id.editImageContainer)
        val btnUpdate = findViewById<Button>(R.id.btnUpdate)
        val btnBack = findViewById<ImageButton>(R.id.btnBack)

        checkStoragePermission()

        btnBack.setOnClickListener {
            val intent = Intent(this, NoteActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }

        noteId = intent.getIntExtra("note_id", -1)
        if (noteId == -1) {
            Toast.makeText(this, "Note ID tidak ditemukan", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Load note & images
        db.noteDao().getNoteById(noteId).observe(this) { noteWithImages ->
            val note = noteWithImages?.note
            val images = noteWithImages?.images.orEmpty()

            if (note != null) {
                etTitle.setText(note.title)
                etContent.setText(note.content)

                imageContainer.removeAllViews()

                // Jika ada imageUri utama
                note.imageUri?.split(",")?.forEach { uriStr ->
                    val uri = Uri.parse(uriStr.trim())
                    addImageToContainer(uri, null)
                }


                // Tambahkan semua NoteImage ke container
                images.forEach { noteImage ->
                    val uri = Uri.parse(noteImage.imageUri)
                    addImageToContainer(uri, noteImage)
                }

            } else {
                Toast.makeText(this, "Note tidak ditemukan di database", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        ivAddImage.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        btnUpdate.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val content = etContent.text.toString().trim()

            if (title.isEmpty() || content.isEmpty()) {
                Toast.makeText(this, "Judul dan konten tidak boleh kosong", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

            val updatedNote = Note(
                id = noteId,
                title = title,
                content = content,
                imageUri = null, // hilangkan imageUri utama karena sudah ditampilkan di container
                timestamp = timestamp
            )

            lifecycleScope.launch {
                db.noteDao().update(updatedNote)

                // Simpan gambar baru
                val newImages = newImageUris.map { uri ->
                    NoteImage(noteId = noteId, imageUri = uri.toString())
                }
                db.noteDao().insertImages(newImages)

                Toast.makeText(this@EditNoteActivity, "Note berhasil diupdate", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun addImageToContainer(uri: Uri, noteImage: NoteImage?) {
        val imageView = ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 8, 0, 8)
            }
            scaleType = ImageView.ScaleType.FIT_CENTER
            adjustViewBounds = true
            setImageURI(uri)
            background = ContextCompat.getDrawable(this@EditNoteActivity, R.drawable.bg_input)

            setOnLongClickListener {
                imageContainer.removeView(this)
                noteImage?.let { image ->
                    lifecycleScope.launch {
                        db.noteDao().deleteImage(image)
                    }
                }
                true
            }
        }
        imageContainer.addView(imageView)
    }


    private fun copyUriToInternalStorage(uri: Uri): Uri? {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            val fileName = "image_${System.currentTimeMillis()}.jpg"
            val file = File(filesDir, fileName)
            val outputStream = FileOutputStream(file)

            inputStream?.copyTo(outputStream)

            inputStream?.close()
            outputStream.close()

            Uri.fromFile(file)
        } catch (e: Exception) {
            Log.e("EditNoteActivity", "Gagal copy file: $uri", e)
            null
        }
    }

    private fun checkStoragePermission() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(permission), 101)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Izin akses gambar diberikan", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Izin akses gambar ditolak", Toast.LENGTH_SHORT).show()
        }
    }
}
