package com.adilmulimani.todoApp.descriptionactivity

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.adilmulimani.todoApp.LoginActivity
import com.adilmulimani.todoApp.MainActivity2
import com.adilmulimani.todoApp.databinding.ActivityFocusDescriptionBinding

class FocusDescriptionActivity : AppCompatActivity() {
    private lateinit var binding:ActivityFocusDescriptionBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFocusDescriptionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.nextButton.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        binding.backButton.setOnClickListener {
            startActivity(Intent(this, AIDescriptionActivity::class.java))
        }


    }

    // onStart() is called just before the activity becomes visible to the user,
    // so it provides an early opportunity to check for the token.
    override fun onStart() {
        super.onStart()

        val todoPreference : SharedPreferences? = getSharedPreferences("user_todo", MODE_PRIVATE)
        //if to do preference contains token then we directly direct to main activity
        if (todoPreference != null && todoPreference.contains("token")) {
            startActivity(Intent(this, MainActivity2::class.java))
            finish()
        }
    }
}