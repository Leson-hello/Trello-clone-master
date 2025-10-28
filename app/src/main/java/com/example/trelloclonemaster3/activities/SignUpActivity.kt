package com.example.trelloclonemaster3.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.Log
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.trelloclonemaster3.MainActivity
import com.example.trelloclonemaster3.R
import com.example.trelloclonemaster3.firebase.FirestoreClass
import com.example.trelloclonemaster3.model.User
import com.google.android.material.textfield.TextInputLayout
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

    fun userRegisteredSucess(){
        Log.e("Login","registered Successful")
        Toast.makeText(this,"registered Successful",Toast.LENGTH_SHORT).show()
        hideCustomProgressDialog()

        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, 2000)
    }

    private fun registerUser(){
        val name: String = findViewById<EditText>(R.id.et_name).text.toString().trim {it <= ' '}
        val eMail: String = findViewById<EditText>(R.id.et_email).text.toString().trim { it <= ' ' }
        val password: String = findViewById<EditText>(R.id.et_password).text.toString().trim { it <= ' ' }
        val confirmPassword: String = findViewById<EditText>(R.id.et_confirm_password).text.toString().trim { it <= ' ' }

        val tilName = findViewById<TextInputLayout>(R.id.til_name)
        val tilEmail = findViewById<TextInputLayout>(R.id.til_email)
        val tilPassword = findViewById<TextInputLayout>(R.id.til_password)
        val tilConfirmPassword = findViewById<TextInputLayout>(R.id.til_confirm_password)


        if(validateForm(name, eMail, password, confirmPassword, tilName, tilEmail, tilPassword, tilConfirmPassword)){
            showCustomProgressBar()

            FirebaseAuth.getInstance().createUserWithEmailAndPassword(eMail,password).addOnCompleteListener {
                    task ->
                if (task.isSuccessful){
                    val firebaseUser: FirebaseUser = task.result!!.user!!
                    val registeredEmail = firebaseUser.email

                    val user = User(firebaseUser.uid,name,eMail)
                    Log.e("Sign up","$name is registered with $registeredEmail ==> $user")
                    FirestoreClass().registerUser(this,user)


                }else {
                    hideCustomProgressDialog()

                    val errorMessage = task.exception?.message ?: "Unknown registration error"

                    Log.e("Sign Up", "Registration failed: $errorMessage")
                    Toast.makeText(
                        this,
                        "Registration Failed: $errorMessage",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun validateForm(
        name: String,
        eMail: String,
        passoward: String,
        confirmPassword: String,
        tilName: TextInputLayout,
        tilEmail: TextInputLayout,
        tilPassword: TextInputLayout,
        tilConfirmPassword: TextInputLayout
    ): Boolean{
        tilName.error = null
        tilEmail.error = null
        tilPassword.error = null
        tilConfirmPassword.error = null

        return when{
            TextUtils.isEmpty(name) -> {
                tilName.error = "Please Enter a Name"
                false
            }
            TextUtils.isEmpty(eMail) -> {
                tilEmail.error = "Please Enter an Email"
                false
            }
            TextUtils.isEmpty(passoward) -> {
                tilPassword.error = "Please Enter a Password"
                false
            }
            TextUtils.isEmpty(confirmPassword) -> {
                tilConfirmPassword.error = "Please confirm your password"
                false
            }
            passoward != confirmPassword -> {
                tilConfirmPassword.error = "Password and Confirm Password do not match"
                false
            }
            else -> {
                true
            }
        }
    }
}