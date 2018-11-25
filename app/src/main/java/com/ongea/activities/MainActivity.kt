package com.ongea.activities

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import butterknife.ButterKnife
import com.google.firebase.auth.FirebaseAuth
import com.ongea.R
import com.ongea.adapters.MainPagerAdapter
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    lateinit var firebaseAuth: FirebaseAuth
    lateinit var mainPagerAdapter: MainPagerAdapter
    private var USER_ID = "user id"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ButterKnife.bind(this)
        setSupportActionBar(toolbar)

        firebaseAuth = FirebaseAuth.getInstance()
        mainPagerAdapter = MainPagerAdapter(supportFragmentManager);
        viewPager.adapter = mainPagerAdapter
        tabs.setupWithViewPager(viewPager)

        viewPager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabs))
        tabs.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(viewPager))

    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_main, menu);
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val id = item?.itemId;

        if (id == R.id.action_people){
            val intent = Intent(this, PeopleActivity::class.java);
            startActivity(intent)
        }

        if (id == R.id.action_profile){
            val intent = Intent(this, ProfileActivity::class.java);
            intent.putExtra(this.USER_ID, firebaseAuth.currentUser!!.uid)
            startActivity(intent)
        }
        return super.onOptionsItemSelected(item)
    }
}
