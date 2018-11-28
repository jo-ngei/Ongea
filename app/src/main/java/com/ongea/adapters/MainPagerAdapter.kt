package com.ongea.adapters

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.app.FragmentStatePagerAdapter
import com.ongea.fragments.ConversationsFragment
import com.ongea.fragments.PeopleFragment
import com.ongea.fragments.ProfileFragment

class MainPagerAdapter: FragmentStatePagerAdapter {

    constructor(fm: FragmentManager?) : super(fm)

    override fun getItem(position: Int): Fragment {
        return when(position){
            0 -> {
                ConversationsFragment()
            } 1 ->{
                PeopleFragment()
            }else -> {
                ProfileFragment()
            }

        }

    }

    override fun getCount(): Int {
        return 3
    }

}