package com.ongea.activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.*
import com.ongea.Constants
import com.ongea.R
import com.ongea.adapters.ChatsAdapter
import com.ongea.models.Message
import com.ongea.models.Room
import com.ongea.models.User
import kotlinx.android.synthetic.main.activity_chats.*

class ChatsActivity : AppCompatActivity() {
    private val TAG: String = ChatsActivity::class.java.simpleName
    // firestore references
    private lateinit var messagesReference: CollectionReference
    private lateinit var roomReferences: CollectionReference
    private lateinit var randomReference: DatabaseReference
    private lateinit var usersReference: CollectionReference
    private lateinit var firebaseAuth: FirebaseAuth
    // firestore listener
    private var listenerRegistration: ListenerRegistration? = null
    //chats adapter
    lateinit var mChatsAdapter: ChatsAdapter
    // firebase current user
    lateinit var currentUser: String
    // intent string
    lateinit var mUid: String
    lateinit var mConversationId: String
    private var CONVERSATIONID: String? = "room id"
    private var USERID: String? = "user id"
    //chats mutable list
    private var chats: MutableList<Message> = mutableListOf<Message>()
    //recycler view layout manager
    lateinit var mLinearLayoutManager: LinearLayoutManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chats)
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_whit)
        toolbar.setNavigationOnClickListener { finish() }
        // init firebase references
        initReferences()
        // get intents extras
        getIntents()
        //set toolbar title
        setUserProfile()
        // get the chat messages set the recycler view
        setRecyclerView()
        getChatMessages()
        // send message on clik
        sendMessage()
    }

    fun getIntents() {
        if (intent.extras != null){
         mUid = intent.getStringExtra(USERID)
         mConversationId = intent.getStringExtra(CONVERSATIONID)

        }
    }

    fun initReferences() {
        // firebase auth
        firebaseAuth = FirebaseAuth.getInstance()
        currentUser = firebaseAuth.currentUser!!.uid
        // firestore
        messagesReference = FirebaseFirestore.getInstance().collection(Constants.CHATS);
        roomReferences = FirebaseFirestore.getInstance().collection(Constants.CHATS)
        usersReference = FirebaseFirestore.getInstance().collection(Constants.USERS)
        //firebase real time
        randomReference = FirebaseDatabase.getInstance().getReference(Constants.CHATS);

    }

    fun sendMessage() {
        sendRelativeLayout.setOnClickListener {
            createNewMessage()
        }
    }

    fun setUserProfile(){
        usersReference.document(mUid)
                .addSnapshotListener(EventListener { documentSnapshot, exception ->
                    if (exception != null){
                        Log.d(TAG, "listerner error")
                        return@EventListener
                    }

                    if (documentSnapshot!!.exists()){
                        var user = documentSnapshot.toObject(User::class.java)
                        toolbar.title = user?.username

                    }
                })
    }

    fun createNewMessage() {
        val newMessage = Message.createMessage()
        val newRoom = Room.createRoom()

        val pushId: String = randomReference.push().key.toString()
        val conversationId: String = randomReference.push().key.toString()
        val time: String = System.currentTimeMillis().toString()
        val text: String = newMessageEditText.text.toString()
        val fromUser: String = firebaseAuth.currentUser!!.uid
        val toUser: String = mUid


        newMessage.objectId = pushId
        newMessage.photo = ""
        newMessage.conversationId = conversationId
        newMessage.text = text
        newMessage.time = time
        newMessage.seen
        newMessage.from = fromUser
        newMessage.to = toUser

        newRoom.to = toUser
        newRoom.from = fromUser
        newRoom.isBlocked = false
        newRoom.isSeen = false
        newRoom.time = time
        newRoom.messageId = pushId
        newRoom.conversationId = conversationId

        messagesReference.document(fromUser)
                .collection(conversationId).document(pushId)
        messagesReference.document(toUser)
                .collection(conversationId).document(pushId)

        roomReferences.document(toUser)
                .collection("conversations").document(fromUser)
        roomReferences.document(fromUser)
                .collection("conversations").document(toUser)

        newMessageEditText.setText("")
    }

    fun getChatMessages() {
       listenerRegistration = messagesReference.document(currentUser).collection(mConversationId)
                .addSnapshotListener(EventListener { documentSnapshots, e ->

                    if (e != null){
                        Log.e(TAG, "Listen failed", e)
                        return@EventListener

                    }

                    if (documentSnapshots!!.isEmpty) {
                        for (doc in documentSnapshots) {
                            val message: Message = doc.toObject(Message::class.java)
                            chats.add(message)
                        }
                    }
                })

    }

    private fun setRecyclerView(){
        mChatsAdapter = ChatsAdapter(chats, this)
        mLinearLayoutManager = LinearLayoutManager(this)
        chatsRecyclerView.layoutManager = mLinearLayoutManager
        chatsRecyclerView.adapter = mChatsAdapter
        mChatsAdapter.notifyDataSetChanged()
    }

    private fun onDocumentAdded(change: DocumentChange) {

    }

    private fun onDocumentModified(change: DocumentChange) {

    }

    private fun onDocumentRemoved(change: DocumentChange) {

    }
}
