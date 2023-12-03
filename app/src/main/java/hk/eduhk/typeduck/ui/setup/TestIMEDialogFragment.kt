package hk.eduhk.typeduck.ui.setup

import android.app.Dialog
import android.os.Bundle
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import hk.eduhk.typeduck.R
import hk.eduhk.typeduck.databinding.TestImeDialogLayoutBinding

class TestIMEDialogFragment : DialogFragment() {
	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
		val activity = requireActivity()
		val binding = TestImeDialogLayoutBinding.inflate(layoutInflater)
		val editText = binding.testImeInput
		return AlertDialog.Builder(activity, R.style.dialog_theme)
			.setTitle(R.string.pref_test_ime)
			.setView(binding.root)
			.setNegativeButton(R.string.close, null)
			.create()
			.apply {
				editText.setOnEditorActionListener { _, actionId, _ ->
					(actionId == EditorInfo.IME_ACTION_DONE).also {
						if (it) dismiss()
					}
				}
				setOnShowListener {
					editText.requestFocus()
				}
				window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
			}
	}
}
