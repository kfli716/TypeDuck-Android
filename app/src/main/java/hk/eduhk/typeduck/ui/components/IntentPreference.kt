package hk.eduhk.typeduck.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.AttributeSet
import androidx.preference.Preference
import hk.eduhk.typeduck.R

class IntentPreference(context: Context, attrs: AttributeSet?) : Preference(context, attrs) {
	private val url = context.obtainStyledAttributes(attrs, R.styleable.IntentPreference).run {
		getString(R.styleable.IntentPreference_jumpToUrl).also {
			recycle()
		}
	}
	override fun onClick() {
		super.onClick()
		context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
	}
}
