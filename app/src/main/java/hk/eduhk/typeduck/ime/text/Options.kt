package hk.eduhk.typeduck.ime.text

enum class Language {
	ENG, HIN, IND, NEP, URD
}

enum class Size {
	SMALL, NORMAL, LARGE;

	val fontSize: Int
		get() = when (this) {
			SMALL -> 24
			NORMAL -> 28
			LARGE -> 36
		}

	val gap: Int
		get() = when (this) {
			SMALL -> 0
			NORMAL -> 8
			LARGE -> 8
		}

	val padding: Int
		get() = when (this) {
			SMALL -> 10
			NORMAL -> 10
			LARGE -> 20
		}
}
