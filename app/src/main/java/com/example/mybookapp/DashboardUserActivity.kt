package com.example.mybookapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.mybookapp.databinding.ActivityDashboardUserBinding
import com.google.firebase.auth.FirebaseAuth

class
DashboardUserActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardUserBinding

    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()

        binding.logoutBtn.setOnClickListener {
            firebaseAuth.signOut()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun checkUser() {
        val firebaseUser = firebaseAuth.currentUser
        // not login user can stay in user dahsboard
        if(firebaseUser == null) {
            binding.subTitleTv.text = "Not logged in"
        }
        else {
            val email = firebaseUser.email
            binding.subTitleTv.text = email
        }
    }
}