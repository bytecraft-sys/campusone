package com.campusone.app

import android.content.Context
import android.content.SharedPreferences
import com.campusone.app.data.PlatformSessionDriver
import com.campusone.app.data.RecentAccount
import com.campusone.app.data.SavedSession

class AndroidSessionDriver(context: Context) : PlatformSessionDriver {
    private val prefs: SharedPreferences = context.getSharedPreferences("campusone_session_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_LOGIN_ID = "login_id"
        private const val KEY_FULL_NAME = "full_name"
        private const val KEY_ROLE = "role"
        private const val KEY_TOKEN = "token"
        private const val KEY_RECENT = "recent_accounts"
    }

    override fun saveSession(session: SavedSession) {
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .putString(KEY_LOGIN_ID, session.loginId)
            .putString(KEY_FULL_NAME, session.fullName)
            .putString(KEY_ROLE, session.role)
            .putString(KEY_TOKEN, session.token)
            .apply()

        addRecentAccount(RecentAccount(session.loginId, session.fullName, session.role))
    }

    override fun getSession(): SavedSession? {
        val isLoggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
        if (!isLoggedIn) return null

        val loginId = prefs.getString(KEY_LOGIN_ID, null) ?: return null
        val fullName = prefs.getString(KEY_FULL_NAME, "") ?: ""
        val role = prefs.getString(KEY_ROLE, "student") ?: "student"
        val token = prefs.getString(KEY_TOKEN, "") ?: ""

        return SavedSession(loginId, fullName, role, token)
    }

    override fun clearSession() {
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, false)
            .remove(KEY_TOKEN)
            .apply()
    }

    override fun getRecentAccounts(): List<RecentAccount> {
        val raw = prefs.getString(KEY_RECENT, "") ?: ""
        if (raw.isBlank()) return emptyList()

        return raw.split(";").mapNotNull { entry ->
            val parts = entry.split("|")
            if (parts.size >= 3) {
                RecentAccount(parts[0], parts[1], parts[2])
            } else null
        }
    }

    override fun addRecentAccount(account: RecentAccount) {
        val current = getRecentAccounts().toMutableList()
        current.removeAll { it.loginId.equals(account.loginId, ignoreCase = true) }
        current.add(0, account)

        val serialized = current.take(5).joinToString(";") { "${it.loginId}|${it.fullName}|${it.role}" }
        prefs.edit().putString(KEY_RECENT, serialized).apply()
    }
}
