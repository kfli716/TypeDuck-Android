package hk.eduhk.typeduck.util

import hk.eduhk.typeduck.core.Rime

object ConfigGetter {

    @JvmStatic
    fun loadMap(name: String, key: String = ""): Map<String, *>? = Rime.getRimeConfigMap(name, key)

    @JvmStatic
    fun Map<String, *>.getInt(key: String, default: Int): Int {
        val o = this.getOrElse(key) { default }
        return o.toString().toInt()
    }

    @JvmStatic
    fun Map<String, *>.getFloat(key: String, default: Float): Float {
        val o = this.getOrElse(key) { default }
        return o.toString().toFloat()
    }

    @JvmStatic
    fun Map<String, *>.getDouble(key: String, default: Double): Double {
        val o = this.getOrElse(key) { default }
        return o.toString().toDouble()
    }

    @JvmStatic
    fun Map<String, *>.getString(key: String, default: String): String {
        val o = this.getOrElse(key) { default }
        return o.toString()
    }

    @JvmStatic
    fun Map<String, *>.getBoolean(key: String, default: Boolean): Boolean {
        val o = this.getOrElse(key) { default }
        return o.toString().toBooleanStrict()
    }

    @JvmStatic
    fun Map<String, *>.getPixel(key: String, default: Float): Int {
        val f = this.getFloat(key, default)
        return dp2px(f).toInt()
    }
}
