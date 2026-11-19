package com.kircla.app

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.preference.PreferenceManager
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt

/**
 * Tam ekran renkli ışık. Parlaklık döngüsü:
 *   %20 -> %100 (rampMs)  ->  %100'de bekle (HOLD_MS)  ->  %100 -> %20 (rampMs)  -> başa dön
 */
class MainActivity : AppCompatActivity() {

    private lateinit var screen: View
    private lateinit var gestures: GestureDetector

    private var baseColor = Color.RED
    private var rampMs = Prefs.DEFAULT_RAMP_SECONDS * 1000L
    private var sineCurve = true
    private var realBrightness = true

    private var startTime = 0L
    private var running = false
    private var lastWindowBrightness = -2f

    private val frame = object : Runnable {
        override fun run() {
            if (!running) return
            val t = (SystemClock.uptimeMillis() - startTime) % cycleMs()
            render(levelAt(t))
            screen.postOnAnimation(this)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        if (Build.VERSION.SDK_INT >= 28) {
            window.attributes = window.attributes.apply {
                layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
        }

        screen = View(this)
        screen.setBackgroundColor(Color.BLACK)
        setContentView(screen)

        gestures = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onDown(e: MotionEvent) = true

            override fun onDoubleTap(e: MotionEvent): Boolean {
                startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
                return true
            }
        })
        screen.setOnTouchListener { _, event ->
            gestures.onTouchEvent(event)
            true
        }
    }

    override fun onResume() {
        super.onResume()
        hideSystemBars()
        loadPrefs()
        showHintOnce()

        lastWindowBrightness = -2f
        if (!realBrightness) setWindowBrightness(WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE)

        startTime = SystemClock.uptimeMillis()
        running = true
        screen.postOnAnimation(frame)
    }

    override fun onPause() {
        running = false
        screen.removeCallbacks(frame)
        // Telefonun kendi parlaklık ayarına geri dön.
        setWindowBrightness(WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE)
        super.onPause()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemBars()
    }

    private fun hideSystemBars() {
        WindowInsetsControllerCompat(window, window.decorView).apply {
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            hide(WindowInsetsCompat.Type.systemBars())
        }
    }

    private fun loadPrefs() {
        val sp = PreferenceManager.getDefaultSharedPreferences(this)
        baseColor = try {
            Color.parseColor(sp.getString(Prefs.KEY_COLOR, Prefs.DEFAULT_COLOR))
        } catch (e: IllegalArgumentException) {
            Color.RED
        }
        rampMs = sp.getInt(Prefs.KEY_RAMP_SECONDS, Prefs.DEFAULT_RAMP_SECONDS) * 1000L
        sineCurve = sp.getString(Prefs.KEY_CURVE, Prefs.CURVE_SINE) == Prefs.CURVE_SINE
        realBrightness = sp.getBoolean(Prefs.KEY_REAL_BRIGHTNESS, true)
    }

    private fun showHintOnce() {
        val sp = PreferenceManager.getDefaultSharedPreferences(this)
        if (!sp.getBoolean(Prefs.KEY_HINT_SHOWN, false)) {
            Toast.makeText(this, R.string.hint_double_tap, Toast.LENGTH_LONG).show()
            sp.edit().putBoolean(Prefs.KEY_HINT_SHOWN, true).apply()
        }
    }

    private fun cycleMs() = rampMs * 2 + Prefs.HOLD_MS

    /** Döngü içindeki t (ms) anı için parlaklık seviyesi (0..1). */
    private fun levelAt(t: Long): Float {
        val range = Prefs.MAX_LEVEL - Prefs.MIN_LEVEL
        return when {
            t < rampMs ->
                Prefs.MIN_LEVEL + range * ease(t.toFloat() / rampMs)
            t < rampMs + Prefs.HOLD_MS ->
                Prefs.MAX_LEVEL
            else ->
                Prefs.MAX_LEVEL - range * ease((t - rampMs - Prefs.HOLD_MS).toFloat() / rampMs)
        }
    }

    private fun ease(x: Float): Float =
        if (sineCurve) 0.5f - 0.5f * cos(PI * x).toFloat() else x

    private fun render(level: Float) {
        if (realBrightness) {
            // Renk tam kalır, telefonun gerçek parlaklığı değişir.
            screen.setBackgroundColor(baseColor)
            setWindowBrightness((level * 100).roundToInt() / 100f)
        } else {
            // Yalnızca renk koyulaşıp açılır.
            screen.setBackgroundColor(
                Color.rgb(
                    (Color.red(baseColor) * level).roundToInt(),
                    (Color.green(baseColor) * level).roundToInt(),
                    (Color.blue(baseColor) * level).roundToInt()
                )
            )
        }
    }

    private fun setWindowBrightness(value: Float) {
        if (value == lastWindowBrightness) return
        lastWindowBrightness = value
        window.attributes = window.attributes.apply { screenBrightness = value }
    }
}
