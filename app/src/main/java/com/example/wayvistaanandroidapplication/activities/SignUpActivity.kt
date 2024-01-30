package com.example.wayvistaanandroidapplication.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import com.example.wayvistaanandroidapplication.databinding.ActivitySignUpBinding
import com.example.wayvistaanandroidapplication.utilities.Actions
import com.example.wayvistaanandroidapplication.utilities.PreferenceManager
import com.example.wayvistaanandroidapplication.utilities.UserDetails
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.io.InputStream

class SignUpActivity : AppCompatActivity() {

    private lateinit var activitySignUpBinding: ActivitySignUpBinding
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var actions: Actions
    private lateinit var userDetails: UserDetails
    private lateinit var sharedPreferences: SharedPreferences

    private var encodedImg: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activitySignUpBinding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(activitySignUpBinding.root)
        preferenceManager = PreferenceManager(applicationContext)
        actions = ViewModelProvider(this)[Actions::class.java]
        sharedPreferences = getSharedPreferences("data", Context.MODE_PRIVATE)
        userDetails = UserDetails()
        setOnListeners()
    }

    private fun setOnListeners() {
        activitySignUpBinding.createNew.setOnClickListener { v ->
            startActivity(Intent(applicationContext, SignInActivity::class.java))
            finish()
        }
        activitySignUpBinding.buttonSignIn.setOnClickListener {
            if (isValidSignUp()) {
                signUp()
            }
        }
        activitySignUpBinding.layoutImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            pickImage.launch(intent)
        }

    }

    private fun showToast(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
    }


    private fun signUp() {
        loading(true)
        val name = activitySignUpBinding.inputName.text.toString()
        val email = activitySignUpBinding.inputEmail.text.toString()
        val password = activitySignUpBinding.inputPassword.text.toString()
        encodedImg?.let { actions.signUp(it, name, email, password) }
        actions.userId.observe(this) {
            MainActivity.userId = it
            val editor: SharedPreferences.Editor = sharedPreferences.edit()
            editor.putInt("USER_ID", it)
            editor.putString("USERNAME", name)
            editor.putString("EMAIL", email)
            editor.putString("USER_IMAGE", encodedImg)
            editor.apply()
            editor.commit()
        }
        loading(false)
        val intent = Intent(applicationContext, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
    }


    private fun encodeImage(bitmap: Bitmap?): String {
        if (bitmap == null) {
            showToast("Bitmap is null")
            return ""
        }
        try {
            val previewWidth = 150
            val previewHeight = bitmap.height * previewWidth / bitmap.width
            val previewBitmap =
                Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false)

            val byteArrayOutputStream = ByteArrayOutputStream()
            previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream)
            val byteArray: ByteArray = byteArrayOutputStream.toByteArray()

            val base64String = Base64.encodeToString(byteArray, Base64.DEFAULT)
            return base64String
        } catch (e: Exception) {
            e.printStackTrace()
            showToast("Failed to encode image")
            return ""
        }
    }


    private val pickImage = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let { imageUri ->
                try {
                    val inputStream: InputStream? =
                        contentResolver.openInputStream(imageUri)
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    activitySignUpBinding.imageProfilePic.setImageBitmap(bitmap)
                    activitySignUpBinding.texImg.visibility = View.GONE
                    encodedImg = encodeImage(bitmap)
                } catch (e: FileNotFoundException) {
                    showToast("Error in sign up")
                    e.printStackTrace()
                }
            }
        }
    }


    private fun isValidSignUp(): Boolean {
        if (encodedImg == null) {
            showToast("Select Profile Image")
            return false
        } else if (activitySignUpBinding.inputName.text.toString().isEmpty()) {
            showToast("Enter Name")
            return false
        } else if (activitySignUpBinding.inputEmail.text.toString().trim().isEmpty()) {
            showToast("Enter Email ID")
            return false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(activitySignUpBinding.inputEmail.text.toString())
                .matches()
        ) {
            showToast("Enter Valid Email ID")
            return false
        } else if (activitySignUpBinding.inputPassword.text.toString().trim().isEmpty()) {
            showToast("Enter Password")
            return false
        } else if (activitySignUpBinding.confirmPassword.text.toString().trim().isEmpty()) {
            showToast("Confirm your password")
            return false
        } else if (!activitySignUpBinding.inputPassword.text.toString()
                .equals(activitySignUpBinding.confirmPassword.text.toString())
        ) {
            showToast("Password & Confirm Password must be same")
            return false
        } else {
            return true
        }
    }

    private fun loading(isLoading: Boolean) {
        if (isLoading) {
            activitySignUpBinding.buttonSignIn.visibility = View.VISIBLE
            activitySignUpBinding.progressBar.visibility = View.INVISIBLE
        } else {
            activitySignUpBinding.buttonSignIn.visibility = View.INVISIBLE
            activitySignUpBinding.buttonSignIn.visibility = View.VISIBLE
        }
    }
}