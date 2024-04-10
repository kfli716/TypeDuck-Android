package hk.eduhk.typeduck.ui.components

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import com.google.android.material.textfield.TextInputLayout
import hk.eduhk.typeduck.R
import splitties.systemservices.inputMethodManager

class TestIMEPreference(context: Context, attrs: AttributeSet?): Preference(context, attrs) {
	private var inputLayout: TextInputLayout? = null
	private var input: EditText? = null

	override fun onBindViewHolder(holder: PreferenceViewHolder) {
		super.onBindViewHolder(holder)
		inputLayout = holder.itemView.findViewById(R.id.test_ime_input_layout)
		input = holder.itemView.findViewById(R.id.test_ime_input)
		input?.apply {
			setOnEditorActionListener { _, actionId, event ->
				(actionId == EditorInfo.IME_ACTION_DONE || event.keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP).also {
					if (it) clearFocus()
				}
			}
			setOnFocusChangeListener { _, hasFocus ->
				if (!inputMethodManager.isAcceptingText) {
					blur()
				}
				if (!hasFocus) {
					setText("")
					inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
				}
			}
		}
	}

	fun blur() {
		// https://stackoverflow.com/a/38185610
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) inputLayout?.isHintAnimationEnabled = false
		input?.clearFocus()
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) inputLayout?.isHintAnimationEnabled = true
	}
}
