package com.esan.payflowapp.core.pref

import android.content.Context
import androidx.core.content.edit

object SharedPreferencesManager {

    private const val PREFS_NAME = "user_prefs"

    fun saveName(context: Context, value: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit { putString("name", value) }
    }

    fun saveEmail(context: Context, value: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit { putString("email", value) }
    }

    fun saveIsAdmin(context: Context, value: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit { putBoolean("is_admin", value) }
    }

    fun saveCreatedAt(context: Context, value: Long) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit { putLong("created_at", value) }
    }

    fun getName(context: Context): String =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString("name", "").orEmpty()

    fun getEmail(context: Context): String =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString("email", "").orEmpty()

    fun isAdmin(context: Context): Boolean =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean("is_admin", false)

    fun getCreatedAt(context: Context): Long =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getLong("created_at", 0L)

    fun clearData(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit { clear() }
    }

}