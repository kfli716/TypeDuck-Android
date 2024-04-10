package hk.eduhk.typeduck.ui.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import hk.eduhk.typeduck.databinding.FragmentOnboardingBinding

class OnboardingFragment: Fragment() {
    private lateinit var binding: FragmentOnboardingBinding
    private val pageNumber by lazy { requireArguments().get("page") as Int }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentOnboardingBinding.inflate(inflater)
        sync()
        return binding.root
    }

    // Called on window focus changed
    fun sync() {
        with(binding) {
            val page = OnboardingPage.get(pageNumber)
            headingText.text = page.getHeadingText(requireContext())
            contentText.text = page.getContentText(requireContext())
            if (page is OnboardingPageWithButton) {
                actionButton.visibility = View.VISIBLE
                actionButton.text = page.getButtonText(requireContext())
                actionButton.setOnClickListener { page.getButtonAction(requireActivity()) }
            } else {
                actionButton.visibility = View.GONE
            }
        }
    }

    override fun onResume() {
        super.onResume()
        sync()
    }
}
