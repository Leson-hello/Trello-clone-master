package com.example.trelloclonemaster3.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.Log
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.example.trelloclonemaster3.MainActivity

import com.example.trelloclonemaster3.R
import com.example.trelloclonemaster3.firebase.FirestoreClass
import com.example.trelloclonemaster3.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

@Suppress("DEPRECATION")
class SignUpActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
        )

        setupActionBar()

    }

    private fun setupActionBar(){
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar_sign_up_activity)
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_black_back_icon_24dp)
        }
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        val btnSignUp = findViewById<Button>(R.id.btn_sign_up)
        btnSignUp.setOnClickListener {
            registerUser()
        }
    }

    /**
     * Phương thức này được gọi từ FirestoreClass sau khi User được lưu thành công vào Firestore.
     * Logic: Ẩn tiến trình, chờ 2 giây (theo yêu cầu), sau đó chuyển sang Activity chính.
     */
    fun userRegisteredSucess(){
        // Bước 1: Log, Toast và Ẩn hộp thoại ngay khi Firebase/Firestore hoàn tất.
        Log.e("Login","registered Successful")
        Toast.makeText(this,"registered Successful",Toast.LENGTH_SHORT).show()
        hideCustomProgressDialog()

        // Bước 2: Dùng Handler để tạo độ trễ 2 giây (2000ms) trước khi chuyển màn hình.
        Handler(Looper.getMainLooper()).postDelayed({
            // Chuyển Activity sau khi chờ 2000ms (2 giây)
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, 2000) // <-- Độ trễ cố định 2000ms
    }

    private fun registerUser(){
        val name: String = findViewById<TextView>(R.id.et_name).text.toString().trim {it <= ' '}
        val eMail: String = findViewById<TextView>(R.id.et_email).text.toString().trim { it <= ' ' }
        val password: String = findViewById<TextView>(R.id.et_password).text.toString().trim { it <= ' ' }

        if(validateForm(name,eMail,password)){
            showCustomProgressBar()

            FirebaseAuth.getInstance().createUserWithEmailAndPassword(eMail,password).addOnCompleteListener {
                    task ->
                if (task.isSuccessful){
                    val firebaseUser: FirebaseUser = task.result!!.user!!
                    val registeredEmail = firebaseUser.email

                    // Tạo đối tượng User và lưu vào Firestore
                    val user = User(firebaseUser.uid,name,eMail)
                    Log.e("Sign up","$name is registered with $registeredEmail ==> $user")
                    FirestoreClass().registerUser(this,user)

                    // Lưu ý: hideCustomProgressDialog() sẽ được gọi trong userRegisteredSucess()

                }else {
                    // THẤT BẠI: CẦN ẨN HỘP THOẠI VÀ HIỂN THỊ LỖI CHI TIẾT
                    hideCustomProgressDialog() // <--- SỬA: ẨN HỘP THOẠI KHI THẤT BẠI

                    val errorMessage = task.exception?.message ?: "Unknown registration error"

                    Log.e("Sign Up", "Registration failed: $errorMessage")
                    Toast.makeText(
                        this,
                        "Registration Failed: $errorMessage",
                        Toast.LENGTH_LONG
                    ).show()

                    // KHÔNG NÊN GỌI finish() ở đây, để người dùng sửa lại thông tin
                }
            }
        }
    }

    // Sửa logic trong validateForm để hiển thị đúng thông báo lỗi
    private fun validateForm(name: String,eMail: String,passoward: String): Boolean{
        return when{
            TextUtils.isEmpty(name) -> {
                showErrorSnackBar("Please Enter a Name")
                false
            }
            TextUtils.isEmpty(eMail) -> {
                showErrorSnackBar("Please Enter an Email") // Sửa thông báo lỗi
                false
            }
            TextUtils.isEmpty(passoward) -> {
                showErrorSnackBar("Please Enter a Password") // Sửa thông báo lỗi
                false
            }else -> {
                true
            }
        }
    }
}