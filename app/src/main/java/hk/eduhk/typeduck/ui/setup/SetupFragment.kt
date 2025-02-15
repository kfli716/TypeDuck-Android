package hk.eduhk.typeduck.ui.setup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import hk.eduhk.typeduck.databinding.FragmentSetupBinding
import hk.eduhk.typeduck.ui.setup.SetupPage.Companion.isLastPage

class SetupFragment : Fragment() {
    private val viewModel: SetupViewModel by activityViewModels()
    private lateinit var binding: FragmentSetupBinding

    private val page: SetupPage by lazy { requireArguments().get("page") as SetupPage }

    private var isDone: Boolean = false
        set(new) {
            if (new && page.isLastPage())
                viewModel.isAllDone.value = true
            with(binding) {
                stepText.text = page.getStepText(requireContext())
                hintText.text = page.getHintText(requireContext())
                actionButton.visibility = if (new) View.GONE else View.VISIBLE
                actionButton.text = page.getButtonText(requireContext())
                actionButton.setOnClickListener { page.getButtonAction(requireContext()) }
                doneText.visibility = if (new) View.VISIBLE else View.GONE
            }
            field = new
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSetupBinding.inflate(inflater)
        sync()
        return binding.root
    }

    // Called on window focus changed
    fun sync() {
        isDone = page.isDone()
    }

    override fun onResume() {
        super.onResume()
        sync()
    }
}
