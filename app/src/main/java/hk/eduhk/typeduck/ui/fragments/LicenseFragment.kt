package hk.eduhk.typeduck.ui.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import androidx.preference.Preference
import hk.eduhk.typeduck.data.LibraryLicenseDao
import hk.eduhk.typeduck.ui.components.PaddingPreferenceFragment
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
                            summary = license.url
                            setOnPreferenceClickListener {
                                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(license.url)))
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
