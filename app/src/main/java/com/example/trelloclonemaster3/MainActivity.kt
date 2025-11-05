package com.example.trelloclonemaster3

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.trelloclonemaster3.activities.*
import com.example.trelloclonemaster3.adapters.BoardItemAdapter
import com.example.trelloclonemaster3.firebase.FirestoreClass
import com.example.trelloclonemaster3.model.Board
import com.example.trelloclonemaster3.model.User
import com.example.trelloclonemaster3.utils.Constants
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.example.trelloclonemaster3.R
import com.example.trelloclonemaster3.utils.NotificationBadgeHelper
import com.example.trelloclonemaster3.utils.TestNotificationHelper
import com.example.trelloclonemaster3.utils.NotificationDebugHelper



@Suppress("DEPRECATION")
class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {

    companion object{
        const val MY_PROFILE_REQUEST_CODE : Int = 11
        const val CREAT_BOARD_REQUEST_CODE: Int = 12
    }

    private lateinit var mUserName: String
    private lateinit var notificationBadgeHelper: NotificationBadgeHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupActionBar()

        FirestoreClass().loadUserData(this,true)

        val navVIew = findViewById<NavigationView>(R.id.nav_view)
        navVIew.setNavigationItemSelectedListener(this)

        // Setup notification badge helper
        notificationBadgeHelper = NotificationBadgeHelper(this, navVIew, this)

        val floatingActionButton = findViewById<FloatingActionButton>(R.id.fabAddBoard)
        floatingActionButton.setOnClickListener {
            val intent = Intent(this,CreatBoardActivity::class.java)
            intent.putExtra(Constants.NAME,mUserName)
            startActivityForResult(intent, CREAT_BOARD_REQUEST_CODE)
        }

        // Long click to debug notifications (for testing)
        floatingActionButton.setOnLongClickListener {
            NotificationDebugHelper.testCompleteNotificationFlow(this)
            true
        }
    }

    private fun setupActionBar(){
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar_main_activity)
        setSupportActionBar(toolbar)
        toolbar.setNavigationIcon(R.drawable.ic_action_navigation_menu)
        toolbar.setNavigationOnClickListener {
            toogleDrawer()
        }
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(false)
    }

    private fun toogleDrawer(){
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START)
        }else{
            drawerLayout.openDrawer(GravityCompat.START)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)

        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            // 1. Nếu Drawer đang mở, chỉ đóng Drawer.
            drawerLayout.closeDrawer(GravityCompat.START)
        }else{
            // 2. Nếu Drawer đã đóng, gọi logic thoát ứng dụng (kiểm tra nhấn Back lần 2)
            // LƯU Ý: Nếu logic doubBackToExit() xác nhận đã đến lúc thoát (nghĩa là đã nhấn lần 2),
            // nó phải gọi super.onBackPressed() để kết thúc Activity.

            // Giả định: doubBackToExit() là logic kiểm tra và chỉ gọi super.onBackPressed()
            // khi nhấn lần thứ hai. Nếu chưa nhấn lần hai, nó hiển thị Toast.

            doubBackToExit()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
        when(item.itemId){
            R.id.nav_my_profile -> {
                startActivityForResult(Intent(this,MyProfileActivity::class.java), MY_PROFILE_REQUEST_CODE)
            }
            R.id.nav_my_tasks -> {
                startActivity(Intent(this, MyTasksActivity::class.java))
            }
            R.id.nav_chat -> {
                startActivity(Intent(this, ChatRoomsActivity::class.java))
            }
            R.id.nav_notifications -> {
                startActivity(Intent(this, NotificationsActivity::class.java))
            }
            R.id.nav_find_projects -> {
                startActivity(Intent(this, FindProjectsActivity::class.java))
            }
            R.id.nav_sign_out -> {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this,IntroActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == MY_PROFILE_REQUEST_CODE){
            FirestoreClass().loadUserData(this)
        }else if (resultCode == RESULT_OK && requestCode == CREAT_BOARD_REQUEST_CODE){
            FirestoreClass().getBoardList(this)
        }else{
            Log.e("message","Pressed Back button")
        }
    }

    fun updateNavigationUserDetail(user: User, readBoardList: Boolean){
        val userImage = findViewById<ImageView>(R.id.iv_user_image)
        val userName = findViewById<TextView>(R.id.tv_username)

        mUserName = user.name!!

        Glide
                .with(this)
                .load(user.image)
                .centerCrop()
                .placeholder(R.drawable.ic_user_place_holder)
                .into(userImage)

        userName.text = user.name

        if(readBoardList){
            showCustomProgressBar()
            FirestoreClass().getBoardList(this)

            // Setup notification badge with current user ID
            notificationBadgeHelper.setupNotificationBadge(FirestoreClass().getCurrentUserID())

            // Initialize and clean up chat rooms for existing boards
            FirestoreClass().initializeChatRoomsForExistingBoards(this)

            // Clean up any duplicate chat rooms manually
            cleanupAllDuplicateChatRooms()
        }
    }

    /**
     * Clean up duplicate chat rooms for all user's boards
     */
    private fun cleanupAllDuplicateChatRooms() {
        Log.d("MainActivity", "Starting cleanup of duplicate chat rooms")
        FirestoreClass().cleanupAllUserDuplicateChatRooms(this)
    }

    fun populateBoardListInUI(boardList: ArrayList<Board>){
        val tvNoUnit = findViewById<TextView>(R.id.tv_no_unit)
        val rvUnitList = findViewById<RecyclerView>(R.id.rv_boards_list)

        if (boardList.size > 0){

            rvUnitList.visibility = View.VISIBLE
            tvNoUnit.visibility = View.GONE

            rvUnitList.layoutManager = LinearLayoutManager(this)
            rvUnitList.setHasFixedSize(true)

            val adapter = BoardItemAdapter(this,boardList)
            rvUnitList.adapter = adapter

            adapter.setOnClickListener(object: BoardItemAdapter.OnClickListener{
                override fun onClick(position: Int, model: Board) {
                    val intent = Intent(this@MainActivity,TaskListActivity::class.java)
                    intent.putExtra(Constants.DOCUMENT_ID,model.documentId)
                    startActivity(intent)
                }
            })
        }else{
            rvUnitList.visibility = View.GONE
            tvNoUnit.visibility = View.VISIBLE
        }
    }
}