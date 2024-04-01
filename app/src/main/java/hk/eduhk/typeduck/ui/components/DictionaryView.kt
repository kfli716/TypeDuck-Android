package hk.eduhk.typeduck.ui.components

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import hk.eduhk.typeduck.R
import hk.eduhk.typeduck.data.AppPrefs
import hk.eduhk.typeduck.databinding.DictionaryEntryBinding
import hk.eduhk.typeduck.databinding.DictionaryViewBinding
import hk.eduhk.typeduck.databinding.TableRowBinding
import hk.eduhk.typeduck.ime.text.CandidateEntry
import hk.eduhk.typeduck.ime.text.ComputedCandidate
import hk.eduhk.typeduck.util.dp2px
import splitties.views.bottomPadding
import splitties.views.dsl.core.lastChild
import splitties.views.dsl.core.margin
import splitties.views.horizontalPadding
import splitties.views.padding
import splitties.views.rightPadding
import splitties.views.topPadding
import splitties.views.textColorResource
import kotlin.math.roundToInt

class DictionaryView(context: Context, attrs: AttributeSet?): RelativeLayout(context, attrs) {
	fun setup(info: ComputedCandidate.Word, closeAction: OnClickListener) {
		removeAllViews()
		scale = AppPrefs.defaultInstance().typeDuck.candidateFontSize.scale
		val inflater = LayoutInflater.from(context)
		DictionaryViewBinding.inflate(inflater, this, true).apply {
			info.entries.filter { it.isDictionaryEntry }.forEach {
				DictionaryEntryBinding.inflate(inflater, dictionaryView, true).apply {
					titleStack.apply {
						it.honzi?.let {
							addTextView(context, it, true, 28, gap = 32 to 10)
						}
						it.jyutping?.let {
							addTextView(context, it, false, 17, gap = 32 to 10)
						}
						val pronunciationType = mutableListOf<String>()
						if (it.sandhi == "1") {
							pronunciationType += "changed tone 變音"
						}
						CandidateEntry.litColReading[it.litColReading]?.let {
							pronunciationType += it
						}
						if (pronunciationType.isNotEmpty()) {
							addTextView(context, "(${pronunciationType.joinToString()})", false, 13, gap = 32 to 10)
						}
						lastChild.removeMargin()
					}
					definitionStack.apply {
						topPadding = 8.scaled.roundToInt()
						it.properties.partOfSpeech?.split(" ")?.forEachWithIsLast { it, isLast ->
							addTextView(context, CandidateEntry.partOfSpeech[it] ?: it, false, 13, bordered = true, gap = (if (isLast) 24 else 8) to 8)
						}
						CandidateEntry.register[it.properties.register]?.let {
							addTextView(context, it, false, 15, Typeface.ITALIC, gap = 24 to 8)
						}
						it.formattedLabels?.forEachWithIsLast { it, isLast ->
							addTextView(context, it, false, 15, gap = (if (isLast) 24 else 8) to 8)
						}
						it.mainLanguage?.let {
							addTextView(context, it, true, 17, gap = 24 to 8)
						}
						lastChild.removeMargin()
					}
					with(
						CandidateEntry.otherData.mapNotNull { (key, value) ->
							value.get(it)?.run {
								key to replace('，', '\n')
							}
						}
					) {
						if (isEmpty()) {
							otherDataStack.visibility = View.GONE
						} else {
							otherDataStack.topPadding = 16.scaled.roundToInt()
							feedTo(otherDataStack, inflater)
						}
					}
					with(it.otherLanguagesWithNames) {
						if (isEmpty()) {
							otherLanguageLabel.visibility = View.GONE
							otherLanguageStack.visibility = View.GONE
						} else {
							otherLanguageLabel.apply {
								topPadding = 20.scaled.roundToInt()
								text = "More Languages"
								textColorResource = R.color.text_color_primary
								textSize = 17.scaled
								setTypeface(null, Typeface.BOLD)
							}
							otherLanguageStack.topPadding = 8.scaled.roundToInt()
							feedTo(otherLanguageStack, inflater)
						}
					}
					root.bottomPadding = 40.scaled.roundToInt()
				}
			}
			dictionaryView.lastChild.padding = 0
		}
		addView(
			ImageView(context).apply {
				layoutParams = LayoutParams(60.scaled.toInt(), 60.scaled.toInt()).apply {
					addRule(ALIGN_PARENT_TOP)
					addRule(ALIGN_PARENT_RIGHT)
					topMargin = 32
					rightMargin = 32
				}
				setOnClickListener(closeAction)
				setImageDrawable(
					ResourcesCompat.getDrawable(resources, R.drawable.ic_baseline_close_24, null)?.apply {
						setColor(true)
					}
				)
				setOnTouchListener { view, event ->
					when (event.actionMasked) {
						MotionEvent.ACTION_DOWN -> {
							drawable.setColor(false)
						}
						MotionEvent.ACTION_UP -> {
							drawable.setColor(true)
							view.performClick()
						}
						MotionEvent.ACTION_OUTSIDE,
						MotionEvent.ACTION_CANCEL -> {
							drawable.setColor(true)
						}
					}
					return@setOnTouchListener true
				}
			}
		)
	}

	private var scale = 0f

	private inline val Int.scaled: Float
		get() = this * scale

	private inline fun <T> List<T>.forEachWithIsLast(callback: (T, isLast: Boolean) -> Unit) =
		forEachIndexed { index, element ->
			callback(element, index == size - 1)
		}

	private inline fun ViewGroup.addTextView(context: Context?, string: String, isPrimary: Boolean, size: Int, style: Int? = null, bordered: Boolean = false, gap: Pair<Int, Int> = 0 to 0) =
		addView(
			TextView(context).apply {
				text = string
				textColorResource = if (isPrimary) R.color.text_color_primary else R.color.text_color_secondary
				textSize = size.scaled
				style?.let {
					setTypeface(null, it)
				}
				if (bordered) {
					setBackgroundResource(R.drawable.text_view_border)
					horizontalPadding = dp2px(4)
					topPadding = dp2px(-4)
				}
				layoutParams = MarginLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
					rightMargin = gap.first.scaled.roundToInt()
					bottomMargin = gap.second.scaled.roundToInt()
				}
			}
		)

	private inline fun List<Pair<String, String>>.feedTo(viewGroup: ViewGroup, inflater: LayoutInflater) =
		forEach { (key, value) ->
			TableRowBinding.inflate(inflater, viewGroup, true).apply {
				keyTextView.apply {
					text = key
					textSize = 17.scaled
					setTypeface(null, Typeface.BOLD)
					rightPadding = 24.scaled.roundToInt()
				}
				valueTextView.apply {
					text = value
					textSize = 17.scaled
				}
			}
		}

	private inline fun View.removeMargin() {
		(layoutParams as? MarginLayoutParams)?.margin = 0
	}

	private inline fun Drawable.setColor(isPrimary: Boolean) =
		setTint(ResourcesCompat.getColor(resources, if (isPrimary) R.color.text_color_primary else R.color.text_color_secondary, null))
}
