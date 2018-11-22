package com.ongea.adapters

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import com.ongea.fragments.ConversationsFragment
import com.ongea.fragments.StoriesFragment

class MainPagerAdapter: FragmentPagerAdapter {

    constructor(fm: FragmentManager?) : super(fm)

    override fun getItem(position: Int): Fragment {
        return when(position){
            0 -> {
                ConversationsFragment()
            }else -> {
                StoriesFragment()
            }

        }

    }

    override fun getCount(): Int {
        return 2
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return when(position){
            0 -> {
                "Chats"
            }else -> {
                "Stories"
            }
        }
    }
}