package com.example.config

import com.example.BuildConfig

object AppConfig {
    // Loaded via Secrets Gradle Plugin from .env / .env.example (or AI Studio Secrets panel)
    val YOUTUBE_API_KEY: String = BuildConfig.YOUTUBE_API_KEY
    val CHANNEL_ID: String = BuildConfig.CHANNEL_ID

    const val CHANNEL_NAME = "Kabir Tech Lab"
    const val CHANNEL_HANDLE = "@KabirTechLab"
    const val ADMIN_EMAIL = "kabir020314@gmail.com"
    const val APP_VERSION_NAME = "1.0.0"
    const val LATEST_REMOTE_VERSION = "1.1.0"

    // Community & Support links (replace with exact channel URLs when publishing)
    const val YOUTUBE_CHANNEL_URL = "https://www.youtube.com/@KabirTechLab?sub_confirmation=1"
    const val WHATSAPP_GROUP_URL = "https://chat.whatsapp.com/KabirTechLabCommunity"
    const val TELEGRAM_GROUP_URL = "https://t.me/KabirTechLab"
    const val FACEBOOK_PAGE_URL = "https://www.facebook.com/KabirTechLab"
    const val INSTAGRAM_URL = "https://www.instagram.com/kabirtechlab"
    const val SUPPORT_DONATION_URL = "https://www.buymeacoffee.com/kabirtechlab"
    const val PLAY_STORE_URL = "https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}"

    fun isYouTubeApiConfigured(): Boolean {
        return YOUTUBE_API_KEY.isNotBlank() &&
            !YOUTUBE_API_KEY.startsWith("YOUR_") &&
            CHANNEL_ID.isNotBlank() &&
            !CHANNEL_ID.startsWith("UC_KABIR")
    }
}
