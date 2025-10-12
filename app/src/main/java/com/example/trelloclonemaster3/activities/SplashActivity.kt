package com.example.trelloclonemaster3.activities

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.view.WindowManager
import android.widget.TextView
import com.example.trelloclonemaster3.MainActivity
import com.example.trelloclonemaster3.R
import com.example.trelloclonemaster3.firebase.FirestoreClass

@Suppress("DEPRECATION")
class SplashActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
        )

        val typeFace: Typeface = Typeface.createFromAsset(assets,"Raleway-Bold.ttf")
        val tvAppName = findViewById<TextView>(R.id.tv_app_name)

        tvAppName.typeface = typeFace

        Handler().postDelayed(
            {

                val currentUserID = FirestoreClass().getCurrentUserID()
                if (currentUserID.isNotEmpty()) {
                    startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                } else {
                    startActivity(Intent(this@SplashActivity, IntroActivity::class.java))
                }
                finish()
//                startActivity(Intent(this,IntroActivity::class.java))
//                finish()
            },2500
        )
    }
}