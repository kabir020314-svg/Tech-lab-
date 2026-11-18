package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material.icons.rounded.CleaningServices
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Feedback
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Policy
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.StarRate
import androidx.compose.material.icons.rounded.Subscriptions
import androidx.compose.material.icons.rounded.SystemUpdate
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import com.example.config.AppConfig
import com.example.data.firebase.launchGoogleSignIn
import com.example.data.firebase.performGoogleSignOut
import com.example.data.local.UserPreferencesState
import com.example.data.model.AppCategory
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.AppStrings
import com.example.ui.i18n.tr
import com.example.ui.theme.AmberHighlight

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProfileAndSettingsScreen(
    lang: AppLanguage,
    isSignedIn: Boolean,
    isAdmin: Boolean,
    displayName: String?,
    email: String?,
    prefs: UserPreferencesState,
    watchedVideosCount: Int,
    xpPoints: Int,
    streakDays: Int,
    onBack: () -> Unit,
    onSetLanguage: (String) -> Unit,
    onSetThemeMode: (String) -> Unit,
    onSetFontSize: (String) -> Unit,
    onSetDataSaver: (Boolean) -> Unit,
    onSetFavoriteCategories: (Set<String>) -> Unit,
    onClearCache: () -> Unit,
    onToggleBell: () -> Unit,
    onSubmitFeedback: (topic: String, details: String, (Boolean) -> Unit) -> Unit,
    onOpenAdminPanel: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    var infoDialogContent by remember { mutableStateOf<Pair<String, String>?>(null) }
    var showFeedbackDialog by remember { mutableStateOf(false) }
    var versionTapCount by remember { mutableIntStateOf(0) }

    infoDialogContent?.let { (title, body) ->
        AlertDialog(
            onDismissRequest = { infoDialogContent = null },
            title = { Text(title) },
            text = { Text(body, style = MaterialTheme.typography.bodyMedium) },
            confirmButton = {
                TextButton(onClick = { infoDialogContent = null }) {
                    Text(tr(lang, "ঠিক আছে", "OK"))
                }
            }
        )
    }

    if (showFeedbackDialog) {
        var subject by remember { mutableStateOf("") }
        var message by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showFeedbackDialog = false },
            title = { Text(tr(lang, "যোগাযোগ ও মতামত", "Contact & Feedback")) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = subject,
                        onValueChange = { subject = it },
                        label = { Text(tr(lang, "বিষয়", "Subject")) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = message,
                        onValueChange = { message = it },
                        label = { Text(tr(lang, "আপনার বার্তা", "Your feedback")) },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (subject.trim().length >= 2 && message.trim().length >= 2) {
                        onSubmitFeedback(subject, message) { ok ->
                            if (ok) showFeedbackDialog = false
                        }
                    }
                }) {
                    Text(tr(lang, "পাঠান", "Send"))
                }
            },
            dismissButton = {
                TextButton(onClick = { showFeedbackDialog = false }) {
                    Text(tr(lang, "বাতিল", "Cancel"))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(tr(lang, "প্রোফাইল ও সেটিংস", "Profile & Settings")) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Account Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.AccountCircle, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isSignedIn) (displayName ?: "Kabir Tech Member") else tr(lang, "গেস্ট / অফলাইন মোড", "Guest / Offline Mode"),
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = if (isSignedIn) (email ?: "") else tr(lang, "ক্লাউড সিঙ্কের জন্য গুগল লগইন করুন", "Sign in with Google for cloud sync"),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            StatPill("$watchedVideosCount", tr(lang, "ভিডিও দেখা", "Watched"))
                            StatPill("$xpPoints XP", tr(lang, "মোট পয়েন্ট", "XP"))
                            StatPill("$streakDays d", tr(lang, "স্ট্রিক", "Streak"))
                        }
                        Spacer(Modifier.height(12.dp))
                        if (isSignedIn) {
                            OutlinedButton(
                                onClick = { performGoogleSignOut(credentialManager, scope) },
                                modifier = Modifier.fillMaxWidth().testTag("profile_sign_out_btn")
                            ) {
                                Icon(Icons.AutoMirrored.Rounded.Logout, contentDescription = null)
                                Spacer(Modifier.width(6.dp))
                                Text(tr(lang, "লগআউট করুন", "Sign Out"))
                            }
                        } else {
                            Button(
                                onClick = {
                                    launchGoogleSignIn(context, credentialManager, scope, {}, {}, {})
                                },
                                modifier = Modifier.fillMaxWidth().testTag("profile_google_signin_btn")
                            ) {
                                Text(AppStrings.signInWithGoogle(lang))
                            }
                        }
                    }
                }
            }

            // In-App Update Notice
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Rounded.SystemUpdate, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(tr(lang, "নতুন আপডেট উপলব্ধ (v${AppConfig.LATEST_REMOTE_VERSION})", "Update Available (v${AppConfig.LATEST_REMOTE_VERSION})"), style = MaterialTheme.typography.titleSmall)
                            Text(tr(lang, "আরও দ্রুত ভিডিও লোডিং ও নতুন কুইজ মোড", "Faster video cache & new quiz milestones"), style = MaterialTheme.typography.bodySmall)
                        }
                        TextButton(onClick = {
                            runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(AppConfig.PLAY_STORE_URL))) }
                        }) {
                            Text(tr(lang, "আপডেট", "Update"))
                        }
                    }
                }
            }

            // Preferences: Language, Theme, Font Size, Data Saver, Cache
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(tr(lang, "অ্যাপ সেটিংস", "Appearance & Preferences"), style = MaterialTheme.typography.titleMedium)

                        Text(tr(lang, "ভাষা / Language", "Language"), style = MaterialTheme.typography.labelLarge)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            AppLanguage.entries.forEach { opt ->
                                FilterChip(
                                    selected = prefs.languageCode == opt.code,
                                    onClick = { onSetLanguage(opt.code) },
                                    label = { Text(opt.label) }
                                )
                            }
                        }

                        Text(tr(lang, "থিম", "Theme"), style = MaterialTheme.typography.labelLarge)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("dark" to tr(lang, "ডার্ক", "Dark"), "light" to tr(lang, "লাইট", "Light"), "system" to tr(lang, "সিস্টেম", "System")).forEach { (key, lbl) ->
                                FilterChip(
                                    selected = prefs.themeMode == key,
                                    onClick = { onSetThemeMode(key) },
                                    label = { Text(lbl) }
                                )
                            }
                        }

                        Text(tr(lang, "ফন্ট সাইজ", "Font Size"), style = MaterialTheme.typography.labelLarge)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("small" to tr(lang, "ছোট", "Small"), "medium" to tr(lang, "মাঝারি", "Medium"), "large" to tr(lang, "বড়", "Large")).forEach { (key, lbl) ->
                                FilterChip(
                                    selected = prefs.fontSize == key,
                                    onClick = { onSetFontSize(key) },
                                    label = { Text(lbl) }
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(tr(lang, "ডেটা সেভার মোড", "Data Saver Mode"), style = MaterialTheme.typography.bodyMedium)
                                Text(tr(lang, "থাম্বনেইল ইমেজ লোড সীমিত করে", "Reduces thumbnail bandwidth usage"), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(checked = prefs.dataSaver, onCheckedChange = onSetDataSaver)
                        }

                        Text(tr(lang, "পছন্দের ক্যাটাগরি", "Favorite Categories"), style = MaterialTheme.typography.labelLarge)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(AppCategory.TECH, AppCategory.STORIES, AppCategory.TIPS, AppCategory.SHORTS).forEach { cat ->
                                val selected = prefs.favoriteCategories.contains(cat.key)
                                FilterChip(
                                    selected = selected,
                                    onClick = {
                                        val next = if (selected && prefs.favoriteCategories.size > 1) {
                                            prefs.favoriteCategories - cat.key
                                        } else prefs.favoriteCategories + cat.key
                                        onSetFavoriteCategories(next)
                                    },
                                    label = { Text(if (lang == AppLanguage.BANGLA) cat.bnLabel else cat.enLabel) }
                                )
                            }
                        }

                        OutlinedButton(onClick = onClearCache, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Rounded.CleaningServices, contentDescription = null)
                            Spacer(Modifier.width(6.dp))
                            Text(tr(lang, "লোকাল ক্যাশ পরিষ্কার করুন", "Clear Local Cache"))
                        }
                    }
                }
            }

            // Premium & Channel Support Section
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.WorkspacePremium, contentDescription = null, tint = AmberHighlight)
                            Spacer(Modifier.width(8.dp))
                            Text(tr(lang, "মেম্বারশিপ ও চ্যানেল সাপোর্ট", "Membership & Channel Support"), style = MaterialTheme.typography.titleMedium)
                        }
                        Text(
                            text = tr(
                                lang,
                                "• অ্যাড-ফ্রি রিডিং ও এক্সক্লুসিভ সোর্স কোড\n• নতুন বাংলা গল্প সিরিজ আর্লি অ্যাক্সেস\n• প্রিমিয়াম PDF নোটস ও কমিউনিটি ব্যাজ",
                                "• Ad-free experience & exclusive source code\n• Early access to Bangla story series\n• Premium PDF notes & supporter badge"
                            ),
                            style = MaterialTheme.typography.bodySmall
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(AppConfig.YOUTUBE_CHANNEL_URL))) }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Rounded.Subscriptions, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(tr(lang, "সাবস্ক্রাইব", "Subscribe"))
                            }
                            OutlinedButton(
                                onClick = onToggleBell,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Rounded.NotificationsActive, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(if (prefs.bellSubscribed) tr(lang, "বেল অন", "Bell On") else tr(lang, "বেল বাজান", "Bell Alert"))
                            }
                        }
                        OutlinedButton(
                            onClick = {
                                runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(AppConfig.SUPPORT_DONATION_URL))) }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Rounded.Favorite, contentDescription = null, tint = AmberHighlight)
                            Spacer(Modifier.width(6.dp))
                            Text(tr(lang, "চ্যানেলকে সাপোর্ট করুন", "Support Kabir Tech Lab"))
                        }
                    }
                }
            }

            // Extras: Share, Rate, Feedback, About, Privacy, Terms, Hidden Admin
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f))
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        SettingsActionItem(Icons.Rounded.Feedback, tr(lang, "যোগাযোগ ও মতামত", "Contact & Feedback")) {
                            showFeedbackDialog = true
                        }
                        SettingsActionItem(Icons.Rounded.Share, tr(lang, "অ্যাপ শেয়ার করুন", "Share App")) {
                            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                putExtra(Intent.EXTRA_TEXT, "Kabir Tech Lab: ${AppConfig.YOUTUBE_CHANNEL_URL}")
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share App"))
                        }
                        SettingsActionItem(Icons.Rounded.StarRate, tr(lang, "অ্যাপ রেট দিন", "Rate App")) {
                            runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(AppConfig.PLAY_STORE_URL))) }
                        }
                        SettingsActionItem(Icons.Rounded.Info, tr(lang, "আমাদের সম্পর্কে (About)", "About Kabir Tech Lab")) {
                            infoDialogContent = tr(lang, "আমাদের সম্পর্কে", "About") to tr(
                                lang,
                                "Kabir Tech Lab হলো বাংলা ভাষায় আধুনিক প্রযুক্তি, কোডিং টিউটোরিয়াল এবং অনুপ্রেরণামূলক বাংলা টেক গল্পের অফিশিয়াল মোবাইল প্ল্যাটফর্ম।",
                                "Kabir Tech Lab is the official companion app for Bangla tech tutorials, coding guides, and tech thriller stories."
                            )
                        }
                        SettingsActionItem(Icons.Rounded.Policy, tr(lang, "প্রাইভেসি পলিসি ও শর্তাবলী", "Privacy Policy & Terms")) {
                            infoDialogContent = tr(lang, "প্রাইভেসি ও শর্তাবলী", "Privacy Policy & Terms") to tr(
                                lang,
                                "আমরা আপনার ইমেইল বা ফোন নম্বর কখনো তৃতীয় পক্ষের কাছে বিক্রি করি না। শুধুমাত্র আপনার লগইন, নোটস ও কমিউনিটি ফিচার সিঙ্ক করার জন্য Firebase ব্যবহার করা হয়।",
                                "We respect your privacy. Profile info and community submissions are securely stored in Firebase solely for app functionality and sync."
                            )
                        }

                        if (isAdmin || versionTapCount >= 5) {
                            SettingsActionItem(Icons.Rounded.AdminPanelSettings, tr(lang, "অ্যাডমিন প্যানেল", "Admin Panel")) {
                                onOpenAdminPanel()
                            }
                        }

                        Text(
                            text = "Version ${AppConfig.APP_VERSION_NAME} • Kabir Tech Lab",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { versionTapCount++ }
                                .padding(12.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatPill(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun SettingsActionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(12.dp))
        Text(label, style = MaterialTheme.typography.bodyLarge)
    }
}
