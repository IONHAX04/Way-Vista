package com.example.wayvistaanandroidapplication.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.wayvistaanandroidapplication.databinding.ActivitySignInBinding
import com.example.wayvistaanandroidapplication.utilities.Actions
import com.example.wayvistaanandroidapplication.utilities.UserDetails

class SignInActivity : AppCompatActivity() {

    private lateinit var activitySignInBinding: ActivitySignInBinding
    private lateinit var actions: Actions
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var resultData: List<UserDetails>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activitySignInBinding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(activitySignInBinding.root)

        sharedPreferences = getSharedPreferences("data", Context.MODE_PRIVATE)
        actions = ViewModelProvider(this)[Actions::class.java]

        val USER_ID: Int = sharedPreferences.getInt("USER_ID", 0)
        if (USER_ID !=0){
            MainActivity.userId = USER_ID
            startActivity(Intent(applicationContext, MainActivity::class.java))
            finish()
        }
        setClickListener()
    }

    private fun setClickListener() {
        activitySignInBinding.createNew.setOnClickListener {
            startActivity(Intent(applicationContext, SignUpActivity::class.java))
            finish()
        }

        activitySignInBinding.buttonSignIn.setOnClickListener {
            if (isValidSignInDetails()) {
                signIn()
            }
        }
    }

    private fun signIn() {
        loading(true)
        val email = activitySignInBinding.inputEmail.text.toString()
        val password = activitySignInBinding.inputPassword.text.toString()
        actions.signIn(email, password)
        actions.loginData.observe(this) {
            resultData = it
            if (resultData.isNotEmpty()) {
                MainActivity.userId = resultData[0].id
                actions.updateStatus(resultData[0].id, true)
                val editor: SharedPreferences.Editor = sharedPreferences.edit()
                editor.putInt("USER_ID", resultData[0].id)
                editor.putString("USERNAME", resultData[0].name)
                editor.putString("EMAIL", resultData[0].email)
                editor.putString("USERIMG", resultData[0].image.toString())
                editor.apply()
                editor.commit()
                startActivity(Intent(applicationContext, MainActivity::class.java))
                finish()
            } else {
                Toast.makeText(
                    this,
                    "Email or Password doesn't match.\nKindly try again",
                    Toast.LENGTH_SHORT
                ).show()
            }
            loading(false)
        }
    }

    private fun isUserLoggedIn(): Boolean {
        val userDetails = UserDetails()
        return sharedPreferences.getBoolean(userDetails.isLoggedIn.toString(), true)
    }


    private fun loading(isLoading: Boolean) {
        if (isLoading) {
            activitySignInBinding.buttonSignIn.visibility = View.VISIBLE
            activitySignInBinding.progressBar.visibility = View.INVISIBLE
        } else {
            activitySignInBinding.buttonSignIn.visibility = View.INVISIBLE
            activitySignInBinding.buttonSignIn.visibility = View.VISIBLE
        }
    }

    private fun isValidSignInDetails(): Boolean {
        val email = activitySignInBinding.inputEmail.text.toString()
        val password = activitySignInBinding.inputPassword.text.toString()

        if (email.trim().isEmpty()) {
            showToast("Enter email address")
            return false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showToast("Enter a valid email address")
            return false
        } else if (password.trim().isEmpty()) {
            showToast("Enter password")
            return false
        }
        return true
    }

    private fun showToast(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
    }
}
