package com.osfans.trime.ime.text

import com.osfans.trime.R
import com.osfans.trime.data.AppPrefs
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1

data class CandidateInfo(
	var matchInputBuffer: String? = null,
	var honzi: String? = null,
	var jyutping: String? = null,
	var pronOrder: String? = null,
	var sandhi: String? = null,
	var litColReading: String? = null,
	var properties: Properties = Properties(),
) {

	data class Properties(
		var partOfSpeech: String? = null,
		var register: String? = null,
		var label: String? = null,
		var normalized: String? = null,
		var written: String? = null,
		var vernacular: String? = null,
		var collocation: String? = null,
		var definition: Definition = Definition(),
	)

	data class Definition(
		var eng: String? = null,
		var urd: String? = null,
		var nep: String? = null,
		var hin: String? = null,
		var ind: String? = null,
	)

	private companion object {
		val columns = listOf(
			(CandidateInfo::matchInputBuffer).asProperty(),
			(CandidateInfo::honzi).asProperty(),
			(CandidateInfo::jyutping).asProperty(),
			(CandidateInfo::pronOrder).asProperty(),
			(CandidateInfo::sandhi).asProperty(),
			(CandidateInfo::litColReading).asProperty(),
			(CandidateInfo::properties)(Properties::partOfSpeech),
			(CandidateInfo::properties)(Properties::register),
			(CandidateInfo::properties)(Properties::label),
			(CandidateInfo::properties)(Properties::normalized),
			(CandidateInfo::properties)(Properties::written),
			(CandidateInfo::properties)(Properties::vernacular),
			(CandidateInfo::properties)(Properties::collocation),
			(CandidateInfo::properties)(Properties::definition)(Definition::eng),
			(CandidateInfo::properties)(Properties::definition)(Definition::urd),
			(CandidateInfo::properties)(Properties::definition)(Definition::nep),
			(CandidateInfo::properties)(Properties::definition)(Definition::hin),
			(CandidateInfo::properties)(Properties::definition)(Definition::ind),
		)

		val checkColumns = listOf(
			(CandidateInfo::properties)(Properties::partOfSpeech),
			(CandidateInfo::properties)(Properties::register),
			(CandidateInfo::properties)(Properties::normalized),
			(CandidateInfo::properties)(Properties::written),
			(CandidateInfo::properties)(Properties::vernacular),
			(CandidateInfo::properties)(Properties::collocation),
		)

		private val pref = AppPrefs.defaultInstance()
		private val prefMainLanguage = pref.typeDuck.mainLanguage
		private val prefDisplayLanguages = pref.typeDuck.displayLanguages
	}

	var isJyutpingOnly = true

	constructor(csv: String) : this() {
		isJyutpingOnly = false
		val charIterator = csv.iterator().asPeekable()
		val columnIterator = columns.iterator()
		var isQuoted = false
		var column = columnIterator.next()
		val value = StringBuilder()
		while (charIterator.hasNext()) {
			val char = charIterator.next()
			if (isQuoted) {
				if (char == '"') {
					if (charIterator.peek() == '"') {
						value.append(charIterator.next())
					} else {
						isQuoted = false
					}
				}
			} else if (value.isBlank() && char == '"') {
				isQuoted = true
			} else if (char == ',') {
				if (!columnIterator.hasNext()) {
					break
				}
				if (value.isNotBlank()) {
					this[column] = value.toString()
				}
				column = columnIterator.next()
				value.clear()
			} else {
				value.append(char)
			}
		}
		if (value.isNotBlank()) {
			this[column] = value.toString()
		}
		if (jyutping != null) {
			val jyutpingIterator = jyutping!!.iterator()
			value.clear()
			while (jyutpingIterator.hasNext()) {
				val char = jyutpingIterator.next()
				value.append(char)
				if (char.isDigit() && jyutpingIterator.hasNext()) {
					value.append(' ')
				}
			}
			jyutping = value.toString()
		}
	}

	fun getDefinition(language: Language): String? {
		return when (language) {
			Language.ENG -> properties.definition.eng
			Language.HIN -> properties.definition.hin
			Language.IND -> properties.definition.ind
			Language.NEP -> properties.definition.nep
			Language.URD -> properties.definition.urd
		}
	}

	val mainLanguage: String?
		get() = getDefinition(prefMainLanguage)

	val otherLanguages: List<String>
		get() = prefDisplayLanguages
			.filter { it != prefMainLanguage }
			.mapNotNull { getDefinition(it) }

	fun otherLanguagesWithNames(languageNames: Array<String>) =
		prefDisplayLanguages
			.filter { it != prefMainLanguage }
			.mapNotNull { language ->
				getDefinition(language)?.let { languageNames[language.ordinal] to it }
			}

	val formattedLabels: List<String>?
		get() = properties.label?.split(" ")?.map { "($it)" }

	val mainLanguageOrLabel: String?
		get() = if (isDictionaryEntry) mainLanguage else formattedLabels?.joinToString(" ")

	val otherLanguagesOrLabels: List<String>
		get() = if (isDictionaryEntry) otherLanguages else formattedLabels.orEmpty()

	val isDictionaryEntry: Boolean
		get() = !isJyutpingOnly && (checkColumns.any { this[it] != null } || prefDisplayLanguages.any { getDefinition(it) != null })
}

fun <T> Iterator<T>.asPeekable(): PeekableIterator<T> = PeekableIterator(this)

class PeekableIterator<T>(private val iterator: Iterator<T>) : Iterator<T> {
	private var nextElement: T? = null

	fun peek(): T? {
		if (nextElement == null) {
			nextElement = if (iterator.hasNext()) iterator.next() else null
		}
		return nextElement
	}

	override fun hasNext() = nextElement != null || iterator.hasNext()

	override fun next(): T {
		val result = nextElement ?: iterator.next()
		nextElement = null
		return result
	}
}

// https://discuss.kotlinlang.org/t/chaining-property-accessors-and-maintaining-both-the-ability-to-read-and-write/24322
operator fun <T, V> T.get(key: Property<T, V>): V = key.get(this)
operator fun <T, V> T.set(key: MutableProperty<T, V>, value: V) = key.set(this, value)

operator fun <T, U, V> KProperty1<T, U>.invoke(next: KMutableProperty1<U, V>): MutableProperty<T, V> = asProperty()(next.asProperty())
operator fun <T, U, V> Property<T, U>.invoke(next: KMutableProperty1<U, V>): MutableProperty<T, V> = this(next.asProperty())
operator fun <T, U, V> Property<T, U>.invoke(next: MutableProperty<U, V>): MutableProperty<T, V> = object : MutableProperty<T, V> {
	override fun get(receiver: T): V = next.get(this@invoke.get(receiver))
	override fun set(receiver: T, value: V) = next.set(this@invoke.get(receiver), value)
}

fun <T, V> KProperty1<T, V>.asProperty(): Property<T, V> = object : Property<T, V> {
	override fun get(receiver: T): V = this@asProperty.get(receiver)
}

fun <T, V> KMutableProperty1<T, V>.asProperty(): MutableProperty<T, V> = object : MutableProperty<T, V> {
	override fun get(receiver: T): V = this@asProperty.get(receiver)
	override fun set(receiver: T, value: V) = this@asProperty.set(receiver, value)
}

interface Property<in T, out V> {
	fun get(receiver: T): V
}

interface MutableProperty<in T, V> : Property<T, V> {
	fun set(receiver: T, value: V)
}
