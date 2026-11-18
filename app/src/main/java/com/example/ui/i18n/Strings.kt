package com.example.ui.i18n

enum class AppLanguage(val code: String, val label: String) {
    BANGLA("bn", "বাংলা"),
    ENGLISH("en", "English");

    companion object {
        fun fromCode(code: String): AppLanguage =
            entries.firstOrNull { it.code == code } ?: BANGLA
    }
}

fun tr(lang: AppLanguage, bn: String, en: String): String =
    if (lang == AppLanguage.BANGLA) bn else en

object AppStrings {
    fun appTitle(lang: AppLanguage) = tr(lang, "কবির টেক ল্যাব", "Kabir Tech Lab")
    fun tagline(lang: AppLanguage) = tr(
        lang,
        "টেক টিউটোরিয়াল ও বাংলা গল্পের স্মার্ট প্ল্যাটফর্ম",
        "Smart Hub for Tech Tutorials & Bangla Stories"
    )

    // Bottom Navigation
    fun navHome(lang: AppLanguage) = tr(lang, "হোম", "Home")
    fun navVideos(lang: AppLanguage) = tr(lang, "ভিডিও", "Videos")
    fun navPlaylists(lang: AppLanguage) = tr(lang, "প্লেলিস্ট", "Series")
    fun navQuiz(lang: AppLanguage) = tr(lang, "কুইজ", "Quiz")
    fun navCommunity(lang: AppLanguage) = tr(lang, "কমিউনিটি", "Community")

    // Categories
    fun catAll(lang: AppLanguage) = tr(lang, "সব", "All")
    fun catTech(lang: AppLanguage) = tr(lang, "টেক টিউটোরিয়াল", "Tech Tutorials")
    fun catStories(lang: AppLanguage) = tr(lang, "বাংলা গল্প", "Stories")
    fun catTips(lang: AppLanguage) = tr(lang, "টেক টিপস", "Tips")
    fun catShorts(lang: AppLanguage) = tr(lang, "শর্টস", "Shorts")

    // Actions & Common
    fun watchNow(lang: AppLanguage) = tr(lang, "এখনই দেখুন", "Watch Now")
    fun continueWatching(lang: AppLanguage) = tr(lang, "যেখান থেকে শেষ করেছেন", "Continue Watching")
    fun trendingVideos(lang: AppLanguage) = tr(lang, "ট্রেন্ডিং ভিডিও", "Trending Videos")
    fun todaysTechTip(lang: AppLanguage) = tr(lang, "আজকের টেক টিপ", "Today's Tech Tip")
    fun featuredStory(lang: AppLanguage) = tr(lang, "বিশেষ বাংলা গল্প", "Featured Story")
    fun retry(lang: AppLanguage) = tr(lang, "আবার চেষ্টা করুন", "Retry")
    fun save(lang: AppLanguage) = tr(lang, "সেভ", "Save")
    fun saved(lang: AppLanguage) = tr(lang, "সেভ করা হয়েছে", "Saved")
    fun share(lang: AppLanguage) = tr(lang, "শেয়ার", "Share")
    fun like(lang: AppLanguage) = tr(lang, "লাইক", "Like")
    fun addNote(lang: AppLanguage) = tr(lang, "নোট যোগ করুন", "Add Note")
    fun loadMore(lang: AppLanguage) = tr(lang, "আরও ভিডিও দেখুন", "Load More")
    fun signInWithGoogle(lang: AppLanguage) = tr(lang, "গুগল দিয়ে লগইন করুন", "Sign in with Google")
    fun continueGuest(lang: AppLanguage) = tr(lang, "অফলাইন প্রিভিউ মোডে চালান", "Continue in Offline Preview")
    fun signInRequiredTitle(lang: AppLanguage) = tr(
        lang,
        "ক্লাউড সিঙ্ক ও কমিউনিটির জন্য লগইন করুন",
        "Sign in for Cloud Sync & Community"
    )
    fun signInRequiredBody(lang: AppLanguage) = tr(
        lang,
        "ফায়ারবেস সিঙ্ক, পোস্ট, ভোট এবং লিডারবোর্ডে অংশ নিতে গুগল অ্যাকাউন্ট দিয়ে সাইন-ইন করুন।",
        "Sign in with Google to sync notes, post in community, vote on polls, and join the leaderboard."
    )
}
