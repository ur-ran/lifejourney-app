package com.example.lifejourney

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.lifejourney.data.Note
import com.example.lifejourney.data.NoteDatabase
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class AddnoteActivity : AppCompatActivity() {
    private lateinit var db: NoteDatabase
    private lateinit var ivAddImage: ImageView
    private lateinit var imageContainer: LinearLayout
    private val selectedImageUris = mutableListOf<Uri>()

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            val savedUri = copyUriToInternalStorage(it)
            if (savedUri != null) {
                selectedImageUris.add(savedUri)
                addImageToContainer(savedUri)
            } else {
                Toast.makeText(this, "Gagal menyimpan gambar", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_addnote)

        db = NoteDatabase.getDatabase(this)

        val etTitle = findViewById<EditText>(R.id.etTitle)
        val etContent = findViewById<EditText>(R.id.etContent)
        ivAddImage = findViewById(R.id.ivNoteImage)
        imageContainer = findViewById(R.id.imageContainer)
        val btnSave = findViewById<Button>(R.id.btnSave)
        val btnBack = findViewById<ImageButton>(R.id.btnBack)

        checkStoragePermission()

        ivAddImage.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        btnBack.setOnClickListener {
            val intent = Intent(this, NoteActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }

        btnSave.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val content = etContent.text.toString().trim()

            if (title.isEmpty() || content.isEmpty()) {
                Toast.makeText(this, "Judul dan konten tidak boleh kosong", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            val imageUrisString = selectedImageUris.joinToString(",") { it.toString() }

            val note = Note(
                title = title,
                content = content,
                imageUri = imageUrisString,
                timestamp = timestamp
            )


            lifecycleScope.launch {
                db.noteDao().insert(note)
                Toast.makeText(this@AddnoteActivity, "Note berhasil disimpan", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun addImageToContainer(imageUri: Uri) {
        val imageView = ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 8, 0, 8)
            }
            scaleType = ImageView.ScaleType.FIT_CENTER
            adjustViewBounds = true
            setImageURI(imageUri)
            background = ContextCompat.getDrawable(this@AddnoteActivity, R.drawable.bg_input)
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
            Log.e("AddNoteActivity", "Gagal copy file: $uri", e)
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
