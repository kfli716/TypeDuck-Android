package hk.eduhk.typeduck.ui.onboarding

import android.content.Context
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import hk.eduhk.typeduck.data.AppPrefs
import hk.eduhk.typeduck.databinding.ActivityOnboardingBinding
import hk.eduhk.typeduck.util.InputMethodUtils
import hk.eduhk.typeduck.util.applyTranslucentSystemBars
import hk.eduhk.typeduck.util.setLocale

class OnboardingActivity: FragmentActivity() {
    private lateinit var viewPager: ViewPager2

    companion object {
        private var binaryCount = false
        fun shouldSetup() = !binaryCount && !InputMethodUtils.checkIsTypeDuckSelected()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyTranslucentSystemBars()
        val binding = ActivityOnboardingBinding.inflate(layoutInflater)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, windowInsets ->
            val sysBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.root.setPadding(
                sysBars.left, sysBars.top, sysBars.right, sysBars.bottom
            )
            windowInsets
        }
        setContentView(binding.root)
        binding.skipButton.apply {
            setOnClickListener {
                finish()
            }
        }
        viewPager = binding.viewPager
        viewPager.adapter = Adapter()
        TabLayoutMediator(binding.pageControl, binding.viewPager) { tab, position -> }.attach()
        viewPager.currentItem = if (InputMethodUtils.checkIsTypeDuckEnabled()) OnboardingPage.numberOfPages - 1 else 0
        binaryCount = true
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        val fragment = supportFragmentManager.findFragmentByTag("f${viewPager.currentItem}")
        (fragment as OnboardingFragment).sync()
    }

    private inner class Adapter: FragmentStateAdapter(this) {
        override fun getItemCount() = OnboardingPage.numberOfPages

        override fun createFragment(position: Int): Fragment =
            OnboardingFragment().apply {
                arguments = bundleOf("page" to position)
            }
    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(newBase?.setLocale(AppPrefs.defaultInstance().typeDuck.interfaceLanguage))
    }
}
