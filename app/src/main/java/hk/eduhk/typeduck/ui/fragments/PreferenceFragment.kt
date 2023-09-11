package hk.eduhk.typeduck.ui.fragments

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.preference.ListPreference
import androidx.preference.MultiSelectListPreference
import androidx.preference.Preference
import androidx.preference.get
import com.blankj.utilcode.util.ResourceUtils
import com.google.android.material.textfield.TextInputLayout
import hk.eduhk.typeduck.R
import hk.eduhk.typeduck.core.Rime
import hk.eduhk.typeduck.data.AppPrefs
import hk.eduhk.typeduck.data.DataManager
import hk.eduhk.typeduck.ime.core.Trime
import hk.eduhk.typeduck.ime.text.Language
import hk.eduhk.typeduck.ime.text.Size
import hk.eduhk.typeduck.ui.components.PaddingPreferenceFragment
import hk.eduhk.typeduck.util.dp2px
import hk.eduhk.typeduck.util.withLoadingDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.EnumSet

class PreferenceFragment :
    PaddingPreferenceFragment(),
    SharedPreferences.OnSharedPreferenceChangeListener {

    private lateinit var languageNames: Array<String>
    private lateinit var sizeNames: Array<String>

    override fun onResume() {
        super.onResume()
        preferenceScreen.sharedPreferences?.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        preferenceScreen.sharedPreferences?.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preference, rootKey)
        languageNames = resources.getStringArray(R.array.pref_languages)
        sizeNames = resources.getStringArray(R.array.pref_sizes)
        with(preferenceScreen) {
            get<Preference>("pref_refresh")?.setOnPreferenceClickListener {
                lifecycleScope.withLoadingDialog(context) {
                    withContext(Dispatchers.IO) {
                        ResourceUtils.copyFileFromAssets("rime", DataManager.sharedDataDir.absolutePath)
                        Rime.syncRimeUserData()
                        Rime.deployRime()
                    }
                }
                true
            }
            get<Preference>("pref_test_ime")?.setOnPreferenceClickListener {
                val editText = EditText(context).apply {
                    inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
                }
                with(
                    AlertDialog.Builder(context, R.style.dialog_theme)
                        .setTitle(R.string.pref_test_ime)
                        .setView(
                            TextInputLayout(context).apply {
                                setPadding(dp2px(20), dp2px(10), dp2px(20), 0)
                                hint = getString(R.string.pref_test_ime_placeholder)
                                addView(editText)
                            }
                        )
                        .setNegativeButton(R.string.close, null)
                        .create()
                ) {
                    editText.setOnEditorActionListener { _, actionId, _ ->
                        (actionId == EditorInfo.IME_ACTION_DONE).also {
                            if (it) dismiss()
                        }
                    }
                    setOnShowListener {
                        editText.requestFocus()
                    }
                    window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
                    show()
                }
                true
            }
            sharedPreferences?.run {
                val typeDuck = AppPrefs(this).typeDuck
                val displayLanguages = typeDuck.displayLanguages
                val mainLanguage = typeDuck.mainLanguage
                val candidateFontSize = typeDuck.candidateFontSize
                val candidateGap = typeDuck.candidateGap
                val languageValues = Language.values().map { it.name }.toTypedArray()
                get<MultiSelectListPreference>(AppPrefs.TypeDuck.DISPLAY_LANGUAGES)?.also {
                    it.entries = languageNames
                    it.entryValues = languageValues
                    it.values = displayLanguages.map { it.name }.toSet()
                    it.summary = displayLanguages.joinToString { languageNames[it.ordinal] }
                    it.setOnPreferenceChangeListener { _, value ->
                        !(value as Set<*>).isEmpty().also { empty ->
                            if (empty) {
                                typeDuck.displayLanguages = EnumSet.of(mainLanguage)
                                it.values = setOf(mainLanguage.name)
                            }
                        }
                    }
                }
                get<ListPreference>(AppPrefs.TypeDuck.MAIN_LANGUAGE)?.also {
                    it.entries = languageNames
                    it.entryValues = languageValues
                    it.value = mainLanguage.name
                    it.summary = languageNames[mainLanguage.ordinal]
                }
                val sizeValues = Size.values().map { it.name }.toTypedArray()
                get<ListPreference>(AppPrefs.TypeDuck.CANDIDATE_FONT_SIZE)?.also {
                    it.entries = sizeNames
                    it.entryValues = sizeValues
                    it.value = candidateFontSize.name
                    it.summary = sizeNames[candidateFontSize.ordinal]
                }
                get<ListPreference>(AppPrefs.TypeDuck.CANDIDATE_GAP)?.also {
                    it.entries = sizeNames
                    it.entryValues = sizeValues
                    it.value = candidateGap.name
                    it.summary = sizeNames[candidateGap.ordinal]
                }
            }
            get<Preference>("pref_typeduck_source")?.setOnPreferenceClickListener {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/TypeDuck-HK/TypeDuck-Android")))
                true
            }
            get<Preference>("pref_trime_source")?.setOnPreferenceClickListener {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/osfans/trime")))
                true
            }
            get<Preference>("pref_license")?.setOnPreferenceClickListener {
                findNavController().navigate(R.id.action_preferenceFragment_to_licenseFragment)
                true
            }
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (sharedPreferences == null) return
        val typeDuck = AppPrefs.initDefault(sharedPreferences).typeDuck
        with(preferenceScreen) {
            when (key) {
                AppPrefs.TypeDuck.DISPLAY_LANGUAGES -> {
                    val displayLanguages = typeDuck.displayLanguages
                    var mainLanguage = typeDuck.mainLanguage
                    get<MultiSelectListPreference>(AppPrefs.TypeDuck.DISPLAY_LANGUAGES)?.also {
                        it.summary = displayLanguages.joinToString { languageNames[it.ordinal] }
                    }
                    if (mainLanguage !in displayLanguages) {
                        get<ListPreference>(AppPrefs.TypeDuck.MAIN_LANGUAGE)?.also {
                            mainLanguage = displayLanguages.first()
                            typeDuck.mainLanguage = mainLanguage
                            it.value = mainLanguage.name
                            it.summary = languageNames[mainLanguage.ordinal]
                        }
                    }
                    true
                }
                AppPrefs.TypeDuck.MAIN_LANGUAGE -> {
                    val displayLanguages = typeDuck.displayLanguages
                    val mainLanguage = typeDuck.mainLanguage
                    get<ListPreference>(AppPrefs.TypeDuck.MAIN_LANGUAGE)?.also {
                        it.summary = languageNames[mainLanguage.ordinal]
                    }
                    if (mainLanguage !in displayLanguages) {
                        get<MultiSelectListPreference>(AppPrefs.TypeDuck.DISPLAY_LANGUAGES)?.also {
                            displayLanguages.add(mainLanguage)
                            typeDuck.displayLanguages = displayLanguages
                            it.values = displayLanguages.map { it.name }.toSet()
                            it.summary = displayLanguages.joinToString { languageNames[it.ordinal] }
                        }
                    }
                    true
                }
                AppPrefs.TypeDuck.CANDIDATE_FONT_SIZE -> {
                    get<ListPreference>(AppPrefs.TypeDuck.CANDIDATE_FONT_SIZE)?.also {
                        it.summary = sizeNames[typeDuck.candidateFontSize.ordinal]
                    }
                }
                AppPrefs.TypeDuck.CANDIDATE_GAP -> {
                    get<ListPreference>(AppPrefs.TypeDuck.CANDIDATE_GAP)?.also {
                        it.summary = sizeNames[typeDuck.candidateGap.ordinal]
                    }
                }
                else -> {}
            }
        }
        Trime.getServiceOrNull()?.initKeyboard()
    }
}
