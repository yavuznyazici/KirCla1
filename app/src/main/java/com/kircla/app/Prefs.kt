package com.kircla.app

/** Ayar anahtarları ve varsayılan değerler. */
object Prefs {
    const val KEY_COLOR = "color"
    const val KEY_RAMP_SECONDS = "ramp_seconds"
    const val KEY_CURVE = "curve"
    const val KEY_REAL_BRIGHTNESS = "real_brightness"
    const val KEY_HINT_SHOWN = "hint_shown"

    const val DEFAULT_COLOR = "#FF0000"
    const val DEFAULT_RAMP_SECONDS = 15

    const val CURVE_SINE = "sine"
    const val CURVE_LINEAR = "linear"

    /** En parlak seviyede bekleme süresi (ms). */
    const val HOLD_MS = 4_000L

    const val MIN_LEVEL = 0.20f
    const val MAX_LEVEL = 1.00f
}
