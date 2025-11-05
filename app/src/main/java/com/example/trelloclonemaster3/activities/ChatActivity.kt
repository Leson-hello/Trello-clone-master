package com.example.trelloclonemaster3.activities

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.trelloclonemaster3.R
import com.example.trelloclonemaster3.adapters.ChatMessagesAdapter
import com.example.trelloclonemaster3.firebase.FirestoreClass
import com.example.trelloclonemaster3.model.ChatMessage
import com.example.trelloclonemaster3.model.ChatRoom
import com.example.trelloclonemaster3.model.User
import com.example.trelloclonemaster3.utils.Constants
import com.google.android.material.textfield.TextInputEditText
import androidx.appcompat.widget.Toolbar
import com.google.firebase.firestore.*

class ChatActivity : BaseActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var rvChatMessages: RecyclerView
    private lateinit var etMessage: TextInputEditText
    private lateinit var ibSendMessage: ImageButton

    private lateinit var chatMessagesAdapter: ChatMessagesAdapter
    private var messagesList: ArrayList<ChatMessage> = ArrayList()
    private var currentUser: User? = null
    private var chatRoom: ChatRoom? = null

    // Real-time listener
    private var messagesListener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        getIntentData()
        setupActionBar()
        setupViews()
        setupRecyclerView()
        setupSendMessage()

        loadCurrentUser()
        loadMessages()
        setupRealTimeListener()
    }

    private fun getIntentData() {
        if (intent.hasExtra(Constants.CHAT_ROOM)) {
            chatRoom = intent.getParcelableExtra(Constants.CHAT_ROOM)
        }
    }

    private fun setupActionBar() {
        toolbar = findViewById(R.id.toolbar_chat_activity)
        setSupportActionBar(toolbar)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_back_color_24dp)
            actionBar.title = chatRoom?.name ?: "Chat"
        }

        toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun setupViews() {
        rvChatMessages = findViewById(R.id.rv_chat_messages)
        etMessage = findViewById(R.id.et_message_input)
        ibSendMessage = findViewById(R.id.ib_send_message)
    }

    private fun setupRecyclerView() {
        val layoutManager = LinearLayoutManager(this)
        layoutManager.stackFromEnd = true // Show latest messages at bottom
        rvChatMessages.layoutManager = layoutManager
        rvChatMessages.setHasFixedSize(true)

        chatMessagesAdapter =
            ChatMessagesAdapter(this, messagesList, FirestoreClass().getCurrentUserID())
        rvChatMessages.adapter = chatMessagesAdapter
    }

    private fun setupSendMessage() {
        // Enable/disable send button based on message content
        etMessage.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                ibSendMessage.isEnabled = s?.toString()?.trim()?.isNotEmpty() == true
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        ibSendMessage.setOnClickListener {
            sendMessage()
        }
    }

    private fun loadCurrentUser() {
        FirestoreClass().getCurrentUserDetails(this)
    }

    private fun loadMessages() {
        if (chatRoom != null) {
            // Don't show progress dialog for better UX in chat
            FirestoreClass().getChatMessages(this, chatRoom!!.id)
        }
    }

    private fun setupRealTimeListener() {
        if (chatRoom != null) {
            val messagesRef = FirebaseFirestore.getInstance()
                .collection(Constants.CHAT_ROOMS)
                .document(chatRoom!!.id)
                .collection(Constants.MESSAGES)
                .orderBy("timestamp", Query.Direction.ASCENDING)

            messagesListener = messagesRef.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("ChatActivity", "Error listening to messages", error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val newMessages = ArrayList<ChatMessage>()
                    for (document in snapshot.documents) {
                        try {
                            val message = document.toObject(ChatMessage::class.java)
                            if (message != null) {
                                newMessages.add(message.copy(id = document.id))
                            }
                        } catch (e: Exception) {
                            Log.e("ChatActivity", "Error parsing message", e)
                        }
                    }

                    updateMessagesList(newMessages)
                }
            }
        }
    }

    private fun updateMessagesList(newMessages: ArrayList<ChatMessage>) {
        messagesList.clear()
        messagesList.addAll(newMessages)
        chatMessagesAdapter.notifyDataSetChanged()

        // Scroll to bottom for new messages
        if (messagesList.isNotEmpty()) {
            rvChatMessages.scrollToPosition(messagesList.size - 1)
        }
    }

    private fun sendMessage() {
        val messageText = etMessage.text.toString().trim()

        if (messageText.isEmpty() || currentUser == null || chatRoom == null) {
            return
        }

        val message = ChatMessage(
            senderId = currentUser!!.id!!,
            senderName = currentUser!!.name!!,
            senderImage = currentUser!!.image ?: "",
            message = messageText,
            timestamp = System.currentTimeMillis(),
            messageType = "text"
        )

        // Clear the input field immediately for better UX
        etMessage.setText("")
        ibSendMessage.isEnabled = false

        // Send message to Firestore
        FirestoreClass().sendChatMessage(this, chatRoom!!.id, message)
    }

    fun onMessageSent() {
        // Message sent successfully - no additional action needed
        // The real-time listener will update the UI
    }

    fun onMessageSendFailed() {
        Toast.makeText(this, "Failed to send message", Toast.LENGTH_SHORT).show()
    }

    fun setCurrentUser(user: User) {
        currentUser = user
    }

    fun populateMessagesList(messages: ArrayList<ChatMessage>) {
        updateMessagesList(messages)
    }

    fun onMessagesLoadFailed() {
        Toast.makeText(this, "Failed to load messages", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Remove real-time listener to prevent memory leaks
        messagesListener?.remove()
    }

    override fun onResume() {
        super.onResume()
        // Mark messages as read when user opens the chat
        if (chatRoom != null && currentUser != null) {
            FirestoreClass().markMessagesAsRead(chatRoom!!.id, currentUser!!.id!!)
        }
    }
}