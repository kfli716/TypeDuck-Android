package hk.eduhk.typeduck.ui.setup

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import hk.eduhk.typeduck.R
import hk.eduhk.typeduck.databinding.ActivitySetupBinding
import hk.eduhk.typeduck.ui.setup.SetupPage.Companion.firstUndonePage
import hk.eduhk.typeduck.ui.setup.SetupPage.Companion.isLastPage
import hk.eduhk.typeduck.util.applyTranslucentSystemBars

class SetupActivity : FragmentActivity() {
    private lateinit var viewPager: ViewPager2
    private val viewModel: SetupViewModel by viewModels()

    companion object {
        private var binaryCount = false

        fun shouldSetup() = !binaryCount && SetupPage.hasUndonePage()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyTranslucentSystemBars()
        val binding = ActivitySetupBinding.inflate(layoutInflater)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, windowInsets ->
            val sysBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.root.setPadding(
                sysBars.left, sysBars.top, sysBars.right, sysBars.bottom
            )
            windowInsets
        }
        setContentView(binding.root)
        val prevButton = binding.prevButton.apply {
            text = getString(R.string.setup_prev)
            setOnClickListener { viewPager.currentItem = viewPager.currentItem - 1 }
        }
        binding.skipButton.apply {
            text = getString(R.string.setup_skip)
            setOnClickListener {
                finish()
            }
        }
        val nextButton = binding.nextButton.apply {
            setOnClickListener {
                if (viewPager.currentItem != SetupPage.values().size - 1) {
                    viewPager.currentItem = viewPager.currentItem + 1
                } else finish()
            }
        }
        viewPager = binding.viewpager
        viewPager.adapter = Adapter()
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                // Manually call following observer when page changed
                // intentionally before changing the text of nextButton
                viewModel.isAllDone.value = viewModel.isAllDone.value
                // Hide prev button for the first page
                prevButton.visibility = if (position != 0) View.VISIBLE else View.GONE
                nextButton.text =
                    getString(
                        if (position.isLastPage())
                            R.string.setup_done else R.string.setup_next
                    )
            }
        })
        viewModel.isAllDone.observe(this) { allDone ->
            nextButton.apply {
                // Hide next button for the last page when allDone == false
                (allDone || !viewPager.currentItem.isLastPage()).let {
                    visibility = if (it) View.VISIBLE else View.GONE
                }
            }
            binding.skipButton.apply {
                // Hide skip button for the last page when allDone == true
                (!allDone || !viewPager.currentItem.isLastPage()).let {
                    visibility = if (it) View.VISIBLE else View.GONE
                }
            }
        }
        // Skip to undone page
        firstUndonePage()?.let { viewPager.currentItem = it.ordinal }
        binaryCount = true
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        val fragment = supportFragmentManager.findFragmentByTag("f${viewPager.currentItem}")
        (fragment as SetupFragment).sync()
    }

    private inner class Adapter : FragmentStateAdapter(this) {
        override fun getItemCount(): Int = SetupPage.values().size

        override fun createFragment(position: Int): Fragment =
            SetupFragment().apply {
                arguments = bundleOf("page" to SetupPage.values()[position])
            }
    }
}
