package com.example.firebasephoneauth

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.example.firebasephoneauth.databinding.ActivityMainBinding
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    private val TAG = "MainActivity"

    // if code sending failed, will used to resend it
    private lateinit var forceResendingToken: PhoneAuthProvider.ForceResendingToken

    private lateinit var mCallback: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    private var mVerificationId: String? = null
    private lateinit var firebaseAuth: FirebaseAuth

    //progress dailog
    private lateinit var progressDailog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        binding.otp.visibility = View.GONE
        binding.resend.visibility = View.GONE
        binding.submit.visibility = View.GONE

        binding.phNum.visibility = View.VISIBLE
        binding.btn.visibility = View.VISIBLE

        // init firebase
        firebaseAuth = FirebaseAuth.getInstance()

        //init, setup progress dailogue
        progressDailog = ProgressDialog(this)
        progressDailog.setTitle("Please wait")
        progressDailog.setCanceledOnTouchOutside(false)

        // verification state change, verification completed, verification failed, etc
        mCallback = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks(){

            override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {
                signInWithPhoneAuthCredentials(phoneAuthCredential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                progressDailog.dismiss()
                Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_LONG).show()
            }

            override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                Log.d(TAG, "onCodeSent: $verificationId")
                mVerificationId = verificationId
                forceResendingToken = token
                progressDailog.dismiss()

                binding.phNum.visibility = View.GONE
                binding.btn.visibility = View.GONE
                binding.otp.visibility = View.VISIBLE
                binding.submit.visibility = View.VISIBLE
            }
        }

        binding.btn.setOnClickListener {

            val phone = binding.phNum.text.toString().trim()
            //validate phomne number
            if (TextUtils.isEmpty(phone)){
                Toast.makeText(this, "Please enter phone number", Toast.LENGTH_LONG).show()
            }else{
                startPhoneNumberVerification(phone)
            }

        }

        binding.resend.setOnClickListener {
            val phone = binding.phNum.text.toString().trim()
            //validate phomne number
            if (TextUtils.isEmpty(phone)){
                Toast.makeText(this, "Please enter phone number", Toast.LENGTH_LONG).show()
            }else{
                resendVerificationCode(phone, forceResendingToken)
            }
        }

        binding.submit.setOnClickListener {
            val code = binding.otp.text.toString().trim()

            if (TextUtils.isEmpty(code)){
                Toast.makeText(this, "Please enter otp", Toast.LENGTH_LONG).show()
            }else{
                mVerificationId?.let { it1 -> verifyPhoneNumberWithCode(it1, code) }

            }

        }

    }

    private fun startPhoneNumberVerification(number: String){
        progressDailog.setTitle("Veryfying phone number")
        progressDailog.show()

        val option = PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber(number)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(mCallback)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(option)
    }

    private fun resendVerificationCode(number: String, token:PhoneAuthProvider.ForceResendingToken){
        progressDailog.setTitle("Veryfying phone number")
        progressDailog.show()

        val option = PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber(number)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(mCallback)
            .setForceResendingToken(token)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(option)

    }

    private fun verifyPhoneNumberWithCode(verificationId: String, code: String){
        progressDailog.setMessage("Verifying code")
        progressDailog.show()

        val credential = PhoneAuthProvider.getCredential(verificationId, code)
        signInWithPhoneAuthCredentials(credential)
    }

    private fun signInWithPhoneAuthCredentials(credential: PhoneAuthCredential){
        progressDailog.setMessage("Logging In")

        firebaseAuth.signInWithCredential(credential)
            .addOnSuccessListener {
                //login success
                progressDailog.dismiss()
                val phone = firebaseAuth.currentUser?.phoneNumber
                Toast.makeText(this, "Login with $phone", Toast.LENGTH_LONG).show()

                startActivity(Intent(this, HomeActivity::class.java))
            }
            .addOnFailureListener {e ->
                //login fail
                progressDailog.dismiss()
                Toast.makeText(this,e.message, Toast.LENGTH_LONG).show()
            }

    }

}