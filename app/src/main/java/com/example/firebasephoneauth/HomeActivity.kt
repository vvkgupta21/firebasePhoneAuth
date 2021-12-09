package com.example.firebasephoneauth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.example.firebasephoneauth.databinding.ActivityHomeBinding
import com.google.firebase.auth.FirebaseAuth

class HomeActivity : AppCompatActivity() {

    lateinit var binding: ActivityHomeBinding

    private lateinit var firebase: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_home)
        firebase = FirebaseAuth.getInstance()
        binding.btn.setOnClickListener {
            firebase.signOut()
            checkUser()
        }
    }

    private fun checkUser(){
        //get current user
        val firebaseUser = firebase.currentUser
        if (firebaseUser == null){
            //logoput
            startActivity(Intent(this,MainActivity::class.java))
        }else{
            val phone = firebaseUser.phoneNumber
            binding.text.text = phone.toString()
        }

    }
}