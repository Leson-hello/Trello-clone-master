package com.example.trelloclonemaster3.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import com.example.trelloclonemaster3.R
import com.example.trelloclonemaster3.model.TaskStatus

abstract class StatusListDialog(
    context: Context,
    private var title: String = "",
    private var selectedStatus: TaskStatus = TaskStatus.PENDING
) : Dialog(context) {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_status_list, null)

        setContentView(view)
        setCanceledOnTouchOutside(true)
        setCancelable(true)
        setupDialog(view)
    }

    private fun setupDialog(view: View) {
        val tvTitle = view.findViewById<TextView>(R.id.tv_title)
        val radioGroup = view.findViewById<RadioGroup>(R.id.rg_status_options)
        val btnSelect = view.findViewById<TextView>(R.id.btn_select)

        tvTitle.text = title

        // Set up radio buttons
        val rbPending = view.findViewById<RadioButton>(R.id.rb_pending)
        val rbInProgress = view.findViewById<RadioButton>(R.id.rb_in_progress)
        val rbCompleted = view.findViewById<RadioButton>(R.id.rb_completed)

        // Set current selection
        when (selectedStatus) {
            TaskStatus.PENDING -> rbPending.isChecked = true
            TaskStatus.IN_PROGRESS -> rbInProgress.isChecked = true
            TaskStatus.COMPLETED -> rbCompleted.isChecked = true
        }

        btnSelect.setOnClickListener {
            val checkedId = radioGroup.checkedRadioButtonId
            val newStatus = when (checkedId) {
                R.id.rb_pending -> TaskStatus.PENDING
                R.id.rb_in_progress -> TaskStatus.IN_PROGRESS
                R.id.rb_completed -> TaskStatus.COMPLETED
                else -> TaskStatus.PENDING
            }
            onItemSelected(newStatus)
            dismiss()
        }
    }

    abstract fun onItemSelected(status: TaskStatus)
}