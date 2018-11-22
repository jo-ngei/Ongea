package com.ongea.activities

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.ongea.Constants
import com.ongea.R
import kotlinx.android.synthetic.main.activity_sign_in.*

class SignInActivity : AppCompatActivity(), View.OnClickListener {
    private val TAG = SignInActivity::class.java.simpleName
    //firebase authentication
    private var mFirebaseAuth: FirebaseAuth? = null
    // firebase authenitcation listenr
    private var mFirebaseAuthStateListener: FirebaseAuth.AuthStateListener? = null
    //firebase firestore references
    lateinit var usersReference: CollectionReference;
    //firebase login progress dialog
    lateinit var mProgressDialog: ProgressDialog
    private val RC_SIGN_IN = 9001
    private var mGoogleSignInClient :GoogleSignInClient? = null
    private var PASSWORD = "password"
    private var EMAIL = "email"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        // initialize click listeners
        loginButton.setOnClickListener(this)
        googleSignInButton.setOnClickListener(this)
        forgotPasswordTextView.setOnClickListener(this)
        signUpTextView.setOnClickListener(this)

        //init firebase auth
        mFirebaseAuth = FirebaseAuth.getInstance()
        //initialize firebase references
        usersReference = FirebaseFirestore.getInstance().collection(Constants.USERS)
        //listen for change in authentication
        mFirebaseAuthStateListener =  FirebaseAuth.AuthStateListener {
            var user: FirebaseUser? = it.currentUser
            if (user != null){
                usersReference.document(mFirebaseAuth?.currentUser!!.uid)
                        .addSnapshotListener(EventListener { documentSnapshot, e ->

                            if (e != null){
                                Log.e(TAG, "listen error", e)
                                return@EventListener
                            }

                            if (documentSnapshot!!.exists()){
                                var intent = Intent(this, MainActivity::class.java)
                                startActivity(intent)
                                finish()
                            }else {
                                naviagateToCreateProfile()
                            }
                        })
            }
        }

        mFirebaseAuthStateListener = FirebaseAuth.AuthStateListener {  }

        //init authentication progress listener
        createAuthProgressDialog()
        // init google sign in
        signInWithGoogle()

    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onStart() {
        super.onStart()
        mFirebaseAuth?.addAuthStateListener { mFirebaseAuthStateListener }
    }

    override fun onStop() {
        super.onStop()
        if (mFirebaseAuthStateListener != null){
            mFirebaseAuth?.removeAuthStateListener { mFirebaseAuthStateListener }
        }
    }

    private fun naviagateToCreateProfile(){
        var email: String = emailEditText.text.toString()
        var password: String = passwordEditText.text.toString()
        mProgressDialog.show()

        if (email.isNullOrEmpty() || email.isNullOrBlank() ){
            emailEditText.error = "Please enter a valid email"
            return
        }

        if (password.isNullOrEmpty() ||password.isNullOrBlank()){
            passwordEditText.error = ("Please enter your password")
            return
        }

        var intent = Intent(this, CreateProfileActivity::class.java)
        intent.putExtra(this.PASSWORD, password)
        intent.putExtra(this.EMAIL, email)
        startActivity(intent)
        finish()
    }

    /**custom authentication with email and password*/
    fun loginWithPassword() {
        var email: String = emailEditText.text.toString()
        var password: String = passwordEditText.text.toString()
        mProgressDialog.show()

        if (email.isNullOrEmpty() || email.isNullOrBlank() ){
            emailEditText.error = "Please enter a valid email"
            return
        }

        if (password.isNullOrEmpty() ||password.isNullOrBlank()){
            passwordEditText.error = ("Please enter your password")
            return
        }

        mFirebaseAuth?.signInWithEmailAndPassword(email, password)
                ?.addOnCompleteListener(OnCompleteListener {
                    mProgressDialog.dismiss()
                    if (!it.isSuccessful){
                        errorRelativeLayout.visibility = View.VISIBLE
                        errorTextView.setText(R.string.error_message_confirm)

                        errorRelativeLayout.postDelayed(Runnable {
                            errorRelativeLayout.visibility = View.GONE
                        }, 5000)

                    }else{
                        progressBar.visibility = View.VISIBLE
                        usersReference.document(mFirebaseAuth?.currentUser!!.uid)
                                .addSnapshotListener(EventListener { documentSnapshot, e ->

                                    if (e != null){
                                        Log.e(TAG, "listen error", e)
                                        return@EventListener
                                    }

                                    if (documentSnapshot!!.exists()){
                                        var intent = Intent(this, MainActivity::class.java)
                                        startActivity(intent)
                                        finish()
                                    }else {
                                        var intent = Intent(this, CreateProfileActivity::class.java)
                                        intent.putExtra(this.PASSWORD, password)
                                        intent.putExtra(this.EMAIL, email)
                                        startActivity(intent)
                                        finish()
                                    }
                                })
                    }
                })
    }

    fun signInWithGoogle() {
        var googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions)
    }

    fun googleSignIn() {
        var intent  = mGoogleSignInClient?.signInIntent
        startActivityForResult(intent, RC_SIGN_IN)
    }

    fun firebaseAuthWithGoogle(googleSignInAccount: GoogleSignInAccount?){
        mProgressDialog.show()
        var credentials  = GoogleAuthProvider.getCredential(googleSignInAccount?.idToken, null)
        mFirebaseAuth?.signInWithCredential(credentials)
                ?.addOnCompleteListener {
                     if (!it.isSuccessful){
                         toast(this, R.string.authentication_failed, Toast.LENGTH_SHORT)
                     }else{
                         usersReference.document(mFirebaseAuth?.currentUser!!.uid)
                                 .addSnapshotListener(EventListener { documentSnapshot, e ->
                                     if (e != null){
                                         Log.e(TAG, "listen error", e)
                                         return@EventListener
                                     }

                                     if (documentSnapshot!!.exists()){
                                         var intent = Intent(this, MainActivity::class.java)
                                         startActivity(intent)
                                         finish()
                                     }else {
                                         var intent = Intent(this, CreateProfileActivity::class.java)
                                         intent.putExtra(this.PASSWORD, "")
                                         intent.putExtra(this.EMAIL, googleSignInAccount!!.email)
                                         startActivity(intent)
                                         finish()
                                     }
                                 })

                     }

                    mProgressDialog.dismiss()
                }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN){
            var task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                var account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account)
            }catch (exception: ApiException){
                println("google api exception " + exception )
                var toast = Toast.makeText(this, "Sign in with Google failed please try once more", Toast.LENGTH_SHORT)
                toast.show()
            }
        }
    }

    fun toast(context: Context, message: Int, length: Int){
        var toast: Toast = Toast.makeText(context, message, length)
        toast.show()
    }

    /**sign in progress dialog*/
    fun createAuthProgressDialog() {
        mProgressDialog = ProgressDialog(this)
        mProgressDialog.setTitle("Connecting")
        mProgressDialog.setMessage("Authenticating your sign in credentials")
        mProgressDialog.setCancelable(false)
    }


    override fun onClick(v: View?) {
        val id = v!!.id
        if (id == R.id.loginButton) {
            loginWithPassword()
        }

        if (id == R.id.googleSignInButton) {
            googleSignIn()
        }

        if (id == R.id.signUpTextView){
            var intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }

        if (id == R.id.forgotPasswordTextView) {
            var intent = Intent(this, PasswordResetActivity::class.java)
            startActivity(intent)
        }
    }
}
