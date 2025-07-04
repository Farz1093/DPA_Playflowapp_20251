package com.esan.payflowapp.core.pref

import android.content.Context
import androidx.core.content.edit

object SharedPreferencesManager {

    fun saveName(context: Context, value: String) {
        val sharedPref = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        sharedPref.edit {
            putString("name", value)
            apply()
        }
    }

    fun saveIsAdmin(context: Context, value: Boolean) {
        val sharedPref = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        sharedPref.edit {
            putBoolean("is_admin", value)
            apply()
        }
    }

    fun getName(context: Context): String {
        val sharedPref = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        return sharedPref.getString("name", null).orEmpty()
    }

    fun isAdmin(context: Context): Boolean {
        val sharedPref = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        return sharedPref.getBoolean("is_admin", false)
    }
    fun clearData(context: Context) {
        val sharedPref = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        sharedPref.edit {
            clear()
            apply()
        }
    }

}