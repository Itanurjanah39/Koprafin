package com.masfitatech.koprafin.data.auth

import android.content.Context

object AuthToken {
    fun tokenLogin(context: Context): String? {
        val sessionManager = SessionManager(context)
        return "Bearer ${sessionManager.getToken()}"
    }

    fun roleUser(context: Context): String? {
        val sessionManager = SessionManager(context)
        return sessionManager.getRole()
    }
}