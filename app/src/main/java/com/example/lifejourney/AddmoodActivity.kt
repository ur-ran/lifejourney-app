package com.example.lifejourney

import android.content.Intent
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.lifejourney.data.AppDatabase
import com.example.lifejourney.data.MoodEntry
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AddmoodActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_addmood)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btnBack = findViewById<ImageView>(R.id.back_button)
        btnBack.setOnClickListener {
            val intent = Intent(this, TrackerActivity::class.java)
            startActivity(intent)
        }

        val seekBar = findViewById<SeekBar>(R.id.moodSeekBar)
        val eyeLeft = findViewById<ImageView>(R.id.eye_left)
        val eyeRight = findViewById<ImageView>(R.id.eye_right)
        val mouth = findViewById<ImageView>(R.id.mouth)
        val textmood = findViewById<TextView>(R.id.mood_text)
        val submit = findViewById<MaterialButton>(R.id.submit_button)

        seekBar.progress = 0
        seekBar.invalidate()

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val thumb: Drawable? = ContextCompat.getDrawable(this@AddmoodActivity, R.drawable.seekbar_thumb)?.mutate()

                when (progress) {
                    0 -> {
                        eyeLeft.setImageResource(R.drawable.eye_shape_good)
                        eyeRight.setImageResource(R.drawable.eye_shape_good)
                        mouth.setImageResource(R.drawable.ellipse)
                        val color = ContextCompat.getColor(this@AddmoodActivity, R.color.good)
                        thumb?.setColorFilter(color, PorterDuff.Mode.SRC_IN)
                        textmood.text = "GOOD"
                        textmood.setTextColor(color)
                        submit.setBackgroundColor(color)
                    }
                    1 -> {
                        eyeLeft.setImageResource(R.drawable.eye_shape_notbad)
                        eyeRight.setImageResource(R.drawable.eye_shape_notbad)
                        mouth.setImageResource(R.drawable.mouth_flat)
                        val color = ContextCompat.getColor(this@AddmoodActivity, R.color.not_bad)
                        thumb?.setColorFilter(color, PorterDuff.Mode.SRC_IN)
                        textmood.text = "NOT BAD"
                        textmood.setTextColor(color)
                        submit.setBackgroundColor(color)
                    }
                    2 -> {
                        eyeLeft.setImageResource(R.drawable.eye_shape_bad)
                        eyeRight.setImageResource(R.drawable.eye_shape_bad)
                        mouth.setImageResource(R.drawable.mouth_bad)
                        val color = ContextCompat.getColor(this@AddmoodActivity, R.color.bad)
                        thumb?.setColorFilter(color, PorterDuff.Mode.SRC_IN)
                        textmood.text = "BAD"
                        textmood.setTextColor(color)
                        submit.setBackgroundColor(color)
                    }
                }

                seekBar?.thumb = thumb
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        submit.setOnClickListener {
            val moodValue = seekBar.progress

            // Format tanggal menggunakan SimpleDateFormat agar support minSdk 24
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = dateFormat.format(Date())

            val moodEntry = MoodEntry(date, moodValue)

            lifecycleScope.launch {
                val db = AppDatabase.getDatabase(applicationContext)
                db.moodDao().insertMood(moodEntry)
                val intent = Intent(this@AddmoodActivity, TrackerActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }
}
