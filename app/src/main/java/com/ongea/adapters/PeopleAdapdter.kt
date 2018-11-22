package com.ongea.adapters

import android.content.Context
import android.content.Intent
import android.support.v4.content.ContextCompat.startActivity
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.ongea.Constants
import com.ongea.R
import com.ongea.activities.ChatsActivity
import com.ongea.models.Room
import com.ongea.models.User
import de.hdodenhof.circleimageview.CircleImageView

class PeopleAdapdter(
        private val context: Context,
        private val people: MutableList<DocumentSnapshot>)
    : RecyclerView.Adapter<PeopleAdapdter.PeopleViewHolder>() {

    private var TAG = PeopleAdapdter::class.java.simpleName
    private lateinit var usersReference: CollectionReference
    private lateinit var roomReferences: CollectionReference
    private lateinit var randomReference: DatabaseReference
    private lateinit var mFirebaseAuth: FirebaseAuth
    //strings
    private lateinit var  mRoomId: String
    private var USER_ID = "user id"
    private var ROOM_ID = "room id"

    private var processRoom: Boolean? = null

    init {
        initFirebaseReferences()
    }

    private fun initFirebaseReferences(){
        usersReference = FirebaseFirestore.getInstance().collection(Constants.USERS)
        roomReferences = FirebaseFirestore.getInstance().collection(Constants.CHATS)
        randomReference = FirebaseDatabase.getInstance().getReference(Constants.CHATS)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PeopleViewHolder {
        var view: View = LayoutInflater.from(parent.context)
                .inflate(R.layout.layout_people, parent, false)
        return PeopleViewHolder(view)
    }

    override fun getItemCount(): Int {
        return people.size
    }

    override fun onBindViewHolder(holder: PeopleViewHolder, position: Int) {
        var user: User? = people[position].toObject(User::class.java)
        var userId: String? = user?.user_id
        var username: String? = user?.username
        var firstName: String? = user?.first_name
        var secondName: String? = user?.second_name
        var profileImage: String? = user?.profile_image

        holder.usernameTextView.text = username
        holder.fullNameTextView.text = firstName + " " + secondName
        Glide.with(context)
                .load(profileImage)
                .apply(RequestOptions()
                        .placeholder(R.drawable.ic_user)
                        .diskCacheStrategy(DiskCacheStrategy.DATA))
                .into(holder.profileImageView)

        holder.itemView.setOnClickListener { sendMessage(userId!!) }

    }

    inner class PeopleViewHolder internal constructor(view: View)
        : RecyclerView.ViewHolder(view) {

        internal var profileImageView: CircleImageView
        internal var usernameTextView: TextView
        internal var fullNameTextView: TextView
        internal var sendMessageRelativeLayout: RelativeLayout

        init {
            profileImageView = view.findViewById(R.id.profileImageView)
            usernameTextView = view.findViewById(R.id.usernameTextView)
            fullNameTextView = view.findViewById(R.id.fullNameTextView)
            sendMessageRelativeLayout = view.findViewById(R.id.sendMessageRelativeLayout)
        }

    }

    private fun sendMessage(mUid: String) {
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

                            var intent = Intent(context, ChatsActivity::class.java)
                            intent.putExtra(this.ROOM_ID, roomId)
                            intent.putExtra(this.USER_ID, mUid)
                            context.startActivity(intent)
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

                                                var intent = Intent(context, ChatsActivity::class.java)
                                                intent.putExtra(this.ROOM_ID, roomId)
                                                intent.putExtra(this.USER_ID, mFirebaseAuth.currentUser!!.uid)
                                                context.startActivity(intent)
                                                processRoom = false
                                            }else{
                                                var roomId: String = randomReference.push().key.toString()
                                                var intent = Intent(context, ChatsActivity::class.java)
                                                intent.putExtra(this.ROOM_ID, roomId)
                                                intent.putExtra(this.USER_ID, mUid)
                                                context.startActivity(intent)
                                                processRoom = false
                                            }
                                        }

                                    })

                        }
                    }

                })
    }
}