package hk.eduhk.typeduck.ui.setup

import android.content.Context
import hk.eduhk.typeduck.R
import hk.eduhk.typeduck.util.InputMethodUtils

enum class SetupPage {
    Enable, Select;

    fun getStepText(context: Context) = context.getText(
        when (this) {
            Enable -> R.string.setup_step_one
            Select -> R.string.setup_step_two
        }
    )

    fun getHintText(context: Context) = context.getText(
        when (this) {
            Enable -> R.string.setup_enable_ime_hint
            Select -> R.string.setup_select_ime_hint
        }
    )

    fun getButtonText(context: Context) = context.getText(
        when (this) {
            Enable -> R.string.setup_enable_ime
            Select -> R.string.setup_select_ime
        }
    )

    fun getButtonAction(context: Context) {
        when (this) {
            Enable -> InputMethodUtils.showImeEnablerActivity(context)
            Select -> InputMethodUtils.showImePicker()
        }
    }

    fun isDone() = when (this) {
        Enable -> InputMethodUtils.checkIsTypeDuckEnabled()
        Select -> InputMethodUtils.checkIsTypeDuckSelected()
    }

    companion object {
        fun SetupPage.isLastPage() = this == values().last()
        fun Int.isLastPage() = this == values().size - 1
        fun hasUndonePage() = values().any { !it.isDone() }
        fun firstUndonePage() = values().firstOrNull { !it.isDone() }
    }
}
