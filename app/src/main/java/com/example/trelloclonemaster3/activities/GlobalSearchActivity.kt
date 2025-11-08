package com.example.trelloclonemaster3.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.trelloclonemaster3.R
import com.example.trelloclonemaster3.adapters.SearchResultsAdapter
import com.example.trelloclonemaster3.firebase.FirestoreClass
import com.example.trelloclonemaster3.model.SearchResult
import com.example.trelloclonemaster3.model.SearchResultType
import com.example.trelloclonemaster3.utils.Constants

class GlobalSearchActivity : BaseActivity() {

    private lateinit var searchView: SearchView
    private lateinit var rvSearchResults: RecyclerView
    private lateinit var tvNoResults: TextView
    private lateinit var tvSearchPrompt: TextView
    private lateinit var searchResultsAdapter: SearchResultsAdapter
    
    private val searchHandler = Handler(Looper.getMainLooper())
    private var searchRunnable: Runnable? = null
    private val SEARCH_DELAY = 300L // Debounce delay in milliseconds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_global_search)
        
        setupActionBar()
        setupViews()
        setupSearchView()
        setupRecyclerView()
    }

    private fun setupActionBar() {
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar_global_search)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = "Global Search"
        
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun setupViews() {
        searchView = findViewById(R.id.search_view)
        rvSearchResults = findViewById(R.id.rv_search_results)
        tvNoResults = findViewById(R.id.tv_no_results)
        tvSearchPrompt = findViewById(R.id.tv_search_prompt)
    }

    private fun setupSearchView() {
        searchView.requestFocus()
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                performSearch(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Cancel previous search
                searchRunnable?.let { searchHandler.removeCallbacks(it) }
                
                if (TextUtils.isEmpty(newText) || newText!!.length < 2) {
                    clearResults()
                    return true
                }
                
                // Create new debounced search
                searchRunnable = Runnable {
                    performSearch(newText)
                }
                searchHandler.postDelayed(searchRunnable!!, SEARCH_DELAY)
                
                return true
            }
        })
    }

    private fun setupRecyclerView() {
        searchResultsAdapter = SearchResultsAdapter(this, ArrayList())
        rvSearchResults.layoutManager = LinearLayoutManager(this)
        rvSearchResults.adapter = searchResultsAdapter
        
        searchResultsAdapter.setOnItemClickListener(object : SearchResultsAdapter.OnItemClickListener {
            override fun onItemClick(searchResult: SearchResult) {
                handleSearchResultClick(searchResult)
            }
        })
    }

    private fun performSearch(query: String?) {
        if (query.isNullOrBlank() || query.length < 2) {
            clearResults()
            return
        }

        showCustomProgressBar()

        // Use the real FirestoreClass search functionality
        FirestoreClass().performGlobalSearch(this, query.trim())
    }

    private fun clearResults() {
        searchResultsAdapter.updateResults(ArrayList())
        showSearchPrompt()
    }

    private fun showSearchPrompt() {
        rvSearchResults.visibility = View.GONE
        tvNoResults.visibility = View.GONE
        tvSearchPrompt.visibility = View.VISIBLE
    }

    private fun showNoResults() {
        rvSearchResults.visibility = View.GONE
        tvSearchPrompt.visibility = View.GONE
        tvNoResults.visibility = View.VISIBLE
    }

    private fun showResults() {
        tvSearchPrompt.visibility = View.GONE
        tvNoResults.visibility = View.GONE
        rvSearchResults.visibility = View.VISIBLE
    }

    fun onSearchResultsReceived(results: ArrayList<SearchResult>) {
        hideCustomProgressDialog()
        
        if (results.isEmpty()) {
            showNoResults()
        } else {
            showResults()
            searchResultsAdapter.updateResults(results)
        }
    }

    private fun handleSearchResultClick(searchResult: SearchResult) {
        when (searchResult.type) {
            SearchResultType.PROJECT -> {
                // Navigate to TaskListActivity for the board
                val intent = Intent(this, TaskListActivity::class.java)
                intent.putExtra(Constants.DOCUMENT_ID, searchResult.id)
                startActivity(intent)
            }
            SearchResultType.TASK -> {
                // Navigate to CardDetailsActivity for the task
                val intent = Intent(this, CardDetailsActivity::class.java)
                intent.putExtra(Constants.BOARD_DETAILS, searchResult.boardId)
                intent.putExtra(Constants.TASK_LIST_ITEM_POSITION, -1) // Will need to find position
                intent.putExtra(Constants.CARD_LIST_ITEM_POSITION, -1) // Will need to find position
                startActivity(intent)
            }
            SearchResultType.USER -> {
                // Could show user profile or do nothing for now
                showErrorSnackBar("User profiles not implemented yet")
            }
        }
    }

    override fun onDestroy() {
        searchRunnable?.let { searchHandler.removeCallbacks(it) }
        super.onDestroy()
    }
}