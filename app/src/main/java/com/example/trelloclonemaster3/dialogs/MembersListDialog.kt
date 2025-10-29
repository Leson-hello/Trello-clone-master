package com.example.trelloclonemaster3.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.trelloclonemaster3.R
import com.example.trelloclonemaster3.adapters.MembersListItemAdapter
import com.example.trelloclonemaster3.model.Board
import com.example.trelloclonemaster3.model.User

abstract class MembersListDialog(
        context: Context,
        private var list: ArrayList<User>,
        private val board: Board,
        private val title: String = ""
) : Dialog(context) {

    private var adapter: MembersListItemAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState ?: Bundle())

        val view = LayoutInflater.from(context).inflate(R.layout.dialog_list, null)

        setContentView(view)
        setCanceledOnTouchOutside(true)
        setCancelable(true)
        setUpRecyclerView(view)
    }

    private fun setUpRecyclerView(view: View) {

        val tvTitle = findViewById<TextView>(R.id.tvTitle)
        val rvList =  findViewById<RecyclerView>(R.id.rvList)

        tvTitle.text = title

        if (list.size > 0) {

            rvList.layoutManager = LinearLayoutManager(context)
            adapter = MembersListItemAdapter(context, list, board)
            rvList.adapter = adapter

            adapter!!.setOnClickListener(object :
                    MembersListItemAdapter.OnClickListener {
                override fun onClick(position: Int, user: User, action:String) {
                    dismiss()
                    onItemSelected(user, action)
                }
            })
        }
    }

    protected abstract fun onItemSelected(user: User, action:String)
}