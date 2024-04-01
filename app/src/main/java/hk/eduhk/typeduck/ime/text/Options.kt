package hk.eduhk.typeduck.ime.text

import hk.eduhk.typeduck.ime.keyboard.Keyboard
import hk.eduhk.typeduck.util.dp2px
import hk.eduhk.typeduck.util.sp2px

enum class Language(val displayName: String) {
	ENG("English"),
	HIN("Hindi"),
	IND("Indonesian"),
	NEP("Nepali"),
	URD("Urdu")
}

enum class Size {
	SMALL, NORMAL, LARGE;

	val scale: Float
		get() = when (this) {
			SMALL -> 1.25f
			NORMAL -> 1.5f
			LARGE -> 2f
		}

	val fontSize: Float
		get() = sp2px(when (this) {
			SMALL -> 20f
			NORMAL -> 24f
			LARGE -> 30f
		}) * Keyboard.adjustRatioSmall

	val gap: Float
		get() = dp2px(when (this) {
			SMALL -> 0f
			NORMAL -> 8f
			LARGE -> 8f
		}) * Keyboard.adjustRatioSmall

	val padding: Float
		get() = dp2px(when (this) {
			SMALL -> 10f
			NORMAL -> 10f
			LARGE -> 20f
		}) * Keyboard.adjustRatioSmall
}
