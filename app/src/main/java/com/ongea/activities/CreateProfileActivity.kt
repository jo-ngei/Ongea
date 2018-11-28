package com.ongea.activities

import android.Manifest
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.View
import android.widget.Toast
import butterknife.ButterKnife
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.ongea.Constants
import com.ongea.R
import com.ongea.RunTimePermissions
import com.ongea.models.User
import kotlinx.android.synthetic.main.activity_create_profile.*

class CreateProfileActivity : AppCompatActivity(), View.OnClickListener {
    private var TAG = CreateProfileActivity::class.java.simpleName
    //firebase reference
    lateinit var usersReference: CollectionReference;
    //firebase auth
    private var firebaseAuth: FirebaseAuth? = null
    // intent
    lateinit var email: String
    lateinit var password: String
    //intent extras
    private var photoUri: Uri? = null
    private var PASSWORD = "password"
    private var EMAIL = "email"
    //authentication progress dialog
    lateinit var progressDialog: ProgressDialog
    // request code
    private var REQUEST_CODE = 101
    private var GALLERY_PROFILE_PHOTO_REQUEST = 111;
    lateinit var runTimePermissions: RunTimePermissions


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_profile)
        ButterKnife.bind(this)

        //initialize firebase authentication
        firebaseAuth = FirebaseAuth.getInstance()
        // get intent extras
        email = intent.getStringExtra(EMAIL)
        password = intent.getStringExtra(PASSWORD)

        //initialize firebase references
        usersReference = FirebaseFirestore.getInstance().collection(Constants.USERS)

        //initialize click listeneres
        submitUserInfoButton.setOnClickListener(this)
        resendLinkRelativeLayout.setOnClickListener(this)
        profileImageView.setOnClickListener(this)

        resendLinkRelativeLayout.visibility = View.GONE

        createAuthProgressDialog()

        requestPermissions()

    }




    fun createAuthProgressDialog() {
        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Signing in")
        progressDialog.setCancelable(false)

    }

    fun logingWithPassword() {
        var firstName = fisrtNameEditText.text.toString()
        var secondName = secondNameEditText.text.toString()

        if (firstName.isNullOrBlank() || firstName.isNullOrEmpty()){
            fisrtNameEditText.error = ""
        }else if (secondName.isNullOrEmpty() || secondName.isNullOrBlank()){
            secondNameEditText.error = ""
        }else {
            progressDialog.show()

            firebaseAuth?.signInWithEmailAndPassword(email, password)
                    ?.addOnCompleteListener {

                        if (!it.isSuccessful) {
                            errorRelativeLayout.visibility = View.VISIBLE
                            errorTextView.text = "Please confirm that you are connected to the internet"

                            errorRelativeLayout.postDelayed(Runnable {
                                errorRelativeLayout.visibility = View.GONE
                            }, 5000)

                        }else {
                            checkIfEmailIsVerified(email, password)
                        }

                    }

        }

    }

    fun checkIfEmailIsVerified(email: String, password: String) {
        if (firebaseAuth?.currentUser!!.isEmailVerified) {
            progressDialog.setMessage("Updating your profile info")
            createUserPtrofile(email, password)
        }else {
            sendVerificationEmail()
        }
    }

    fun requestPermissions() {
        val requiredPermissions = listOf<String>(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

        runTimePermissions = RunTimePermissions(this, requiredPermissions, REQUEST_CODE)

        val version = Build.VERSION.SDK_INT
        if (version > Build.VERSION_CODES.LOLLIPOP_MR1) {
            runTimePermissions.checkPermissions()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CODE -> {
                val isPermissionGranted =  runTimePermissions
                        .processPermissionsResult(requestCode, permissions, grantResults)
                if (isPermissionGranted) {
                    shortToast("Permissions granted.")
                }else {
                    shortToast("Permissions denied.")
                }
                return
            }
        }
    }

    // Extension function to show shortToast message
    fun Context.shortToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    fun sendVerificationEmail() {
        firebaseAuth?.currentUser!!.sendEmailVerification().addOnCompleteListener {
            if (it.isSuccessful) {
                shortToast("Please confirm your this " + email + " we have sent you a confirmation link")
                FirebaseAuth.getInstance().signOut()
                resendLinkRelativeLayout.visibility = View.VISIBLE
            }else {
               refreshActivity()
            }
        }
    }

    fun refreshActivity(){
        overridePendingTransition(0, 0)
        finish()
        overridePendingTransition(0,0)
        startActivity(intent)
        AlertDialog.Builder(this)
                .setMessage("Ongea could not send verification email, please confirm that you " +
                        "entered the right email and check your internet connection")
                .setPositiveButton(android.R.string.ok, DialogInterface.OnClickListener { dialog, which ->  }
                ).setIcon(android.R.drawable.ic_dialog_alert).show()
    }

    fun createUserPtrofile(email: String, password: String) {
        var uid = firebaseAuth?.currentUser!!.uid
        var deviceId = FirebaseInstanceId.getInstance().token
        var firstName = fisrtNameEditText.text.toString()
        var secondName = secondNameEditText.text.toString()
        var username = usernameEditText.text.toString()

        if (firstName.isNullOrBlank() || firstName.isNullOrEmpty()){
            fisrtNameEditText.error = ""
        }else if (secondName.isNullOrEmpty() || secondName.isNullOrBlank()){
            secondNameEditText.error = ""
        }else if (username.isNullOrBlank() || username.isNullOrEmpty()){
            usernameEditText.error = ""
        }else{
            var user: User = User.createUser()
            user.first_name = firstName
            user.second_name = secondName
            user.user_id = uid
            user.email = email
            user.device_id = deviceId
            user.username = username
            usersReference.document(uid).set(user)
                    .addOnCompleteListener {
                        if (photoUri != null){
                            var storageRef: StorageReference = FirebaseStorage
                                    .getInstance().reference.child("profile images")
                                    .child(uid)
                            var uploadtask: UploadTask = storageRef.putFile(photoUri!!)
                            var urlTask= uploadtask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                                if (!task.isSuccessful){
                                    Log.d("upload exception", task.exception.toString())
                                }
                                return@Continuation storageRef.downloadUrl
                            }).addOnCompleteListener { task ->
                                if (task.isSuccessful){
                                    val downloadurl = task.result
                                    usersReference.document(uid).update("profile_image", downloadurl.toString())
                                            .addOnCompleteListener {
                                                if (it.isSuccessful){
                                                    var intent = Intent(this, CreateProfileActivity::class.java)
                                                    startActivity(intent)
                                                    finish()
                                                }
                                            }

                                }
                            }
                        }else{
                            var intent = Intent(this, CreateProfileActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    }
        }

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK){
            if (requestCode == GALLERY_PROFILE_PHOTO_REQUEST) {
               if (data != null ){
                   photoUri = data.data
                   Glide.with(this)
                           .asBitmap()
                           .load(photoUri)
                           .into(profileImageView)
               }
            }
        }

    }

    override fun onClick(v: View?) {
        val id = v?.id
        if (id == R.id.profileImageView) {
            var intent = Intent()
            intent.setAction(Intent.ACTION_GET_CONTENT)
            intent.setType("image/*")
            startActivityForResult(intent, GALLERY_PROFILE_PHOTO_REQUEST)
        }

        if (id == R.id.submitUserInfoButton) {
            progressDialog.setMessage("please wait...")
            usersReference!!.whereEqualTo("username", usernameEditText.text.toString())
                    .addSnapshotListener(EventListener { documentSnapshots, e ->

                        if (e != null){
                            Log.e(TAG, "Listen failed", e)
                            return@EventListener

                        }

                        if (!documentSnapshots!!.isEmpty) {
                            shortToast("username has been taken")
                        }else {
                            logingWithPassword()
                        }
                    })
        }

        if (v == resendLinkRelativeLayout) {
            firebaseAuth!!.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener {
                        progressDialog.dismiss()

                        if (it.isSuccessful){
                            sendVerificationEmail()
                        }else {
                            errorRelativeLayout.visibility = View.VISIBLE
                            errorTextView.text = "Please check that you are connected to the internet"
                            errorRelativeLayout.postDelayed(Runnable {
                                errorRelativeLayout.visibility = View.GONE
                            }, 5000)
                        }
                    }
        }

    }

}
