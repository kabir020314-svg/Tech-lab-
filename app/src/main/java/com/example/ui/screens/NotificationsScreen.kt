package com.example.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.data.local.UserPreferencesState
import com.example.data.model.NotificationItem
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.tr
import com.example.util.NotificationHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    lang: AppLanguage,
    prefs: UserPreferencesState,
    notifications: List<NotificationItem>,
    onBack: () -> Unit,
    onTogglePreference: (String, Boolean) -> Unit,
    onOpenVideo: (String) -> Unit
) {
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            NotificationHelper.showLocalNotification(
                context,
                tr(lang, "কবির টেক ল্যাব অ্যালার্ট চালু হয়েছে!", "Kabir Tech Lab Alerts Enabled!"),
                tr(lang, "নতুন ভিডিও ও ডেইলি টিপ সাথে সাথে পাবেন।", "You will receive new upload & daily tip alerts.")
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(tr(lang, "নোটিফিকেশন ও অ্যালার্ট", "Notifications & Alerts")) },
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f))
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(tr(lang, "নোটিফিকেশন সেটিংস", "Notification Preferences"), style = MaterialTheme.typography.titleMedium)
                        NotificationToggleRow(
                            label = tr(lang, "নতুন ভিডিও আপলোড অ্যালার্ট", "New Video Upload Alerts"),
                            checked = prefs.notifyNewUploads,
                            onCheckedChange = { onTogglePreference("uploads", it) }
                        )
                        NotificationToggleRow(
                            label = tr(lang, "দৈনিক টেক টিপ নোটিফিকেশন", "Daily Tech Tip Notification"),
                            checked = prefs.notifyDailyTip,
                            onCheckedChange = { onTogglePreference("tip", it) }
                        )
                        NotificationToggleRow(
                            label = tr(lang, "ডেইলি কুইজ রিমাইন্ডার", "Daily Quiz Reminder"),
                            checked = prefs.notifyQuizReminder,
                            onCheckedChange = { onTogglePreference("quiz", it) }
                        )
                        NotificationToggleRow(
                            label = tr(lang, "কমিউনিটি রিপ্লাই অ্যালার্ট", "Community Reply Alerts"),
                            checked = prefs.notifyCommunityReplies,
                            onCheckedChange = { onTogglePreference("community", it) }
                        )
                        Spacer(Modifier.height(4.dp))
                        Button(
                            onClick = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    NotificationHelper.showLocalNotification(
                                        context,
                                        tr(lang, "আজকের টেক টিপ", "Daily Tech Tip"),
                                        tr(lang, "নতুন Jetpack Compose ও বাংলা গল্প ভিডিও দেখুন!", "Check out the latest Compose tutorial & Bangla story!")
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Rounded.NotificationsActive, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(tr(lang, "টেস্ট নোটিফিকেশন পাঠান", "Enable & Test Push Alert"))
                        }
                    }
                }
            }

            item {
                Text(tr(lang, "সাম্প্রতিক অ্যালার্ট", "Recent Alerts"), style = MaterialTheme.typography.titleMedium)
            }

            items(notifications, key = { it.id }) { item ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.68f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = item.targetVideoId != null) {
                            item.targetVideoId?.let(onOpenVideo)
                        }
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = if (lang == AppLanguage.BANGLA) item.titleBn else item.titleEn,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = if (lang == AppLanguage.BANGLA) item.bodyBn else item.bodyEn,
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(item.timeLabel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
