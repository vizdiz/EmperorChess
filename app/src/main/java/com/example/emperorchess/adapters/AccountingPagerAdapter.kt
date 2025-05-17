package com.example.emperorchess.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.emperorchess.fragments.CustomersFragment
import com.example.emperorchess.fragments.InventoryFragment
import com.example.emperorchess.fragments.ReportsFragment
import com.example.emperorchess.fragments.SalesFragment

class AccountingPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 4

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> InventoryFragment()
            1 -> CustomersFragment()
            2 -> SalesFragment()
            3 -> ReportsFragment()
            else -> throw IllegalArgumentException("Invalid position $position")
        }
    }
} 