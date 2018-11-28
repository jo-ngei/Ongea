package com.ongea.fragments


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.ongea.Constants

import com.ongea.R
import com.ongea.adapters.PeopleAdapdter
import kotlinx.android.synthetic.main.fragment_people.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 *
 */
class PeopleFragment : Fragment() {
    private var TAG = PeopleFragment::class.java.simpleName
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


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        var view = inflater.inflate(R.layout.fragment_people, container, false)
        // initialize firebase auth and current user
        firebaseAuth = FirebaseAuth.getInstance()
        //initialize firestore references
        initReferences()
        return  view
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
        mPeopleAdapter = PeopleAdapdter(context!!, users)
        mLinearLayoutManager = LinearLayoutManager(context)
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
