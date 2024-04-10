package hk.eduhk.typeduck.ui.components

import android.content.Context
import android.util.AttributeSet
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import hk.eduhk.typeduck.R
import hk.eduhk.typeduck.util.userKnowsChinese

class BinarySegmentPreference(context: Context, attrs: AttributeSet?): Preference(context, attrs) {
	private var segmentTextLeft: String? = null
	private var segmentTextRight: String? = null

	init {
		context.obtainStyledAttributes(attrs, R.styleable.BinarySegmentPreference).apply {
			segmentTextLeft = getString(R.styleable.BinarySegmentPreference_segmentLeftText)
			segmentTextRight = getString(R.styleable.BinarySegmentPreference_segmentRightText)
			recycle()
		}
	}

	override fun onBindViewHolder(holder: PreferenceViewHolder) {
		super.onBindViewHolder(holder)
		holder.itemView.apply {
			findViewById<TextView>(R.id.segment_left).text = segmentTextLeft
			findViewById<TextView>(R.id.segment_right).text = segmentTextRight
			findViewById<SwitchCompat>(R.id.segment).apply {
				sharedPreferences?.apply {
					isChecked = getBoolean(key, !userKnowsChinese)
					setOnCheckedChangeListener { _, isChecked ->
						edit().putBoolean(key, isChecked).apply()
					}
				}
			}
		}
	}
}
