package com.ongea.activities

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.ongea.R
import kotlinx.android.synthetic.main.activity_forgot_password.*

class PasswordResetActivity : AppCompatActivity() {

    private var firebaseAuth: FirebaseAuth? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        firebaseAuth = FirebaseAuth.getInstance()

        btn_reset_password.setOnClickListener {
            var email: String = registeredEmailEditText.text.toString()

            if (email.isNullOrBlank() || email.isNullOrEmpty()){
                registeredEmailEditText.error = "Enter your registered emaill address"
            }

            progressBar.visibility = View.VISIBLE
            firebaseAuth?.sendPasswordResetEmail(email)
                    ?.addOnCompleteListener {
                        if (it.isSuccessful){
                            toast(this, R.string.reset_password_instructions, Toast.LENGTH_SHORT)
                        }else{
                            toast(this, R.string.reset_password_failed, Toast.LENGTH_SHORT)
                        }
                    }

        }
    }

    fun toast(context: Context, message: Int, length: Int){
        var toast: Toast = Toast.makeText(context, message, length)
        toast.show()
    }
}
