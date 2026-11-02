package com.campusone.app.data

data class SavedSession(
    val loginId: String,
    val fullName: String,
    val role: String,
    val token: String
)

data class RecentAccount(
    val loginId: String,
    val fullName: String,
    val role: String
)

interface PlatformSessionDriver {
    fun saveSession(session: SavedSession)
    fun getSession(): SavedSession?
    fun clearSession()
    fun getRecentAccounts(): List<RecentAccount>
    fun addRecentAccount(account: RecentAccount)
}

object SessionStorage {
    private var inMemorySession: SavedSession? = null
    private val inMemoryRecent = mutableListOf(
        RecentAccount("adarsh@rit.com", "Adarsh", "student"),
        RecentAccount("STU2026001", "Shrey Student", "student"),
        RecentAccount("TEA101", "Prof. Alan Turing", "teacher")
    )
    var driver: PlatformSessionDriver? = null

    fun saveSession(session: SavedSession) {
        inMemorySession = session
        driver?.saveSession(session)
        addRecentAccount(RecentAccount(session.loginId, session.fullName, session.role))
    }

    fun getSession(): SavedSession? {
        return driver?.getSession() ?: inMemorySession
    }

    fun clearSession() {
        inMemorySession = null
        driver?.clearSession()
    }

    fun getRecentAccounts(): List<RecentAccount> {
        val fromDriver = driver?.getRecentAccounts()
        return if (!fromDriver.isNullOrEmpty()) fromDriver else inMemoryRecent
    }

    fun addRecentAccount(account: RecentAccount) {
        val existingIndex = inMemoryRecent.indexOfFirst { it.loginId.equals(account.loginId, ignoreCase = true) }
        if (existingIndex >= 0) {
            inMemoryRecent.removeAt(existingIndex)
        }
        inMemoryRecent.add(0, account)
        driver?.addRecentAccount(account)
    }
}
