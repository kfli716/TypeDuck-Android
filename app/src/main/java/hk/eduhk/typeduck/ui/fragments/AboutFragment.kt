package hk.eduhk.typeduck.ui.fragments

import android.os.Bundle
import hk.eduhk.typeduck.R
import hk.eduhk.typeduck.ui.components.PaddingPreferenceFragment

class AboutFragment : PaddingPreferenceFragment() {
	override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
		setPreferencesFromResource(R.xml.about, rootKey)
	}
}
