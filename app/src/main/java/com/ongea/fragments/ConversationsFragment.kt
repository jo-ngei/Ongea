package com.ongea.fragments


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.ongea.Constants

import com.ongea.R
import com.ongea.adapters.ConversationsAdapter
import kotlinx.android.synthetic.main.fragment_conversations.*


class ConversationsFragment : Fragment() {
    private var TAG = ConversationsFragment::class.java.simpleName
    //firebase reference
    private var roomsCollection: CollectionReference? = null
    private var roomsQuery: Query? = null
    private var listenerRegistration: ListenerRegistration? = null
    //firebase auth
    lateinit var firebaseAuth: FirebaseAuth
    lateinit var currentUser: String
    // conversations lists
    private var conversations: MutableList<DocumentSnapshot> = mutableListOf()
    // recycler view layout manager
    lateinit var mLinearLayoutManager: LinearLayoutManager
    // conversations recycler adapter
    private var mConversationsAdapter: ConversationsAdapter? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        var view = inflater.inflate(R.layout.fragment_conversations, container, false)
        // initialize firebase auth and current user
        firebaseAuth = FirebaseAuth.getInstance()
        currentUser = firebaseAuth.currentUser!!.uid
        //initialize firestore references
        initReferences()
        return view
    }


    override fun onDestroy() {
        super.onDestroy()
        listenerRegistration?.remove()
    }

    override fun onStart() {
        super.onStart()
        conversations.clear()
        getChatMessages()
        // set the recycler view
        setRecyclerView()

    }


    fun initReferences() {
        roomsCollection = FirebaseFirestore.getInstance().collection(Constants.CHATS)
    }

    fun setRecyclerView() {
        mConversationsAdapter = ConversationsAdapter(context!!, conversations)
        mLinearLayoutManager = LinearLayoutManager(context)
        conversationsRecyclerView.layoutManager = mLinearLayoutManager
        conversationsRecyclerView.adapter = mConversationsAdapter
    }

    fun getChatMessages() {
        listenerRegistration = roomsCollection?.document(currentUser)
                ?.collection("conversations")
                ?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    if (firebaseFirestoreException != null) {
                        Log.e(TAG, "Listen failed!", firebaseFirestoreException)
                        return@addSnapshotListener
                    }

                    if (querySnapshot != null) {
                        for (document in querySnapshot) {
                            conversations.add(document)

                        }

                        for (change: DocumentChange in querySnapshot.documentChanges){
                            when(change.type){
                                DocumentChange.Type.ADDED -> onDocumentAdded(change)
                                DocumentChange.Type.MODIFIED -> onDocumentModified(change)
                                DocumentChange.Type.REMOVED -> onDocumentRemoved(change)
                            }
                        }
                    }

                }
    }

    private fun onDocumentAdded(change: DocumentChange) {
        conversations.add(change.document)
        mConversationsAdapter?.notifyItemInserted(conversations.size - 1)
        mConversationsAdapter?.itemCount
    }

    private fun onDocumentModified(change: DocumentChange) {
        if (change.oldIndex == change.newIndex){
            conversations.set(change.oldIndex, change.document)
            mConversationsAdapter?.notifyItemChanged(change.oldIndex)
        }else{
            conversations.remove(change.document)
            conversations.add(change.newIndex, change.document)
            mConversationsAdapter?.notifyItemRangeChanged(0, conversations.size)
        }

    }

    private fun onDocumentRemoved(change: DocumentChange) {
        conversations.removeAt(change.oldIndex)
        mConversationsAdapter?.notifyItemRemoved(change.oldIndex)
        mConversationsAdapter?.notifyItemRangeChanged(0, conversations.size)
    }
}
