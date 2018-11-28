package com.ongea.adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.bumptech.glide.Glide.init
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.firebase.ui.firestore.ObservableSnapshotArray
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.ongea.Constants
import com.ongea.R
import com.ongea.models.Message

class ChatsAdapter(private val context:
    Context, private val options: FirestoreRecyclerOptions<Message>)
    : FirestoreRecyclerAdapter<Message, RecyclerView.ViewHolder>(options) {

    private var messagesCollection: CollectionReference
    private var SENT: Int
    private var RECEIVED: Int
    private var MESSAGE1D: String
    private var CONVERSATIONID: String
    private var showOnClick: Boolean
    private var firebaseAuth = FirebaseAuth.getInstance()

    init {
        // initialize firebase auth
        firebaseAuth = FirebaseAuth.getInstance()
        // initialize firestore references
        messagesCollection = FirebaseFirestore.getInstance().collection(Constants.CHATS)
        // intent extras
        MESSAGE1D = "message id"
        CONVERSATIONID = "room id"
        showOnClick = false

        SENT = 0
        RECEIVED = 1

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, model: Message) {
        when(holder.itemViewType){
            SENT -> {
                populateSentText(SentMessagesViewHolder(holder.itemView), position)
            }
            RECEIVED -> {
                populateRecievedText(ReceivedMessagesViewHolder(holder.itemView), position)
            }
        }
    }


    override fun getItemViewType(position: Int): Int {
        var message = getSnapshot(position)
        var toUserId = message.to
        var fromUserId = message.from

        if (fromUserId.equals(firebaseAuth.currentUser?.uid)){
            return SENT
        }else{
            return RECEIVED
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SentMessagesViewHolder {
        val view: View? = null
        when(getItemViewType(viewType)){
               SENT ->  {
                   LayoutInflater.from(parent.context)
                       .inflate(R.layout.layout_message_sent_default, parent, false)
                   return SentMessagesViewHolder(view!!)
               }else -> {
                   LayoutInflater.from(parent.context)
                            .inflate(R.layout.layout_message_sent_default, parent, false)
                   return SentMessagesViewHolder(view!!)
            }
        }

    }

    override fun getItemCount(): Int {
        return snapshots.size
    }

    override fun getSnapshots(): ObservableSnapshotArray<Message> {
        return super.getSnapshots()
    }

    private fun getSnapshot(index: Int): Message {
        return snapshots.get(index);
    }

    private fun populateSentText(holder: SentMessagesViewHolder, position: Int){
        val message: Message? = getSnapshot(position)
        val text: String? = message?.text
        holder.messageText.text = text
    }

    private fun populateRecievedText(holder: ReceivedMessagesViewHolder, position: Int){
        val message: Message? = getSnapshot(position)
        val text: String? = message?.text
        holder.messageText.text = text
    }


    inner class SentMessagesViewHolder internal constructor(view: View)
        : RecyclerView.ViewHolder(view) {

        internal var messageText: TextView
        internal var messageStatus: TextView
        internal var messageDate: TextView

        init {
            messageText = view.findViewById(R.id.messageTextView)
            messageStatus = view.findViewById(R.id.statusTextView)
            messageDate = view.findViewById(R.id.dateTextView)
        }
    }

    inner class ReceivedMessagesViewHolder internal constructor(view: View)
        :RecyclerView.ViewHolder(view){

        internal var messageText: TextView
        internal var messageStatus: TextView
        internal var messageDate: TextView

        init {
            messageText = view.findViewById(R.id.messageTextView)
            messageStatus = view.findViewById(R.id.statusTextView)
            messageDate = view.findViewById(R.id.dateTextView)
        }

    }

}