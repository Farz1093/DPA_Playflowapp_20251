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

    fun saveAccountNumber(context: Context, value: String) {
        val sharedPref = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        sharedPref.edit {
            putString("account_number", value)
            apply()
        }
    }

    fun saveBalance(context: Context, value: Double) {
        val sharedPref = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        sharedPref.edit {
            putLong("balance", value.toLong())
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

    fun getAccountNumber(context: Context): String {
        val sharedPref = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        return sharedPref.getString("account_number", null).orEmpty()
    }

    fun getBalance(context: Context): Double {
        val sharedPref = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        return sharedPref.getLong("balance", 0).toDouble()
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