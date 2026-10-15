package com.masfitatech.koprafin.data.auth

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class SessionManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "koprafin-prefs"
        private const val KEY_TOKEN = "token"
        private const val KEY_NAME = "name"
        private const val KEY_ROLE = "role"
        private const val KEY_PHONE = "phone"
    }

    fun saveToken(token: String?) = prefs.edit {
        putString(KEY_TOKEN, token)
    }

    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)

    fun saveName(name: String?) = prefs.edit {
        putString(KEY_NAME, name)
    }

    fun getName(): String? = prefs.getString(KEY_NAME, null)

    fun saveRole(role: String?) = prefs.edit {
        putString(KEY_ROLE, role)
    }

    fun getRole(): String? = prefs.getString(KEY_ROLE, null)

    fun savePhone(phone: String?) = prefs.edit {
        putString(KEY_PHONE, phone)
    }

    fun getPhone(): String? = prefs.getString(KEY_PHONE, null)

    fun clearSession() = prefs.edit {
        clear()
    }
}
