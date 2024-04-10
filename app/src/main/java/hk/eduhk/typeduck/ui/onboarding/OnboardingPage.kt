package hk.eduhk.typeduck.ui.onboarding

import android.app.Activity
import android.content.Context
import hk.eduhk.typeduck.R
import hk.eduhk.typeduck.util.InputMethodUtils

interface OnboardingPage {
    val page: Int
    fun getHeadingText(context: Context) = context.resources.getStringArray(R.array.onboarding_headings)[page]
    fun getContentText(context: Context) = context.resources.getStringArray(R.array.onboarding_contents)[page]

    companion object {
        const val numberOfPages = 2

        fun get(page: Int) =
            if (page < numberOfPages - 1) OnboardingPageBase(page)
            else OnboardingPageWithButton(when (false) {
                InputMethodUtils.checkIsTypeDuckEnabled() -> numberOfPages - 1
                InputMethodUtils.checkIsTypeDuckSelected() -> numberOfPages
                else -> numberOfPages + 1
            })
    }
}

data class OnboardingPageBase(override val page: Int): OnboardingPage

data class OnboardingPageWithButton(override val page: Int): OnboardingPage {
    fun getButtonText(context: Context) = context.getText(
        when (page) {
            OnboardingPage.numberOfPages - 1 -> R.string.onboarding_open_keyboard_settings
            OnboardingPage.numberOfPages -> R.string.onboarding_open_keyboard_picker
            OnboardingPage.numberOfPages + 1 -> R.string.onboarding_done
            else -> throw IndexOutOfBoundsException("Invalid page number: $page")
        }
    )

    fun getButtonAction(activity: Activity) {
        when (page) {
            OnboardingPage.numberOfPages - 1 -> InputMethodUtils.showImeEnablerActivity(activity)
            OnboardingPage.numberOfPages -> InputMethodUtils.showImePicker()
            OnboardingPage.numberOfPages + 1 -> activity.finish()
            else -> throw IndexOutOfBoundsException("Invalid page number: $page")
        }
    }
}
