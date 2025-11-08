package com.example.trelloclonemaster3.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.trelloclonemaster3.R
import com.example.trelloclonemaster3.model.SearchResult
import com.example.trelloclonemaster3.model.SearchResultType

class SearchResultsAdapter(
    private val context: Context,
    private var searchResults: ArrayList<SearchResult>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_RESULT = 1
    }

    private var onItemClickListener: OnItemClickListener? = null

    interface OnItemClickListener {
        fun onItemClick(searchResult: SearchResult)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.onItemClickListener = listener
    }

    // Combined list of headers and results for display
    private val displayItems = ArrayList<DisplayItem>()

    init {
        updateDisplayItems()
    }

    fun updateResults(newResults: ArrayList<SearchResult>) {
        searchResults = newResults
        updateDisplayItems()
        notifyDataSetChanged()
    }

    private fun updateDisplayItems() {
        displayItems.clear()

        // Group results by type
        val projectResults = searchResults.filter { it.type == SearchResultType.PROJECT }
        val taskResults = searchResults.filter { it.type == SearchResultType.TASK }
        val userResults = searchResults.filter { it.type == SearchResultType.USER }

        // Add projects section
        if (projectResults.isNotEmpty()) {
            displayItems.add(DisplayItem.Header("Projects (${projectResults.size})"))
            projectResults.forEach { displayItems.add(DisplayItem.Result(it)) }
        }

        // Add tasks section
        if (taskResults.isNotEmpty()) {
            displayItems.add(DisplayItem.Header("Tasks (${taskResults.size})"))
            taskResults.forEach { displayItems.add(DisplayItem.Result(it)) }
        }

        // Add users section
        if (userResults.isNotEmpty()) {
            displayItems.add(DisplayItem.Header("Team Members (${userResults.size})"))
            userResults.forEach { displayItems.add(DisplayItem.Result(it)) }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (displayItems[position]) {
            is DisplayItem.Header -> TYPE_HEADER
            is DisplayItem.Result -> TYPE_RESULT
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_HEADER -> {
                val view =
                    LayoutInflater.from(context).inflate(R.layout.item_search_header, parent, false)
                HeaderViewHolder(view)
            }

            else -> {
                val view =
                    LayoutInflater.from(context).inflate(R.layout.item_search_result, parent, false)
                ResultViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = displayItems[position]) {
            is DisplayItem.Header -> {
                (holder as HeaderViewHolder).bind(item.title)
            }

            is DisplayItem.Result -> {
                (holder as ResultViewHolder).bind(item.searchResult)
            }
        }
    }

    override fun getItemCount(): Int = displayItems.size

    inner class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvHeader: TextView = itemView.findViewById(R.id.tv_search_header)

        fun bind(title: String) {
            tvHeader.text = title
        }
    }

    inner class ResultViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivIcon: ImageView = itemView.findViewById(R.id.iv_result_icon)
        private val tvTitle: TextView = itemView.findViewById(R.id.tv_result_title)
        private val tvSubtitle: TextView = itemView.findViewById(R.id.tv_result_subtitle)

        fun bind(searchResult: SearchResult) {
            tvTitle.text = searchResult.title
            tvSubtitle.text = searchResult.subtitle

            // Set appropriate icon based on type
            when (searchResult.type) {
                SearchResultType.PROJECT -> {
                    if (searchResult.imageUrl.isNotEmpty()) {
                        Glide.with(context)
                            .load(searchResult.imageUrl)
                            .centerCrop()
                            .placeholder(R.drawable.ic_project)
                            .into(ivIcon)
                    } else {
                        ivIcon.setImageResource(R.drawable.ic_project)
                    }
                }

                SearchResultType.TASK -> {
                    ivIcon.setImageResource(R.drawable.ic_vector_add_24dp)
                }

                SearchResultType.USER -> {
                    if (searchResult.imageUrl.isNotEmpty()) {
                        Glide.with(context)
                            .load(searchResult.imageUrl)
                            .centerCrop()
                            .placeholder(R.drawable.ic_user_place_holder)
                            .into(ivIcon)
                    } else {
                        ivIcon.setImageResource(R.drawable.ic_user_place_holder)
                    }
                }
            }

            itemView.setOnClickListener {
                onItemClickListener?.onItemClick(searchResult)
            }
        }
    }

    sealed class DisplayItem {
        data class Header(val title: String) : DisplayItem()
        data class Result(val searchResult: SearchResult) : DisplayItem()
    }
}