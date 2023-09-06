package com.osfans.trime.ui.fragments

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
import com.osfans.trime.R
import com.osfans.trime.core.Rime
import com.osfans.trime.data.AppPrefs
import com.osfans.trime.data.DataManager
import com.osfans.trime.ime.core.Trime
import com.osfans.trime.ime.text.Language
import com.osfans.trime.ui.components.PaddingPreferenceFragment
import com.osfans.trime.util.withLoadingDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.EnumSet

class PreferenceFragment :
    PaddingPreferenceFragment(),
    SharedPreferences.OnSharedPreferenceChangeListener {

    private lateinit var languageNames: Array<String>

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
                    inputType = InputType.TYPE_CLASS_TEXT
                }
                with(
                    AlertDialog.Builder(context, R.style.dialog_theme)
                        .setTitle(R.string.pref_test_ime)
                        .setView(
                            TextInputLayout(context).apply {
                                setPadding(30, 15, 30, 0)
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
                get<MultiSelectListPreference>(AppPrefs.TypeDuck.DISPLAY_LANGUAGES)?.also {
                    it.entries = languageNames
                    it.entryValues = Language.values().map { it.name }.toTypedArray()
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
                    it.entries = displayLanguages.map { languageNames[it.ordinal] }.toTypedArray()
                    it.entryValues = displayLanguages.map { it.name }.toTypedArray()
                    it.value = mainLanguage.name
                    it.summary = languageNames[mainLanguage.ordinal]
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
        val trime = Trime.getServiceOrNull()
        when (key) {
            AppPrefs.TypeDuck.DISPLAY_LANGUAGES -> {
                sharedPreferences?.run {
                    val typeDuck = AppPrefs(this).typeDuck
                    val displayLanguages = typeDuck.displayLanguages
                    var mainLanguage = typeDuck.mainLanguage
                    with(preferenceScreen) {
                        get<MultiSelectListPreference>(AppPrefs.TypeDuck.DISPLAY_LANGUAGES)?.also {
                            it.summary = displayLanguages.joinToString { languageNames[it.ordinal] }
                        }
                        get<ListPreference>(AppPrefs.TypeDuck.MAIN_LANGUAGE)?.also {
                            it.entries = displayLanguages.map { languageNames[it.ordinal] }.toTypedArray()
                            it.entryValues = displayLanguages.map { it.name }.toTypedArray()
                            if (mainLanguage !in displayLanguages) {
                                mainLanguage = displayLanguages.first()
                                it.value = mainLanguage.name
                                typeDuck.mainLanguage = mainLanguage
                            }
                            it.summary = languageNames[mainLanguage.ordinal]
                        }
                    }
                }
                trime?.resetKeyboard()
            }
            AppPrefs.TypeDuck.MAIN_LANGUAGE -> {
                sharedPreferences?.run {
                    preferenceScreen.get<ListPreference>(AppPrefs.TypeDuck.MAIN_LANGUAGE)?.also {
                        it.summary = languageNames[AppPrefs(this).typeDuck.mainLanguage.ordinal]
                    }
                }
                trime?.resetKeyboard()
            }
            AppPrefs.TypeDuck.VISUAL_FEEDBACK,
            AppPrefs.TypeDuck.SYMBOLS_ON_QWERTY -> {
                trime?.resetKeyboard()
            }
        }
    }
}
