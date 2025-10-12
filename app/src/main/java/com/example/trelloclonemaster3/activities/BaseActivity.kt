package com.example.trelloclonemaster3.activities

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.trelloclonemaster3.R
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth

@Suppress("DEPRECATION")
open class BaseActivity : AppCompatActivity() {

    private var mDoubleBackToExit = false
    private lateinit var mProgressDialog: Dialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base)
    }

    fun showCustomProgressBar(){
        mProgressDialog = Dialog(this)

        mProgressDialog.setContentView(R.layout.dialog_custom_progress)

        mProgressDialog.show()
    }

    fun hideCustomProgressDialog(){
        mProgressDialog.dismiss()
    }

    fun getCurrentUserId(): String{
        return FirebaseAuth.getInstance().currentUser!!.uid
    }

    private var doubleBackToExitPressedOnce = false

    fun doubBackToExit() {
        if (doubleBackToExitPressedOnce) {
            // Nếu nhấn lần 2, thực hiện hành vi mặc định: ĐÓNG Activity
            super.onBackPressed() // GỌI HÀM GỐC TẠI ĐÂY
            return
        }

        this.doubleBackToExitPressedOnce = true
        Toast.makeText(this,
            resources.getString(R.string.please_click_back_again_to_exit), // Giả định ID String của bạn
            Toast.LENGTH_SHORT
        ).show()

        // Đặt lại biến sau 2 giây
        Handler(Looper.getMainLooper()).postDelayed({
            doubleBackToExitPressedOnce = false
        }, 2000)
    }
    fun showErrorSnackBar(message: String){
        val snackBar = Snackbar.make(findViewById(android.R.id.content),message,Snackbar.LENGTH_LONG)
        val snackBarView = snackBar.view
        snackBarView.setBackgroundColor(ContextCompat.getColor(this,R.color.snackBar_error_color))

        snackBar.show()
    }


}