package hk.eduhk.typeduck.data

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.blankj.utilcode.util.PathUtils
import hk.eduhk.typeduck.R
import hk.eduhk.typeduck.ime.enums.InlineModeType
import hk.eduhk.typeduck.ime.landscapeinput.LandscapeInputUIMode
import hk.eduhk.typeduck.ime.text.Language
import hk.eduhk.typeduck.ime.text.Size
import hk.eduhk.typeduck.util.appContext
import java.lang.ref.WeakReference
import java.util.EnumSet

/**
 * Helper class for an organized access to the shared preferences.
 */
class AppPrefs(
    private val shared: SharedPreferences
) {
    private val applicationContext: WeakReference<Context> = WeakReference(appContext)

    val typeDuck = TypeDuck(this)
    val internal = Internal(this)
    val keyboard = Keyboard(this)
    val themeAndColor = ThemeAndColor(this)
    val profile = Profile(this)
    val clipboard = Clipboard(this)
    val other = Other(this)

    /**
     * Fetches the value for [key] from the shared preferences and returns it.
     * The type is automatically derived from the given [default] value.
     * @return The value for [key] or [default].
     */
    private inline fun <reified T> getPref(key: String, default: T): T = default
    private inline fun <reified T> getTypeDuckPref(key: String, default: T): T {
        return when {
            false is T -> {
                shared.getBoolean(key, default as Boolean) as T
            }
            0 is T -> {
                shared.getInt(key, default as Int) as T
            }
            "" is T -> {
                (shared.getString(key, default as String) ?: (default as String)) as T
            }
            setOf<String>() is T -> {
                (shared.getStringSet(key, default as Set<String>) ?: (default as Set<String>)) as T
            }
            else -> null as T
        }
    }

    /**
     * Sets the [value] for [key] in the shared preferences, puts the value into the corresponding
     * cache and returns it.
     */
    private inline fun <reified T> setPref(key: String, value: T) {}
    private inline fun <reified T> setTypeDuckPref(key: String, value: T) {
        when {
            false is T -> {
                shared.edit().putBoolean(key, value as Boolean).apply()
            }
            0 is T -> {
                shared.edit().putInt(key, value as Int).apply()
            }
            "" is T -> {
                shared.edit().putString(key, value as String).apply()
            }
            setOf<String>() is T -> {
                shared.edit().putStringSet(key, value as Set<String>).apply()
            }
        }
    }

    companion object {
        private var defaultInstance: AppPrefs? = null

        fun initDefault(sharedPreferences: SharedPreferences): AppPrefs {
            val instance = AppPrefs(sharedPreferences)
            defaultInstance = instance
            return instance
        }

        @JvmStatic
        fun defaultInstance(): AppPrefs {
            return defaultInstance
                ?: throw UninitializedPropertyAccessException(
                    """
                    Default preferences not initialized! Make sure to call initDefault()
                    before accessing the default preferences.
                    """.trimIndent()
                )
        }
    }

    /**
     * Tells the [PreferenceManager] to set the defined preferences to their default values, if
     * they have not been initialized yet.
     */
    fun initDefaultPreferences() {
        try {
            applicationContext.get()?.let { context ->
                PreferenceManager.setDefaultValues(context, R.xml.preference, true)
            }
        } catch (e: Exception) {
            e.fillInStackTrace()
        }
    }

    class TypeDuck(private val prefs: AppPrefs) {
        companion object {
            const val DISPLAY_LANGUAGES = "pref_display_languages"
            const val MAIN_LANGUAGE = "pref_main_language"
            const val SHOW_ROMANIZATION = "pref_show_romanization"
            const val SHOW_REVERSE_LOOKUP = "pref_show_reverse_lookup"
            const val CANDIDATE_FONT_SIZE = "pref_candidate_font_size"
            const val CANDIDATE_GAP = "pref_candidate_gap"
            const val AUTO_CAP = "pref_auto_cap"
            const val DOUBLE_SPACE_FULL_STOP = "pref_double_space_full_stop"
            const val HAPTIC_FEEDBACK = "pref_haptic_feedback"
            const val AUDIO_FEEDBACK = "pref_audio_feedback"
            const val VISUAL_FEEDBACK = "pref_visual_feedback"
            const val SYMBOLS_ON_QWERTY = "pref_symbols_on_qwerty"
        }
        var displayLanguages: EnumSet<Language>
            get() = EnumSet.copyOf(prefs.getTypeDuckPref(DISPLAY_LANGUAGES, setOf(Language.values().first().name)).map { Language.valueOf(it) })
            set(v) = prefs.setTypeDuckPref(DISPLAY_LANGUAGES, v.map { it.name }.toSet())
        var mainLanguage: Language
            get() = Language.valueOf(prefs.getTypeDuckPref(MAIN_LANGUAGE, Language.values().first().name))
            set(v) = prefs.setTypeDuckPref(MAIN_LANGUAGE, v.name)
        var showRomanization: Boolean
            get() = prefs.getTypeDuckPref(SHOW_ROMANIZATION, true)
            set(v) = prefs.setTypeDuckPref(SHOW_ROMANIZATION, v)
        var showReverseLookup: Boolean
            get() = prefs.getTypeDuckPref(SHOW_REVERSE_LOOKUP, false)
            set(v) = prefs.setTypeDuckPref(SHOW_REVERSE_LOOKUP, v)
        var candidateFontSize: Size
            get() = Size.valueOf(prefs.getTypeDuckPref(CANDIDATE_FONT_SIZE, Size.NORMAL.name))
            set(v) = prefs.setTypeDuckPref(CANDIDATE_FONT_SIZE, v.name)
        var candidateGap: Size
            get() = Size.valueOf(prefs.getTypeDuckPref(CANDIDATE_GAP, Size.NORMAL.name))
            set(v) = prefs.setTypeDuckPref(CANDIDATE_GAP, v.name)
        var autoCap: Boolean
            get() = prefs.getTypeDuckPref(AUTO_CAP, true)
            set(v) = prefs.setTypeDuckPref(AUTO_CAP, v)
        var doubleSpaceFullStop: Boolean
            get() = prefs.getTypeDuckPref(DOUBLE_SPACE_FULL_STOP, true)
            set(v) = prefs.setTypeDuckPref(DOUBLE_SPACE_FULL_STOP, v)
        var hapticFeedback: Boolean
            get() = prefs.getTypeDuckPref(HAPTIC_FEEDBACK, false)
            set(v) = prefs.setTypeDuckPref(HAPTIC_FEEDBACK, v)
        var audioFeedback: Boolean
            get() = prefs.getTypeDuckPref(AUDIO_FEEDBACK, true)
            set(v) = prefs.setTypeDuckPref(AUDIO_FEEDBACK, v)
        var visualFeedback: Boolean
            get() = prefs.getTypeDuckPref(VISUAL_FEEDBACK, true)
            set(v) = prefs.setTypeDuckPref(VISUAL_FEEDBACK, v)
        var symbolsOnQwerty: Boolean
            get() = prefs.getTypeDuckPref(SYMBOLS_ON_QWERTY, true)
            set(v) = prefs.setTypeDuckPref(SYMBOLS_ON_QWERTY, v)
    }

    class Internal(private val prefs: AppPrefs) {
        companion object {
            const val LAST_VERSION_NAME = "general__last_version_name"
            const val PID = "general__pid"
            const val LAST_BUILD_GIT_HASH = "general__last_build_git_hash"
        }
        var lastVersionName: String
            get() = prefs.getPref(LAST_VERSION_NAME, "")
            set(v) = prefs.setPref(LAST_VERSION_NAME, v)
        var pid: Int
            get() = prefs.getPref(PID, 0)
            set(v) = prefs.setPref(PID, v)
        var lastBuildGitHash: String
            get() = prefs.getPref(LAST_BUILD_GIT_HASH, "")
            set(v) = prefs.setPref(LAST_BUILD_GIT_HASH, v)
    }

    /**
     *  Wrapper class of keyboard settings.
     */
    class Keyboard(private val prefs: AppPrefs) {
        companion object {
            const val INLINE_PREEDIT_MODE = "keyboard__inline_preedit"
            const val SOFT_CURSOR_ENABLED = "keyboard__soft_cursor"
            const val FLOATING_WINDOW_ENABLED = "keyboard__show_window"
            const val POPUP_KEY_PRESS_ENABLED = "keyboard__show_key_popup"
            const val SWITCHES_ENABLED = "keyboard__show_switches"
            const val SWITCH_ARROW_ENABLED = "keyboard__show_switch_arrow"
            const val FULLSCREEN_MODE = "keyboard__fullscreen_mode"
            const val CANDIDATE_PAGE_SIZE = "keyboard__candidate_page_size"

            const val HOOK_FAST_INPUT = "keyboard__hook_fast_input"
            const val HOOK_CANDIDATE = "keyboard__hook_candidate"
            const val HOOK_CANDIDATE_COMMIT = "keyboard__hook_candidate_commit"
            const val HOOK_CTRL_A = "keyboard__hook_ctrl_a"
            const val HOOK_CTRL_CV = "keyboard__hook_ctrl_cv"
            const val HOOK_CTRL_LR = "keyboard__hook_ctrl_lr"
            const val HOOK_CTRL_ZY = "keyboard__hook_ctrl_zy"
            const val HOOK_SHIFT_SPACE = "keyboard__hook_shift_space"
            const val HOOK_SHIFT_NUM = "keyboard__hook_shift_num"
            const val HOOK_SHIFT_SYMBOL = "keyboard__hook_shift_symbol"

            const val SOUND_ENABLED = "keyboard__key_sound"
            const val SOUND_VOLUME = "keyboard__key_sound_volume"
            const val CUSTOM_SOUND_ENABLED = "keyboard__custom_key_sound"
            const val CUSTOM_SOUND_PACKAGE = "keyboard__key_sound_package"

            const val VIBRATION_ENABLED = "keyboard__key_vibration"
            const val VIBRATION_DURATION = "keyboard__key_vibration_duration"
            const val VIBRATION_AMPLITUDE = "keyboard__key_vibration_amplitude"

            const val SPEAK_KEY_PRESS_ENABLED = "keyboard__speak_key_press"
            const val SPEAK_COMMIT_ENABLED = "keyboard__speak_commit"

            const val SWIPE_ENABLED = "keyboard__swipe_enabled"
            const val SWIPE_TRAVEL = "keyboard__key_swipe_travel"
            const val SWIPE_TRAVEL_HI = "keyboard__key_swipe_travel_hi"
            const val SWIPE_VELOCITY = "keyboard__key_swipe_velocity"
            const val SWIPE_VELOCITY_HI = "keyboard__key_swipe_velocity_hi"
            const val SWIPE_TIME_HI = "keyboard__key_swipe_time_hi"
            const val LONG_PRESS_TIMEOUT = "keyboard__key_long_press_timeout"
            const val REPEAT_INTERVAL = "keyboard__key_repeat_interval"
            const val DELETE_CANDIDATE_TIMEOUT = "keyboard__key_delete_candidate_timeout"
            const val SHOULD_LONG_CLICK_DELETE_CANDIDATE = "keyboard__long_click_delete_candidate"
        }
        var inlinePreedit: InlineModeType
            get() = InlineModeType.fromString(prefs.getPref(INLINE_PREEDIT_MODE, "composition"))
            set(v) = prefs.setPref(INLINE_PREEDIT_MODE, v)
        var fullscreenMode: LandscapeInputUIMode
            get() = LandscapeInputUIMode.fromString(prefs.getPref(FULLSCREEN_MODE, "auto_show"))
            set(v) = prefs.setPref(FULLSCREEN_MODE, v)
        var softCursorEnabled: Boolean = false
            get() = prefs.getPref(SOFT_CURSOR_ENABLED, true)
            private set
        var popupWindowEnabled: Boolean = false
            get() = prefs.getPref(FLOATING_WINDOW_ENABLED, true)
            private set
        var popupKeyPressEnabled: Boolean = false
            get() = prefs.getPref(POPUP_KEY_PRESS_ENABLED, true)
            private set
        var switchesEnabled: Boolean = false
            get() = prefs.getPref(SWITCHES_ENABLED, true)
            private set
        var switchArrowEnabled: Boolean = false
            get() = prefs.getPref(SWITCH_ARROW_ENABLED, true)
            private set
        var candidatePageSize: String = "30"
            get() = prefs.getPref(CANDIDATE_PAGE_SIZE, "30")
            private set

        var hookFastInput: Boolean = false
            get() = prefs.getPref(HOOK_FAST_INPUT, false)
            private set
        var hookCandidate: Boolean = false
            get() = prefs.getPref(HOOK_CANDIDATE, true)
            private set
        var hookCandidateCommit: Boolean = false
            get() = prefs.getPref(HOOK_CANDIDATE_COMMIT, false)
            private set
        var hookCtrlA: Boolean = false
            get() = prefs.getPref(HOOK_CTRL_A, false)
            private set
        var hookCtrlCV: Boolean = false
            get() = prefs.getPref(HOOK_CTRL_CV, false)
            private set
        var hookCtrlLR: Boolean = false
            get() = prefs.getPref(HOOK_CTRL_LR, false)
            private set
        var hookCtrlZY: Boolean = false
            get() = prefs.getPref(HOOK_CTRL_ZY, false)
            private set
        var hookShiftSpace: Boolean = false
            get() = prefs.getPref(HOOK_SHIFT_SPACE, false)
            private set
        var hookShiftNum: Boolean = false
            get() = prefs.getPref(HOOK_SHIFT_NUM, false)
            private set
        var hookShiftSymbol: Boolean = false
            get() = prefs.getPref(HOOK_SHIFT_SYMBOL, false)
            private set

        var soundEnabled: Boolean = false
            get() = prefs.getPref(SOUND_ENABLED, true)
            private set
        var customSoundEnabled: Boolean
            get() = prefs.getPref(CUSTOM_SOUND_ENABLED, false)
            set(v) = prefs.setPref(CUSTOM_SOUND_ENABLED, v)
        var customSoundPackage: String
            get() = prefs.getPref(CUSTOM_SOUND_PACKAGE, "")
            set(v) = prefs.setPref(CUSTOM_SOUND_PACKAGE, v)
        var soundVolume: Int = 0
            get() = prefs.getPref(SOUND_VOLUME, 100)
            private set
        var vibrationEnabled: Boolean = false
            get() = prefs.getPref(VIBRATION_ENABLED, false)
            private set
        var vibrationDuration: Int = 0
            get() = prefs.getPref(VIBRATION_DURATION, 10)
            private set
        var vibrationAmplitude: Int = 0
            get() = prefs.getPref(VIBRATION_AMPLITUDE, -1)
            private set
        var swipeEnabled: Boolean = false
            get() = prefs.getPref(SWIPE_ENABLED, false)
            private set
        var swipeTravel: Int = 0
            get() = prefs.getPref(SWIPE_TRAVEL, 80)
            private set
        var swipeTravelHi: Int = 0
            get() = prefs.getPref(SWIPE_TRAVEL_HI, 200)
            private set
        var swipeVelocity: Int = 0
            get() = prefs.getPref(SWIPE_VELOCITY, 800)
            private set
        var swipeVelocityHi: Int = 0
            get() = prefs.getPref(SWIPE_VELOCITY_HI, 25000)
            private set
        var swipeTimeHi: Int = 0
            get() = prefs.getPref(SWIPE_TIME_HI, 80)
            private set
        var longPressTimeout: Int = 0
            get() = prefs.getPref(LONG_PRESS_TIMEOUT, 400)
            private set
        var repeatInterval: Int = 0
            get() = prefs.getPref(REPEAT_INTERVAL, 50)
            private set
        var deleteCandidateTimeout: Int = 0
            get() = prefs.getPref(DELETE_CANDIDATE_TIMEOUT, 2000)
            private set
        var shouldLongClickDeleteCandidate: Boolean = false
            get() = prefs.getPref(SHOULD_LONG_CLICK_DELETE_CANDIDATE, true)
            private set
        var isSpeakKey: Boolean
            get() = prefs.getPref(SPEAK_KEY_PRESS_ENABLED, false)
            set(v) = prefs.setPref(SPEAK_KEY_PRESS_ENABLED, v)
        var isSpeakCommit: Boolean
            get() = prefs.getPref(SPEAK_COMMIT_ENABLED, false)
            set(v) = prefs.setPref(SPEAK_COMMIT_ENABLED, v)
    }

    /**
     *  Wrapper class of theme and color settings.
     */
    class ThemeAndColor(private val prefs: AppPrefs) {
        companion object {
            const val SELECTED_THEME = "theme_selected_theme"
            const val SELECTED_COLOR = "theme_selected_color"
            const val AUTO_DARK = "theme_auto_dark"
            const val USE_MINI_KEYBOARD = "theme_use_mini_keyboard"
        }
        var selectedTheme: String
            get() = prefs.getPref(SELECTED_THEME, "trime")
            set(v) = prefs.setPref(SELECTED_THEME, v)
        var selectedColor: String
            get() = prefs.getPref(SELECTED_COLOR, "default")
            set(v) = prefs.setPref(SELECTED_COLOR, v)
        var autoDark: Boolean = false
            get() = prefs.getPref(AUTO_DARK, false)
            private set
        var useMiniKeyboard: Boolean = false
            get() = prefs.getPref(USE_MINI_KEYBOARD, false)
            private set
    }

    /**
     *  Wrapper class of profile settings.
     */
    class Profile(private val prefs: AppPrefs) {
        companion object {
            const val SHARED_DATA_DIR = "profile_shared_data_dir"
            const val USER_DATA_DIR = "profile_user_data_dir"
            const val SYNC_BACKGROUND_ENABLED = "profile_sync_in_background"
            const val LAST_SYNC_STATUS = "profile_last_sync_status"
            const val LAST_BACKGROUND_SYNC = "profile_last_background_sync"
            val INTERNAL_PATH_PREFIX: String = PathUtils.getInternalAppDataPath()
        }
        var sharedDataDir: String
            get() = prefs.getPref(SHARED_DATA_DIR, "$INTERNAL_PATH_PREFIX/TypeDuck")
            set(v) = prefs.setPref(SHARED_DATA_DIR, v)
        var userDataDir: String
            get() = prefs.getPref(USER_DATA_DIR, "$INTERNAL_PATH_PREFIX/TypeDuck")
            set(v) = prefs.setPref(USER_DATA_DIR, v)
        var syncBackgroundEnabled: Boolean
            get() = prefs.getPref(SYNC_BACKGROUND_ENABLED, false)
            set(v) = prefs.setPref(SYNC_BACKGROUND_ENABLED, v)
        var lastSyncStatus: Boolean
            get() = prefs.getPref(LAST_SYNC_STATUS, false)
            set(v) = prefs.setPref(LAST_SYNC_STATUS, v)
        var lastBackgroundSync: String
            get() = prefs.getPref(LAST_BACKGROUND_SYNC, "")
            set(v) = prefs.setPref(LAST_BACKGROUND_SYNC, v)
    }

    class Clipboard(private val prefs: AppPrefs) {
        companion object {
            const val CLIPBOARD_COMPARE_RULES = "clipboard_clipboard_compare"
            const val CLIPBOARD_OUTPUT_RULES = "clipboard_clipboard_output"
            const val DRAFT_OUTPUT_RULES = "clipboard_draft_output"
            const val DRAFT_EXCLUDE_APP = "clipboard_draft_exclude_app"
            const val DRAFT_LIMIT = "clipboard_draft_limit"
            const val CLIPBOARD_LIMIT = "clipboard_clipboard_limit"
        }
        var clipboardCompareRules: List<String>
            get() = prefs.getPref(CLIPBOARD_COMPARE_RULES, "").trim().split('\n')
            set(v) = prefs.setPref(CLIPBOARD_COMPARE_RULES, v.joinToString("\n"))
        var clipboardOutputRules: List<String>
            get() = prefs.getPref(CLIPBOARD_OUTPUT_RULES, "").trim().split('\n')
            set(v) = prefs.setPref(CLIPBOARD_OUTPUT_RULES, v.joinToString("\n"))
        var draftOutputRules: List<String>
            get() = prefs.getPref(DRAFT_OUTPUT_RULES, "").trim().split('\n')
            set(v) = prefs.setPref(DRAFT_OUTPUT_RULES, v.joinToString("\n"))
        var clipboardLimit: Int
            get() = prefs.getPref(CLIPBOARD_LIMIT, 10)
            set(v) = prefs.setPref(CLIPBOARD_LIMIT, v)
        var draftLimit: Int
            get() = prefs.getPref(DRAFT_LIMIT, 10)
            set(v) = prefs.setPref(DRAFT_LIMIT, v)
        var draftExcludeApp: String
            get() = prefs.getPref(DRAFT_EXCLUDE_APP, "")
            set(v) = prefs.setPref(DRAFT_EXCLUDE_APP, v)
    }

    /**
     *  Wrapper class of configuration settings.
     */
    class Other(private val prefs: AppPrefs) {
        companion object {
            const val UI_MODE = "other__ui_mode"
            const val SHOW_APP_ICON = "other__show_app_icon"
            const val SHOW_STATUS_BAR_ICON = "other__show_status_bar_icon"
            const val DESTROY_ON_QUIT = "other__destroy_on_quit"
        }
        var uiMode: String
            get() = prefs.getPref(UI_MODE, "auto")
            set(v) = prefs.setPref(UI_MODE, v)
        var showAppIcon: Boolean
            get() = prefs.getPref(SHOW_APP_ICON, true)
            set(v) = prefs.setPref(SHOW_APP_ICON, v)
        var showStatusBarIcon: Boolean = false
            get() = prefs.getPref(SHOW_STATUS_BAR_ICON, false)
            private set
        var destroyOnQuit: Boolean = false
            get() = prefs.getPref(DESTROY_ON_QUIT, false)
            private set
    }
}
