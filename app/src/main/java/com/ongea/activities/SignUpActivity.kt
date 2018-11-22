package com.ongea.activities

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.ongea.R
import kotlinx.android.synthetic.main.activity_sign_up.*

class SignUpActivity : AppCompatActivity(), View.OnClickListener {

    private var mFirebaseAuth: FirebaseAuth? = null
    private var mAuthStateListener: FirebaseAuth.AuthStateListener? = null
    private var mFirebaseUser: FirebaseUser? = null
    lateinit var mProgressDialog: ProgressDialog
    private var currentUser: String? = null
    private val PASSWORD = "password"
    private val EMAIL = "email"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        mFirebaseAuth = FirebaseAuth.getInstance()
        mFirebaseUser = mFirebaseAuth?.currentUser


        loginTextView.setOnClickListener(this)
        createUserButton.setOnClickListener(this)

        mAuthStateListener =  FirebaseAuth.AuthStateListener {
            if (mFirebaseUser != null){
              sendVerificationEmail()
            }else{
                // user has not yet created account
            }

        }

        createAuthProgressDialog()

    }

    override fun onStart() {
        super.onStart()
        mFirebaseAuth?.addAuthStateListener { mFirebaseAuth }
    }

    override fun onStop() {
        super.onStop()
        if (mAuthStateListener != null) {
            mFirebaseAuth?.removeAuthStateListener { mFirebaseAuth }
        }
    }

    override fun onClick(v: View?) {
        val id = v?.id

        if (id == R.id.loginTextView) {
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
            finish()
        }

        if (id == R.id.createUserButton) {
            createNewUser()
        }

    }

    /**sign up progress dialog*/
    fun createAuthProgressDialog() {
        mProgressDialog = ProgressDialog(this)
        mProgressDialog.setTitle("Connecting")
        mProgressDialog.setMessage("Authenticating your sign in credentials")
        mProgressDialog.setCancelable(false)
    }


    fun sendVerificationEmail() {
        mAuthStateListener =  FirebaseAuth.AuthStateListener {
            if (mFirebaseUser != null){
                mFirebaseUser?.sendEmailVerification()?.addOnCompleteListener {
                    val msg: String =  " "
                    toast(this, msg,  Toast.LENGTH_SHORT)
                    FirebaseAuth.getInstance().signOut()
                }
            }else{

            }

        }
    }

    fun createNewUser() {
        val email: String = emailEditText.text.toString()
        val password: String = passwordEditText.text.toString()
        val confirmPassword: String = passwordEditText.text.toString()

        // validation for email and password
        val validEmail: Boolean = isValidEmail(email)
        val validPassword: Boolean = isValidPassword(password, confirmPassword)
        if (!validEmail || !validPassword) return

        mProgressDialog.show()

        mFirebaseAuth?.createUserWithEmailAndPassword(email, password)
                ?.addOnCompleteListener {
                    mProgressDialog.dismiss()

                    if (!it.isSuccessful){
                        if (it.exception is FirebaseAuthUserCollisionException){
                            errorRelativeLayout.visibility = View.VISIBLE
                            errorTextView.setText(R.string.error_firebase_collision)

                            errorRelativeLayout.postDelayed(Runnable {
                                errorRelativeLayout.visibility = View.GONE
                            }, 3000)
                        }else{
                            errorRelativeLayout.visibility = View.VISIBLE
                            errorTextView.setText(R.string.error_internet_connection)

                            errorRelativeLayout.postDelayed(Runnable {
                                errorRelativeLayout.visibility = View.GONE
                            }, 3000)
                        }
                    }else{
                        var intent = Intent(this, CreateProfileActivity::class.java)
                        intent.putExtra(this.EMAIL, email)
                        intent.putExtra(this.PASSWORD, password)
                        startActivity(intent)
                        finish()

                    }

                }

    }

    fun toast(context: Context, message: String, length: Int){
        var toast: Toast = Toast.makeText(context, message, length)
        toast.show()
    }

    fun isValidPassword(password: String, confirmPassword: String): Boolean {
        if (password.length < 6) {
            passwordEditText.error = "Please create a password containing at least 6 characters"
            return false
        }else if (!password.equals(confirmPassword)){
            passwordEditText.error = "Passwords do not match"
            return false
        }

        return true

    }

    fun isValidEmail(email: String?): Boolean {
        val isGoodEmail: Boolean = (email.isNullOrEmpty() || email.isNullOrBlank()
                && Patterns.EMAIL_ADDRESS.matcher(email).matches())

        if (isGoodEmail) {
            emailEditText.error = "Please enter a valid emaill address"
            return false
        }
        return true
    }
}
