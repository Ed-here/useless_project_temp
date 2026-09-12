package com.example.phonedialer.data

import android.content.Context
import android.content.SharedPreferences

class SpeedDialManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("speed_dial_prefs", Context.MODE_PRIVATE)

    fun setSpeedDial(keyDigit: Int, phoneNumber: String) {
        prefs.edit().putString("speed_dial_$keyDigit", phoneNumber).apply()
    }

    fun getSpeedDial(keyDigit: Int): String? {
        val stored = prefs.getString("speed_dial_$keyDigit", null)
        if (stored != null) return stored

        // Default seed speed dial numbers
        return when (keyDigit) {
            1 -> "100" // Voicemail
            2 -> "+1 (555) 019-2831" // Alice Vance
            3 -> "+1 (555) 014-9823" // Bob Smith
            else -> null
        }
    }
}
