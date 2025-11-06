package com.example.trelloclonemaster3.activities

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.CalendarView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.trelloclonemaster3.R
import com.example.trelloclonemaster3.adapters.CalendarTasksAdapter
import com.example.trelloclonemaster3.firebase.FirestoreClass
import com.example.trelloclonemaster3.model.Card
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class CalendarActivity : BaseActivity() {

    companion object {
        private const val TAG = "CalendarActivity"
    }

    private lateinit var mCalendarView: CalendarView
    private lateinit var mRecyclerViewTasks: RecyclerView
    private lateinit var mTextViewSelectedDate: TextView
    private lateinit var mTextViewNoTasks: TextView
    
    private lateinit var mCalendarTasksAdapter: CalendarTasksAdapter
    private var mAllTasksWithDates = ArrayList<Card>()
    private var mSelectedDateTasks = ArrayList<Card>()
    private var mSelectedDate = Date()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar_simple)
        
        setupActionBar()
        setupUI()
        loadTasksForCurrentMonth()
    }
    
    private fun setupActionBar() {
        setSupportActionBar(findViewById(R.id.toolbar_calendar_activity))
        
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
            actionBar.title = "Calendar"
        }
        
        findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar_calendar_activity).setNavigationOnClickListener {
            onBackPressed()
        }
    }
    
    private fun setupUI() {
        mCalendarView = findViewById(R.id.calendar_view)
        mRecyclerViewTasks = findViewById(R.id.rv_calendar_tasks)
        mTextViewSelectedDate = findViewById(R.id.tv_selected_date)
        mTextViewNoTasks = findViewById(R.id.tv_no_tasks_on_date)
        
        // Set today as default selected date
        mSelectedDate = Date()
        updateSelectedDateDisplay(mSelectedDate)

        // Setup calendar listeners
        mCalendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val calendar = Calendar.getInstance()
            calendar.set(year, month, dayOfMonth)
            mSelectedDate = calendar.time
            updateSelectedDateDisplay(mSelectedDate)
            loadTasksForSelectedDate(mSelectedDate)
        }

        // Setup RecyclerView
        mRecyclerViewTasks.layoutManager = LinearLayoutManager(this)
        mCalendarTasksAdapter = CalendarTasksAdapter(this, mSelectedDateTasks)
        mRecyclerViewTasks.adapter = mCalendarTasksAdapter

        // Set click listener for task items
        mCalendarTasksAdapter.setOnClickListener(object : CalendarTasksAdapter.OnClickListener {
            override fun onClick(position: Int, model: Card) {
                // Navigate to card details or show task details
                Log.d(TAG, "Clicked on task: ${model.name}")
                // You can add navigation to CardDetailsActivity here if needed
            }
        })

        loadTasksForSelectedDate(mSelectedDate)
    }

    private fun updateSelectedDateDisplay(date: Date) {
        val dateFormat = SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault())
        mTextViewSelectedDate.text = dateFormat.format(date)
    }
    
    private fun loadTasksForCurrentMonth() {
        showCustomProgressBar()
        val today = Date()
        loadTasksForMonth(today)
    }

    private fun loadTasksForMonth(date: Date) {
        Log.d(
            TAG,
            "Loading tasks for month: ${
                SimpleDateFormat(
                    "MM/yyyy",
                    Locale.getDefault()
                ).format(date)
            }"
        )
        
        // Calculate start and end of month in milliseconds
        val calendar = Calendar.getInstance()
        calendar.time = date

        // Start of month
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startTime = calendar.timeInMillis

        // End of month
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val endTime = calendar.timeInMillis

        FirestoreClass().getTasksWithDueDatesInRange(this, startTime, endTime)
    }

    private fun loadTasksForSelectedDate(date: Date) {
        // Calculate start and end of day in milliseconds
        val calendar = Calendar.getInstance()
        calendar.time = date

        // Start of day
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startTime = calendar.timeInMillis

        // End of day
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val endTime = calendar.timeInMillis

        // Filter tasks for selected date
        mSelectedDateTasks.clear()
        for (task in mAllTasksWithDates) {
            if (task.dueDate in startTime..endTime) {
                mSelectedDateTasks.add(task)
            }
        }
        
        updateTasksList()
    }
    
    private fun updateTasksList() {
        if (mSelectedDateTasks.isEmpty()) {
            mRecyclerViewTasks.visibility = View.GONE
            mTextViewNoTasks.visibility = View.VISIBLE
        } else {
            mRecyclerViewTasks.visibility = View.VISIBLE
            mTextViewNoTasks.visibility = View.GONE
            mCalendarTasksAdapter.notifyDataSetChanged()
        }
    }
    
    fun populateTasksForMonth(tasks: ArrayList<Card>) {
        hideCustomProgressDialog()
        
        mAllTasksWithDates.clear()
        mAllTasksWithDates.addAll(tasks)
        
        Log.d(TAG, "Received ${tasks.size} tasks with due dates for the month")
        
        // Update tasks for currently selected date
        loadTasksForSelectedDate(mSelectedDate)
    }
    
    fun onTasksLoadFailed(error: String) {
        hideCustomProgressDialog()
        Log.e(TAG, "Failed to load tasks: $error")
        showErrorSnackBar("Failed to load calendar tasks: $error")
    }
}