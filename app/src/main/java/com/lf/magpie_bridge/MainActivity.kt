package com.lf.magpie_bridge

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.lf.magpie_bridge.databinding.ActivityMainBinding

class MainActivity : FragmentActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 设置适配器
        val adapter = ViewPagerAdapter(supportFragmentManager)
        binding.viewPager.adapter = adapter
    }

}

class ViewPagerAdapter(fm: FragmentManager) :
    FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getCount(): Int {
        return 2 // 两个页面
    }

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> NativeClientFragment()  // 第一个页面是空页面
            1 -> WebClientFragment() // 第二个页面包含 WebView
            else -> NativeClientFragment() // 默认返回空页面
        }
    }
}
