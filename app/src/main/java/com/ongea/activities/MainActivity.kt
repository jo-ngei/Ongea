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

    private var tabIcons = listOf<Int>(R.drawable.ic_chats, R.drawable.ic_people, R.drawable.ic_user_white)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        firebaseAuth = FirebaseAuth.getInstance()
        mainPagerAdapter = MainPagerAdapter(supportFragmentManager);
        viewPager.adapter = mainPagerAdapter
        tabs.setupWithViewPager(viewPager)

        viewPager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabs))
        tabs.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(viewPager))

        setUpTabIcons()

    }

    private fun setUpTabIcons(){
        tabs.getTabAt(0)!!.setIcon(tabIcons[0])
        tabs.getTabAt(1)!!.setIcon(tabIcons[1])
        tabs.getTabAt(2)!!.setIcon(tabIcons[2])

    }
}
