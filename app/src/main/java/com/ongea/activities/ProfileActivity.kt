package com.ongea.activities

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.ongea.Constants
import com.ongea.R
import com.ongea.models.Room
import com.ongea.models.User
import kotlinx.android.synthetic.main.activity_profile.*

class ProfileActivity : AppCompatActivity(), View.OnClickListener {
    private var TAG = ProfileActivity::class.java.simpleName
    private lateinit var usersReference: CollectionReference
    private lateinit var roomReferences: CollectionReference
    private lateinit var randomReference: DatabaseReference
    private lateinit var mFirebaseAuth: FirebaseAuth;
    //intent extras
    private lateinit var mUid: String
    private lateinit var  mRoomId: String
    private var USER_ID = "user id"
    private var ROOM_ID = "room id"

    private var processRoom: Boolean? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        setSupportActionBar(toolbar)
        // navigate back
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_whit)
        toolbar.setNavigationOnClickListener { finish() }
        //initialize firebase auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        //initialize firebase references
        initFirebaseReferences()
        // initialize click listeners
        setUserProfile()

        if (intent.extras != null){
            mUid = intent.getStringExtra(USER_ID)
        }

    }

    private fun initFirebaseReferences(){
        usersReference = FirebaseFirestore.getInstance().collection(Constants.USERS)
        roomReferences = FirebaseFirestore.getInstance().collection(Constants.CHATS)
        randomReference = FirebaseDatabase.getInstance().getReference(Constants.CHATS)
    }

    private fun setUserProfile() {
        usersReference.document(mUid)
                .addSnapshotListener(EventListener { documentSnapshot, exception ->
            if (exception != null){
                Log.d(TAG, "listerner error")
                return@EventListener
            }

            if (documentSnapshot!!.exists()){
                var user = documentSnapshot.toObject(User::class.java)

                // set the background color
                if (user?.profile_image.isNullOrEmpty()
                        && user?.profile_cover.isNullOrEmpty()){
                    backgroundRelativeLayout.setBackgroundColor(resources.getColor(R.color.colorWhite))
                    fullNameTextView.setTextColor(resources.getColor(R.color.grey_700))
                    bioTextView.setTextColor(resources.getColor(R.color.grey_700))
                }else{
                    backgroundRelativeLayout.setBackgroundColor(resources.getColor(R.color.colorPrimary))
                    fullNameTextView.setTextColor(resources.getColor(R.color.colorWhite))
                    bioTextView.setTextColor(resources.getColor(R.color.colorWhite))
                }

                fullNameTextView.text = user?.first_name + " " + user?.second_name
                toolbar.title = user?.username
                Glide.with(this)
                        .load(user!!.profile_image)
                        .apply(RequestOptions()
                                .diskCacheStrategy(DiskCacheStrategy.DATA))
                        .into(profileImageView)
                Glide.with(this)
                        .load(user!!.profile_image)
                        .apply(RequestOptions()
                                .diskCacheStrategy(DiskCacheStrategy.DATA))
                        .into(profileCover)
            }
        })
    }

    override fun onClick(v: View?) {
        if (v == sendMessageButton) {
            sendMessage()
        }

    }

    private fun sendMessage() {
        processRoom = true
        roomReferences.document(mUid)
                .collection("conversations").document(mFirebaseAuth.currentUser!!.uid)
                .addSnapshotListener(EventListener { documentSnapshot, exception ->

                    if ( exception != null){
                        Log.d(TAG, exception.toString())
                        return@EventListener
                    }

                    if (processRoom!!){
                        if (documentSnapshot!!.exists()){
                            var room = documentSnapshot.toObject(Room::class.java)
                            var roomId = room?.conversationId

                            var intent = Intent(this, ChatsActivity::class.java)
                            intent.putExtra(this.ROOM_ID, roomId)
                            intent.putExtra(this.USER_ID, mUid)
                            startActivity(intent)
                            processRoom = false
                        }else{
                            roomReferences.document(mFirebaseAuth.currentUser!!.uid)
                                    .collection("conversations").document(mUid)
                                    .addSnapshotListener(EventListener { documentSnapshot, exception ->

                                        if ( exception != null){
                                            Log.d(TAG, exception.toString())
                                            return@EventListener
                                        }

                                        if (processRoom!!){
                                            if (documentSnapshot!!.exists()){
                                                var room = documentSnapshot.toObject(Room::class.java)
                                                var roomId = room?.conversationId

                                                var intent = Intent(this, ChatsActivity::class.java)
                                                intent.putExtra(this.ROOM_ID, roomId)
                                                intent.putExtra(this.USER_ID, mFirebaseAuth.currentUser!!.uid)
                                                startActivity(intent)
                                                processRoom = false
                                            }else{
                                                var roomId: String = randomReference.push().key.toString()
                                                var intent = Intent(this, ChatsActivity::class.java)
                                                intent.putExtra(this.ROOM_ID, roomId)
                                                intent.putExtra(this.USER_ID, mUid)
                                                startActivity(intent)
                                                processRoom = false
                                            }
                                        }

                                    })

                        }
                    }

                })
    }

}
