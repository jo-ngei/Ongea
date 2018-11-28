package com.ongea.adapters

import android.content.Context
import android.content.Intent
import android.support.v4.content.ContextCompat.startActivity
import android.support.v7.widget.RecyclerView
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
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
import com.ongea.models.Message
import com.ongea.models.Room
import de.hdodenhof.circleimageview.CircleImageView

class ConversationsAdapter(
        private val context: Context,
        private val conversations: MutableList<DocumentSnapshot>)
    : RecyclerView.Adapter<ConversationsAdapter.ConversationsViewHolder>(){

    private var TAG = ConversationsAdapter::class.java.simpleName;
    // firestore references
    private var conversationReferences: CollectionReference? = null
    private var userReference: CollectionReference? = null
    // firebase auth
    lateinit var firebaseAuth: FirebaseAuth
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
        mFirebaseAuth = FirebaseAuth.getInstance()
        usersReference = FirebaseFirestore.getInstance().collection(Constants.USERS)
        roomReferences = FirebaseFirestore.getInstance().collection(Constants.CHATS)
        randomReference = FirebaseDatabase.getInstance().getReference(Constants.CHATS)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversationsViewHolder {
        val view = LayoutInflater.from(parent!!.context)
                .inflate(R.layout.layout_converstaion, parent, false)

        return ConversationsViewHolder(view)

    }

    override fun getItemCount(): Int {
        return conversations.size
    }

    override fun onBindViewHolder(holder: ConversationsViewHolder, position: Int) {
        var room = conversations.get(position).toObject(Room::class.java)
        var toId: String? = room?.to
        var pushId: String? = room?.messageId
        var conversationId: String? = room?.conversationId

        roomReferences.document(toId!!)
                .collection(conversationId!!).document(pushId!!)
                .addSnapshotListener(EventListener { documentSnapshot, exception ->

                    if (exception != null){
                        return@EventListener
                    }

                    if (documentSnapshot!!.exists()){
                        var message = documentSnapshot.toObject(Message::class.java)
                        holder.conversationTextTextView.text = message!!.text

                    }

                })

        holder.itemView.setOnClickListener {  sendMessage(toId) }

    }


    inner class ConversationsViewHolder internal constructor(view: View)
        : RecyclerView.ViewHolder(view) {

        internal var conversationTextTextView: TextView
        internal var converstaionDateTextView: TextView
        internal var profileImageView: CircleImageView
        internal var usernameTextView: TextView

        init {
            conversationTextTextView = view.findViewById(R.id.messageTextView)
            converstaionDateTextView = view.findViewById(R.id.dateTextView)
            profileImageView = view.findViewById(R.id.profileImageView)
            usernameTextView = view.findViewById(R.id.usernameTextView)

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
                                                intent.putExtra(this.USER_ID, mFirebaseAuth.currentUser!!.uid)
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