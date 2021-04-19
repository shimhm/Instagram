package com.example.instagram

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    var googleSigninClient: GoogleSignInClient? = null
    var GOOGLE_LOGIN_CODE = 9001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // firebase 권한
        auth = FirebaseAuth.getInstance()

        // email_login_btn을 click
        email_login_button.setOnClickListener {
            emailLogin()
        }


        google_signin_button.setOnClickListener {
            // First step
            Log.d("LOGIN_GOOGLE", "first step")
            googleLogin()
        }
        // google 계정으로 login
        var gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSigninClient = GoogleSignIn.getClient(this, gso)

    }

    override fun onStart() {
        super.onStart()
        moveMainPage(auth?.currentUser)
    }

    fun googleLogin() {
        val signInIntent = googleSigninClient?.signInIntent

        // 이동된 Activity로부터 값을 가져올 때 -> startActivityForResult
        startActivityForResult(signInIntent, GOOGLE_LOGIN_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GOOGLE_LOGIN_CODE) {
            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            //
            Log.d("LOGIN_GOOGLE", "second step-2")
            if (result!!.isSuccess) {
                val account = result.signInAccount

                // Second step
                Log.d("LOGIN_GOOGLE", "second step")
                firebaseAuthWithGoogle(account)

            }
        }
    }


    fun firebaseAuthWithGoogle(account: GoogleSignInAccount?) {
        val credential = GoogleAuthProvider.getCredential(account?.idToken, null)
        auth.signInWithCredential(credential)
            // 이하 SigninEmail과 동일
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    //Login
                    Log.d("LOGIN_GOOGLE", "login")
                    moveMainPage(task.result?.user)
                } else {
                    //Show the error message
                    Log.d("LOGIN_GOOGLE", "exception")
                    Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()
                }
            }
    }

    // email_login_btn을 click
    // email로 register
    fun signinAndsignup() {
        auth.createUserWithEmailAndPassword(
            email_edittext.text.toString(),
            password_edittext.text.toString()
        ).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // email로 register 성공한 경우
                    // creating a user account
                    moveMainPage(task.result?.user)
                } else if (task.exception?.message.isNullOrEmpty()) {
                    // ???????????????????????????????????????????????????????//
                    // Show the error message
                    Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()
                } else {
                    // email이 이미 존재하는 경우
                    // Login if you have account
                    signinEmail()
                }
            }
    }

    fun emailLogin(){
        if(email_edittext.text.toString().isNullOrEmpty() || password_edittext.text.toString().isNullOrEmpty()){
            Toast.makeText(this, getString(R.string.signout_fail_null),Toast.LENGTH_SHORT).show()
        }else{
            signinAndsignup()
        }
    }

    // email로 login
    fun signinEmail() {
        auth.signInWithEmailAndPassword(
            email_edittext.text.toString(),
            password_edittext.text.toString()
        )
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    //Login
                    moveMainPage(task.result?.user)
                } else {
                    //Show the error message
                    Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()
                }
            }
    }

    fun moveMainPage(user: FirebaseUser?) {
        if (user != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}
