package com.ongea.adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.ongea.Constants
import com.ongea.R
import com.ongea.models.Message

class ChatsAdapter(
        private val chats: MutableList<Message>,
        private val context: Context)
    : RecyclerView.Adapter<ChatsAdapter.SentMessagesViewHolder>() {

    private var messagesCollection: CollectionReference
    private var sent: Int
    private val received: Int
    private var MESSAGE1D: String
    private var CONVERSATIONID: String
    private var showOnClick: Boolean
    private var firebaseAuth = FirebaseAuth.getInstance()


    init {
        // initialize firebase auth
        firebaseAuth = FirebaseAuth.getInstance()
        // initialize firestore references
        messagesCollection = FirebaseFirestore.getInstance().collection(Constants.CHATS)
        // view types
        sent = 0
        received = 1
        // intent extras
        MESSAGE1D = "message id"
        CONVERSATIONID = "room id"
        showOnClick = false

    }

    override fun getItemId(position: Int): Long {
        return super.getItemId(position)
    }

    override fun getItemViewType(position: Int): Int {
        return super.getItemViewType(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SentMessagesViewHolder {
        val view = LayoutInflater.from(parent!!.context)
                .inflate(R.layout.layout_message_sent_default, parent, false)

        return SentMessagesViewHolder(view)

    }

    override fun getItemCount(): Int {
        return chats.size
    }

    override fun onBindViewHolder(holder: SentMessagesViewHolder, position: Int) {
        val message: Message? = chats[position];

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

}