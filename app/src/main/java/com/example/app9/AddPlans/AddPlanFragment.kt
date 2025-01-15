package com.example.app9.AddPlans

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.app9.R
import com.google.android.material.tabs.TabLayout

class AddPlanFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_plan, container, false)

        val tabLayout = view.findViewById<TabLayout>(R.id.tabLayout)
        val fragmentContainer = view.findViewById<ViewGroup>(R.id.fragmentContainer)

        // 添加导航栏选项
        tabLayout.addTab(tabLayout.newTab().setText("一次性任务"))
        tabLayout.addTab(tabLayout.newTab().setText("重复任务"))


        // 默认加载一次性任务界面
        loadFragment(OneTimePlanFragment())

        // 监听导航栏的切换事件
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> loadFragment(OneTimePlanFragment())
                    1 -> loadFragment(RepeatPlanFragment())

                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        return view
    }

    private fun loadFragment(fragment: Fragment) {
        childFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}