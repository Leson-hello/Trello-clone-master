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

//Từ khóa open trong Kotlin cho phép các lớp khác có thể kế thừa (inherit) từ lớp này.
// Nếu không có open, lớp này sẽ là final theo mặc định và không thể được kế thừa.
@Suppress("DEPRECATION")


/*
* : AppCompatActivity():
Điều này có nghĩa là BaseActivity kế thừa tất cả các tính năng và hành vi của AppCompatActivity,
* lớp Activity tiêu chuẩn của Android giúp đảm bảo tính tương thích trên các phiên bản hệ điều hành khác nhau.

* */




open class BaseActivity : AppCompatActivity() {

    private var mDoubleBackToExit = false

    /*private: Biến này chỉ có thể được truy cập bên trong BaseActivity.
    lateinit var: Báo cho Kotlin biết rằng biến mProgressDialog sẽ được khởi tạo sau này (trong hàm showCustomProgressBar), trước khi nó được sử dụng lần đầu tiên.
    Dialog: Đây là một đối tượng Dialog sẽ được dùng để hiển thị một cửa sổ pop-up (trong trường hợp này là thanh tiến trình đang tải).
    */
    private lateinit var mProgressDialog: Dialog


    //super.onCreate(savedInstanceState): Gọi đến hàm onCreate của lớp cha (AppCompatActivity)
    // để thực hiện các thiết lập cần thiết.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_base): Gắn giao diện người dùng được định nghĩa trong file res/layout/activity_base.xml cho Activity này
        setContentView(R.layout.activity_base)
    }

    fun showCustomProgressBar() {
        mProgressDialog = Dialog(this)

        mProgressDialog.setContentView(R.layout.dialog_custom_progress)

        mProgressDialog.show()
    }

    fun hideCustomProgressDialog() {
        mProgressDialog.dismiss()
    }

    //Đây là một hàm tiện ích để lấy ID duy nhất (UID) của người dùng hiện tại đã đăng nhập
    // thông qua Firebase Authentication.
    fun getCurrentUserId(): String {
        //Dấu !! (non-null asserted operator) có nghĩa là bạn chắc chắn rằng
        // FirebaseAuth.getInstance().currentUser sẽ không bao giờ null


        // Sử dụng elvis operator (?:) để cung cấp giá trị mặc định
        return FirebaseAuth.getInstance().currentUser?.uid ?: ""
    }


    private var doubleBackToExitPressedOnce = false/*Đây là một biến cờ (flag) kiểu Boolean (chỉ có giá trị true hoặc false).
     Nó được dùng để theo dõi xem người dùng đã nhấn nút "Back" một lần hay chưa.
     Ban đầu, giá trị của nó là false.*/

    fun doubBackToExit() {
        if (doubleBackToExitPressedOnce) {
            // Nếu nhấn lần 2, thực hiện hành vi mặc định: ĐÓNG Activity
            super.onBackPressed() // GỌI HÀM GỐC TẠI ĐÂY
            return
        }

        this.doubleBackToExitPressedOnce = true
        Toast.makeText(
            this,
            resources.getString(R.string.please_click_back_again_to_exit), // Giả định ID String của bạn
            Toast.LENGTH_SHORT
        ).show()

        // Đặt lại biến sau 2 giây
        Handler(Looper.getMainLooper()).postDelayed({
            doubleBackToExitPressedOnce = false
        }, 2000)
    }


    //Hàm này dùng để hiển thị một Snackbar, một thanh thông báo hiện đại hơn Toast, thường xuất hiện ở cuối màn hình.
    //Snackbar.make(...): Tạo một Snackbar với nội dung là message và hiển thị trong một khoảng thời gian dài (LENGTH_LONG).
    //snackBarView.setBackgroundColor(...): Lấy view của Snackbar và tùy chỉnh màu nền của nó thành một màu đỏ (được định nghĩa trong res/values/colors.xml với tên snackBar_error_color) để báo hiệu đây là một thông báo lỗi.
    //snackBar.show(): Hiển thị Snackbar lên màn hình.
    fun showErrorSnackBar(message: String) {
        val snackBar =
            Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG)
        val snackBarView = snackBar.view
        snackBarView.setBackgroundColor(ContextCompat.getColor(this, R.color.snackBar_error_color))

        snackBar.show()
    }

}/*
findViewById(android.R.id.content)
Khi bạn tạo một Activity, hệ thống Android không chỉ hiển thị layout của bạn.
Nó tạo ra một cấu trúc phức tạp hơn bao gồm cả thanh tiêu đề (Action Bar/Toolbar), thanh trạng thái (Status Bar), v.v.
Bên trong cấu trúc đó, có một khu vực đặc biệt được dành riêng để chứa giao diện chính của bạn.
Khu vực này là một FrameLayout và nó luôn có một ID cố định do hệ thống cung cấp là android.R.id.content.*/