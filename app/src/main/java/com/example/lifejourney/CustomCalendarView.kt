package com.example.lifejourney.ui

import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.GridLayout
import android.widget.TextView
import android.widget.LinearLayout
import com.example.lifejourney.AddmoodActivity
import com.example.lifejourney.R
import com.example.lifejourney.data.MoodEntry
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

class CustomCalendarView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    private var moodMap: Map<String, Int> = emptyMap()
    private lateinit var displayedDate: LocalDate
    private lateinit var calendarGrid: GridLayout
    private lateinit var monthText: TextView

    init {
        orientation = VERTICAL
        LayoutInflater.from(context).inflate(R.layout.activity_custom_calendar_view, this, true)
        calendarGrid = findViewById(R.id.calendar_grid)
        monthText = findViewById(R.id.month_text)
    }

    fun setMoodData(moodList: List<MoodEntry>, displayMonth: LocalDate = LocalDate.now()) {
        moodMap = moodList.associateBy({ it.date }, { it.mood })
        drawCalendar(displayMonth)
    }

    private fun drawCalendar(currentDate: LocalDate) {
        calendarGrid.removeAllViews()
        displayedDate = currentDate

        val yearMonth = YearMonth.from(currentDate)
        val firstOfMonth = yearMonth.atDay(1)
        val daysInMonth = yearMonth.lengthOfMonth()
        val firstDayOfWeek = firstOfMonth.dayOfWeek.value % 7 // Sunday = 0

        val formatter = DateTimeFormatter.ofPattern("MMMM yyyy")
        monthText.text = currentDate.format(formatter)

        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

        // Kosongkan sel sebelum tanggal 1
        for (i in 0 until firstDayOfWeek) {
            calendarGrid.addView(createEmptyCell())
        }

        for (day in 1..daysInMonth) {
            val date = yearMonth.atDay(day)
            val dateStr = date.format(dateFormatter)
            val mood = moodMap[dateStr]
            val view = createDayView(day, mood, dateStr)
            calendarGrid.addView(view)
        }
    }

    private fun createEmptyCell(): View {
        val view = View(context)
        val params = GridLayout.LayoutParams()
        params.width = 0
        params.height = 120
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
        view.layoutParams = params
        return view
    }

    private fun createDayView(day: Int, mood: Int?, dateStr: String): View {
        val view = LayoutInflater.from(context).inflate(R.layout.calendar_day_view, calendarGrid, false)
        val textView = view.findViewById<TextView>(R.id.textViewDate)

        textView.text = day.toString()

        when (mood) {
            0 -> textView.setBackgroundResource(R.drawable.circle_good)
            1 -> textView.setBackgroundResource(R.drawable.circle_not_bad)
            2 -> textView.setBackgroundResource(R.drawable.circle_bad)
            else -> textView.setBackgroundResource(R.drawable.circle_empty)
        }

        return view
    }
}
