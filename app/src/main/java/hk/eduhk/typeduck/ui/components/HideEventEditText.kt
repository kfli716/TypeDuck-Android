package hk.eduhk.typeduck.ui.components

import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import androidx.appcompat.widget.AppCompatEditText

// https://stackoverflow.com/a/50659525
class HideEventEditText(context: Context, attrs: AttributeSet?): AppCompatEditText(context, attrs) {
	private var mEditorListener: OnEditorActionListener? = null

	override fun setOnEditorActionListener(listener: OnEditorActionListener) {
		mEditorListener = listener
		super.setOnEditorActionListener(listener)
	}

	override fun onKeyPreIme(keyCode: Int, event: KeyEvent): Boolean {
		if (event.keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
			mEditorListener?.onEditorAction(this, android.R.id.closeButton, event)
		}
		return super.onKeyPreIme(keyCode, event)
	}
}
