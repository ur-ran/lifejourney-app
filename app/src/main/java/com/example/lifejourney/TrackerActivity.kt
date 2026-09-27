package com.example.lifejourney

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.lifejourney.data.AppDatabase
import com.example.lifejourney.ui.CustomCalendarView
import com.example.lifejournal.data.UserPreferences
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class TrackerActivity : AppCompatActivity() {
    private lateinit var imgProfile: ImageView
    private lateinit var calendarView: CustomCalendarView
    private lateinit var monthText: TextView
    private var currentDate = LocalDate.now()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_tracker)

        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                        View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                )

        monthText = findViewById(R.id.month_text)
        val btnPrev = findViewById<ImageView>(R.id.btn_prev_month)
        val btnNext = findViewById<ImageView>(R.id.btn_next_month)
        calendarView = findViewById(R.id.calendar_view)
        val btnAddMood = findViewById<MaterialButton>(R.id.btnAddMood)
        imgProfile = findViewById(R.id.imgProfile)

        updateCalendarView()

        btnPrev.setOnClickListener {
            currentDate = currentDate.minusMonths(1)
            updateCalendarView()
        }

        btnNext.setOnClickListener {
            currentDate = currentDate.plusMonths(1)
            updateCalendarView()
        }

        monthText.setOnClickListener {
            showMonthYearPicker()
        }

        btnAddMood.setOnClickListener {
            startActivity(Intent(this, AddmoodActivity::class.java))
        }

        imgProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<ImageView>(R.id.nav_calendar).setOnClickListener { }
        findViewById<ImageView>(R.id.nav_note).setOnClickListener {
            startActivity(Intent(this, NoteActivity::class.java))
        }
        findViewById<ImageView>(R.id.nav_home).setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
        }
    }

    private fun showMonthYearPicker() {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, currentDate.year)
        calendar.set(Calendar.MONTH, currentDate.monthValue - 1)

        val datePicker = DatePickerDialog(
            this,
            { _, year, month, _ ->
                currentDate = LocalDate.of(year, month + 1, 1)
                updateCalendarView()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            1
        )

        // Hide day picker (optional)
        try {
            val dayField = datePicker.datePicker.findViewById<View>(
                resources.getIdentifier("day", "id", "android")
            )
            dayField?.visibility = View.GONE
        } catch (e: Exception) {
            e.printStackTrace()
        }

        datePicker.show()
    }

    private fun updateCalendarView() {
        val formatter = DateTimeFormatter.ofPattern("MMMM yyyy")
        monthText.text = currentDate.format(formatter)
        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(applicationContext)
            val moodList = db.moodDao().getAllMoods()
            calendarView.setMoodData(moodList, currentDate)
        }
    }

    override fun onResume() {
        super.onResume()
        val preferences = UserPreferences(this)
        val user = preferences.getUser()

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
    }
}
