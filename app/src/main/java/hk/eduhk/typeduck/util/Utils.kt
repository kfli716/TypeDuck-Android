package hk.eduhk.typeduck.util

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.os.Build
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.ToastUtils
import hk.eduhk.typeduck.R
import hk.eduhk.typeduck.TrimeApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

val appContext: Context get() = TrimeApplication.getInstance().applicationContext

@OptIn(ExperimentalContracts::class)
inline fun <T : Any, U> Result<T?>.bindOnNotNull(block: (T) -> Result<U>): Result<U>? {
    contract {
        callsInPlace(block, InvocationKind.AT_MOST_ONCE)
    }
    return when {
        isSuccess && getOrThrow() != null -> block(getOrThrow()!!)
        isSuccess && getOrThrow() == null -> null
        else -> Result.failure(exceptionOrNull()!!)
    }
}

suspend fun <T> Result<T>.toast() = withContext(Dispatchers.Main.immediate) {
    onSuccess {
        ToastUtils.showShort(R.string.setup_done)
    }
    onFailure {
        ToastUtils.showShort(it.message)
    }
}

fun formatDateTime(timeMillis: Long? = null): String =
    SimpleDateFormat.getDateTimeInstance().format(timeMillis?.let { Date(it) } ?: Date())

private val iso8601DateFormat by lazy {
    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
}

fun iso8601UTCDateTime(timeMillis: Long? = null): String =
    iso8601DateFormat.format(timeMillis?.let { Date(it) } ?: Date())

@Suppress("NOTHING_TO_INLINE")
inline fun CharSequence.startsWithAsciiChar(): Boolean {
    val firstCodePoint = this.toString().codePointAt(0)
    return firstCodePoint in 0x20 until 0x80
}

fun Activity.applyTranslucentSystemBars() {
    WindowCompat.setDecorFitsSystemWindows(window, false)
    // windowLightNavigationBar is available for 27+
    window.navigationBarColor =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            Color.TRANSPARENT
        } else {
            // com.android.internal.R.color.system_bar_background_semi_transparent
            0x66000000
        }
}

fun RecyclerView.applyNavBarInsetsBottomPadding() {
    clipToPadding = false
    ViewCompat.setOnApplyWindowInsetsListener(this) { _, windowInsets ->
        windowInsets.getInsets(WindowInsetsCompat.Type.navigationBars()).also {
            setPadding(paddingLeft, paddingTop, paddingRight, it.bottom)
        }
        windowInsets
    }
}
