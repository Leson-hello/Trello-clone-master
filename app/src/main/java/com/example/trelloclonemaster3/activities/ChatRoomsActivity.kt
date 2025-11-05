package com.example.trelloclonemaster3.activities

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.trelloclonemaster3.R
import com.example.trelloclonemaster3.adapters.ChatRoomsAdapter
import com.example.trelloclonemaster3.firebase.FirestoreClass
import com.example.trelloclonemaster3.model.ChatRoom
import com.example.trelloclonemaster3.utils.Constants
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import androidx.appcompat.widget.Toolbar

class ChatRoomsActivity : BaseActivity() {

    private lateinit var rvChatRooms: RecyclerView
    private lateinit var etSearchChat: TextInputEditText
    private lateinit var fabNewChat: FloatingActionButton
    private lateinit var toolbar: Toolbar

    private lateinit var chatRoomsAdapter: ChatRoomsAdapter
    private var chatRoomsList: ArrayList<ChatRoom> = ArrayList()
    private var filteredChatRoomsList: ArrayList<ChatRoom> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_rooms)

        setupActionBar()
        setupViews()
        setupRecyclerView()
        setupSearchFunctionality()
        setupFloatingActionButton()

        loadChatRooms()
    }

    private fun setupActionBar() {
        toolbar = findViewById(R.id.toolbar_chat_rooms_activity)
        setSupportActionBar(toolbar)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_back_color_24dp)
            actionBar.title = "Chat Rooms"
        }

        toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun setupViews() {
        rvChatRooms = findViewById(R.id.rv_chat_rooms_list)
        etSearchChat = findViewById(R.id.et_search_chat)
        fabNewChat = findViewById(R.id.fab_create_new_chat)
    }

    private fun setupRecyclerView() {
        rvChatRooms.layoutManager = LinearLayoutManager(this)
        rvChatRooms.setHasFixedSize(true)
        
        chatRoomsAdapter = ChatRoomsAdapter(this, filteredChatRoomsList)
        rvChatRooms.adapter = chatRoomsAdapter
    }

    private fun setupSearchFunctionality() {
        etSearchChat.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterChatRooms(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupFloatingActionButton() {
        fabNewChat.setOnClickListener {
            // TODO: Implement create new chat room functionality
            showNewChatDialog()
        }
    }

    private fun filterChatRooms(query: String) {
        filteredChatRoomsList.clear()
        
        if (query.isEmpty()) {
            filteredChatRoomsList.addAll(chatRoomsList)
        } else {
            for (chatRoom in chatRoomsList) {
                if (chatRoom.name.lowercase().contains(query.lowercase()) ||
                    chatRoom.description.lowercase().contains(query.lowercase())) {
                    filteredChatRoomsList.add(chatRoom)
                }
            }
        }
        
        chatRoomsAdapter.notifyDataSetChanged()
        updateEmptyView()
    }

    private fun loadChatRooms() {
        FirestoreClass().getChatRooms(this)
    }

    private fun showNewChatDialog() {
        // TODO: Show dialog to create new chat room or start direct message
        Toast.makeText(this, "Create new chat feature will be implemented", Toast.LENGTH_SHORT).show()
    }

    private fun updateEmptyView() {
        if (filteredChatRoomsList.isEmpty()) {
            rvChatRooms.visibility = View.GONE
            // TODO: Show empty state view
        } else {
            rvChatRooms.visibility = View.VISIBLE
        }
    }

    fun populateChatRoomsList(chatRooms: ArrayList<ChatRoom>) {
        chatRoomsList.clear()
        chatRoomsList.addAll(chatRooms)
        
        filteredChatRoomsList.clear()
        filteredChatRoomsList.addAll(chatRooms)
        
        chatRoomsAdapter.notifyDataSetChanged()
        updateEmptyView()
        
        Log.d("ChatRooms", "Loaded ${chatRooms.size} chat rooms")
    }

    fun onChatRoomClick(chatRoom: ChatRoom) {
        val intent = Intent(this, ChatActivity::class.java)
        intent.putExtra(Constants.CHAT_ROOM, chatRoom)
        startActivity(intent)
    }

    fun onChatRoomsLoadFailed() {
        Toast.makeText(this, "Failed to load chat rooms", Toast.LENGTH_SHORT).show()
        updateEmptyView()
    }

    override fun onResume() {
        super.onResume()
        // Refresh chat rooms when returning to this activity
        loadChatRooms()
    }
}