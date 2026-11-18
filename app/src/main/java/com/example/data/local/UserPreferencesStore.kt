package com.example.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.userPrefsDataStore by preferencesDataStore(name = "kabir_tech_lab_prefs")

data class UserPreferencesState(
    val languageCode: String = "bn", // Default: Bangla
    val themeMode: String = "dark",   // "dark", "light", "system"
    val fontSize: String = "medium",  // "small", "medium", "large"
    val dataSaver: Boolean = false,
    val onboardingDone: Boolean = false,
    val guestPreviewChosen: Boolean = false,
    val favoriteCategories: Set<String> = setOf("Tech Tutorials", "Stories"),
    val notifyNewUploads: Boolean = true,
    val notifyDailyTip: Boolean = true,
    val notifyQuizReminder: Boolean = true,
    val notifyCommunityReplies: Boolean = true,
    val bellSubscribed: Boolean = false,
    val localXp: Int = 50,
    val localStreak: Int = 1,
    val answeredQuizIds: Set<String> = emptySet(),
    val lastCheckInEpochDay: Long = 0L
)

class UserPreferencesStore(private val context: Context) {
    private object Keys {
        val LANGUAGE = stringPreferencesKey("language_code")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val FONT_SIZE = stringPreferencesKey("font_size")
        val DATA_SAVER = booleanPreferencesKey("data_saver")
        val ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
        val GUEST_PREVIEW = booleanPreferencesKey("guest_preview_chosen")
        val FAVORITE_CATEGORIES = stringSetPreferencesKey("favorite_categories")
        val NOTIFY_UPLOADS = booleanPreferencesKey("notify_uploads")
        val NOTIFY_TIP = booleanPreferencesKey("notify_tip")
        val NOTIFY_QUIZ = booleanPreferencesKey("notify_quiz")
        val NOTIFY_COMMUNITY = booleanPreferencesKey("notify_community")
        val BELL_SUBSCRIBED = booleanPreferencesKey("bell_subscribed")
        val LOCAL_XP = intPreferencesKey("local_xp")
        val LOCAL_STREAK = intPreferencesKey("local_streak")
        val ANSWERED_QUIZ_IDS = stringSetPreferencesKey("answered_quiz_ids")
        val LAST_CHECKIN_DAY = stringPreferencesKey("last_checkin_day")
    }

    val preferences: Flow<UserPreferencesState> = context.userPrefsDataStore.data.map { prefs ->
        UserPreferencesState(
            languageCode = prefs[Keys.LANGUAGE] ?: "bn",
            themeMode = prefs[Keys.THEME_MODE] ?: "dark",
            fontSize = prefs[Keys.FONT_SIZE] ?: "medium",
            dataSaver = prefs[Keys.DATA_SAVER] ?: false,
            onboardingDone = prefs[Keys.ONBOARDING_DONE] ?: false,
            guestPreviewChosen = prefs[Keys.GUEST_PREVIEW] ?: false,
            favoriteCategories = prefs[Keys.FAVORITE_CATEGORIES] ?: setOf("Tech Tutorials", "Stories"),
            notifyNewUploads = prefs[Keys.NOTIFY_UPLOADS] ?: true,
            notifyDailyTip = prefs[Keys.NOTIFY_TIP] ?: true,
            notifyQuizReminder = prefs[Keys.NOTIFY_QUIZ] ?: true,
            notifyCommunityReplies = prefs[Keys.NOTIFY_COMMUNITY] ?: true,
            bellSubscribed = prefs[Keys.BELL_SUBSCRIBED] ?: false,
            localXp = prefs[Keys.LOCAL_XP] ?: 50,
            localStreak = prefs[Keys.LOCAL_STREAK] ?: 1,
            answeredQuizIds = prefs[Keys.ANSWERED_QUIZ_IDS] ?: emptySet(),
            lastCheckInEpochDay = prefs[Keys.LAST_CHECKIN_DAY]?.toLongOrNull() ?: 0L
        )
    }

    suspend fun setLanguage(code: String) {
        context.userPrefsDataStore.edit { it[Keys.LANGUAGE] = code }
    }

    suspend fun setThemeMode(mode: String) {
        context.userPrefsDataStore.edit { it[Keys.THEME_MODE] = mode }
    }

    suspend fun setFontSize(size: String) {
        context.userPrefsDataStore.edit { it[Keys.FONT_SIZE] = size }
    }

    suspend fun setDataSaver(enabled: Boolean) {
        context.userPrefsDataStore.edit { it[Keys.DATA_SAVER] = enabled }
    }

    suspend fun completeOnboarding(language: String, categories: Set<String>) {
        context.userPrefsDataStore.edit {
            it[Keys.ONBOARDING_DONE] = true
            it[Keys.LANGUAGE] = language
            it[Keys.FAVORITE_CATEGORIES] = categories
        }
    }

    suspend fun setFavoriteCategories(categories: Set<String>) {
        context.userPrefsDataStore.edit { it[Keys.FAVORITE_CATEGORIES] = categories }
    }

    suspend fun setGuestPreviewChosen(chosen: Boolean) {
        context.userPrefsDataStore.edit { it[Keys.GUEST_PREVIEW] = chosen }
    }

    suspend fun updateNotificationSetting(keyName: String, enabled: Boolean) {
        context.userPrefsDataStore.edit { prefs ->
            when (keyName) {
                "uploads" -> prefs[Keys.NOTIFY_UPLOADS] = enabled
                "tip" -> prefs[Keys.NOTIFY_TIP] = enabled
                "quiz" -> prefs[Keys.NOTIFY_QUIZ] = enabled
                "community" -> prefs[Keys.NOTIFY_COMMUNITY] = enabled
            }
        }
    }

    suspend fun setBellSubscribed(subscribed: Boolean) {
        context.userPrefsDataStore.edit { it[Keys.BELL_SUBSCRIBED] = subscribed }
    }

    suspend fun addLocalXp(delta: Int, answeredQuizId: String? = null) {
        context.userPrefsDataStore.edit { prefs ->
            val current = prefs[Keys.LOCAL_XP] ?: 50
            prefs[Keys.LOCAL_XP] = current + delta
            if (answeredQuizId != null) {
                val existing = prefs[Keys.ANSWERED_QUIZ_IDS] ?: emptySet()
                prefs[Keys.ANSWERED_QUIZ_IDS] = existing + answeredQuizId
            }
        }
    }

    suspend fun recordDailyCheckIn(todayEpochDay: Long): Boolean {
        var awarded = false
        context.userPrefsDataStore.edit { prefs ->
            val lastDay = prefs[Keys.LAST_CHECKIN_DAY]?.toLongOrNull() ?: 0L
            if (todayEpochDay > lastDay) {
                awarded = true
                val currentStreak = prefs[Keys.LOCAL_STREAK] ?: 1
                prefs[Keys.LOCAL_STREAK] = if (lastDay == todayEpochDay - 1L) currentStreak + 1 else 1
                prefs[Keys.LAST_CHECKIN_DAY] = todayEpochDay.toString()
                prefs[Keys.LOCAL_XP] = (prefs[Keys.LOCAL_XP] ?: 50) + 15
            }
        }
        return awarded
    }
}
