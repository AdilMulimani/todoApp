package com.adilmulimani.todoApp.descriptionactivity

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.adilmulimani.todoApp.MainActivity2
import com.adilmulimani.todoApp.databinding.ActivityDescriptionBinding

class DescriptionActivity : AppCompatActivity() {
    private lateinit var binding :ActivityDescriptionBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        Thread.sleep(3000)
        binding = ActivityDescriptionBinding.inflate(layoutInflater)
        setContentView(binding.root)

     binding.nextButton.setOnClickListener {
         startActivity(Intent(this@DescriptionActivity, AIDescriptionActivity::class.java))
     }

    }


    // onStart() is called just before the activity becomes visible to the user,
    // so it provides an early opportunity to check for the token.
    override fun onStart() {
        super.onStart()

        val todoPreference : SharedPreferences? = getSharedPreferences("user_todo", MODE_PRIVATE)
        //if to do preference contains token then we directly direct to main activity
        if (todoPreference != null && todoPreference.contains("token")) {
            startActivity(Intent(this@DescriptionActivity, MainActivity2::class.java))
            finish()
        }
    }
}