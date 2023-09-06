package com.osfans.trime.ui.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import androidx.preference.Preference
import com.osfans.trime.data.LibraryLicenseDao
import com.osfans.trime.ui.components.PaddingPreferenceFragment
import kotlinx.coroutines.launch

class LicenseFragment : PaddingPreferenceFragment() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        lifecycleScope.launch {
            LibraryLicenseDao.getAll().onSuccess { licenses ->
                val context = preferenceManager.context
                val screen = preferenceManager.createPreferenceScreen(context)
                licenses.forEach { license ->
                    screen.addPreference(
                        Preference(context).apply {
                            isIconSpaceReserved = false
                            title = license.libraryName
                            summary = license.artifactId.group
                            setOnPreferenceClickListener {
                                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(license.licenseUrl)))
                                true
                            }
                        }
                    )
                }
                preferenceScreen = screen
            }
        }
    }
}
