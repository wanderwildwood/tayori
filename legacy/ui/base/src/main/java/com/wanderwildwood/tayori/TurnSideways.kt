package com.wanderwildwood.tayori

import android.content.Context
import android.content.SharedPreferences

/**
 * Email's own switch for turning the screen with the phone (Settings > Display), on unless
 * turned off. Every screen built on BaseActivity reads it when it comes to the front, and the
 * one in front hears it change, so the Settings screen itself turns or stops at once.
 */
object TurnSideways {
    private const val FILE = "turn_with_phone"
    private const val ON = "on"

    fun interface Listener {
        fun changed(on: Boolean)
    }

    private val heard = HashMap<Listener, SharedPreferences.OnSharedPreferenceChangeListener>()

    fun on(context: Context): Boolean = prefs(context).getBoolean(ON, true)

    fun set(context: Context, on: Boolean) {
        prefs(context).edit().putBoolean(ON, on).apply()
    }

    fun listen(context: Context, listener: Listener) {
        val prefs = prefs(context)
        val heard = SharedPreferences.OnSharedPreferenceChangeListener { p, key ->
            if (key == ON) listener.changed(p.getBoolean(ON, true))
        }
        this.heard.put(listener, heard)?.let(prefs::unregisterOnSharedPreferenceChangeListener)
        prefs.registerOnSharedPreferenceChangeListener(heard)
    }

    fun unlisten(context: Context, listener: Listener) {
        heard.remove(listener)?.let(prefs(context)::unregisterOnSharedPreferenceChangeListener)
    }

    private fun prefs(context: Context) = context.applicationContext.getSharedPreferences(FILE, Context.MODE_PRIVATE)
}
