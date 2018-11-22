package com.ongea.activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.ongea.Constants
import com.ongea.R
import com.ongea.adapters.PeopleAdapdter
import kotlinx.android.synthetic.main.activity_people.*


class PeopleActivity : AppCompatActivity() {
    private var TAG = PeopleActivity::class.java.simpleName
    //firebase reference
    private var usersCollection: CollectionReference? = null
    private var roomsQuery: Query? = null
    private var listenerRegistration: ListenerRegistration? = null
    //firebase auth
    lateinit var firebaseAuth: FirebaseAuth
    // users lists
    private var users: MutableList<DocumentSnapshot> = mutableListOf()
    // recycler view layout manager
    lateinit var mLinearLayoutManager: LinearLayoutManager
    // users recycler adapter
    lateinit var mPeopleAdapter: PeopleAdapdter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_people)
        setSupportActionBar(toolbar)
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_whit)
        toolbar.setNavigationOnClickListener { finish() }
        // initialize firebase auth and current user
        firebaseAuth = FirebaseAuth.getInstance()
        //initialize firestore references
        initReferences()
    }

    override fun onDestroy() {
        super.onDestroy()
        listenerRegistration?.remove()
    }

    override fun onStart() {
        super.onStart()
        users.clear()
        getYourContacts()
        // set the recycler view
        setRecyclerView()
    }


    fun initReferences() {
        usersCollection = FirebaseFirestore.getInstance().collection(Constants.USERS)
    }

    fun setRecyclerView() {
        mPeopleAdapter = PeopleAdapdter(this, users)
        mLinearLayoutManager = LinearLayoutManager(this)
        usersRecyclerView.layoutManager = mLinearLayoutManager
        usersRecyclerView.adapter = mPeopleAdapter
    }

    fun getYourContacts() {
        usersCollection?.addSnapshotListener(EventListener { documentSnapshots, e ->

            if ( e != null) {
                Log.e(TAG, "Listen failed!",  e)
                return@EventListener
            }

            if (documentSnapshots != null) {
                Log.d("document snapshots", documentSnapshots.size().toString())
                for (change: DocumentChange in documentSnapshots.documentChanges){
                    when(change.type){
                        DocumentChange.Type.ADDED -> {
                            onDocumentAdded(change)
                        }
                        DocumentChange.Type.MODIFIED -> {
                            onDocumentModified(change)
                        }
                        DocumentChange.Type.REMOVED -> {
                            onDocumentRemoved(change)
                        }
                    }
                }

            }else{
                Log.d("document snapshots", "query snaphot empty")
            } })

    }

    private fun onDocumentAdded(change: DocumentChange) {
        users.add(change.document)
        mPeopleAdapter.notifyItemInserted(users.size - 1)
        mPeopleAdapter.itemCount
    }

    private fun onDocumentModified(change: DocumentChange) {
        if (change.oldIndex == change.newIndex){
            users.set(change.oldIndex, change.document)
            mPeopleAdapter.notifyItemChanged(change.oldIndex)
        }else{
            users.remove(change.document)
            users.add(change.newIndex, change.document)
            mPeopleAdapter.notifyItemRangeChanged(0, users.size)
        }

    }

    private fun onDocumentRemoved(change: DocumentChange) {
        users.removeAt(change.oldIndex)
        mPeopleAdapter.notifyItemRemoved(change.oldIndex)
        mPeopleAdapter.notifyItemRangeChanged(0, users.size)
    }
}
