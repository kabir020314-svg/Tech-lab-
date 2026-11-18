package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.data.model.VideoItem
import com.example.data.model.VideoRequestDoc
import com.example.ui.components.formatCount
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.tr

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    lang: AppLanguage,
    isAdminUser: Boolean,
    videos: List<VideoItem>,
    subscriberCount: Long,
    totalViews: Long,
    requests: List<VideoRequestDoc>,
    onBack: () -> Unit,
    onPublishAnnouncement: (titleBn: String, titleEn: String, bodyBn: String, bodyEn: String) -> Unit,
    onCreatePoll: (qBn: String, qEn: String, options: List<String>) -> Unit,
    onCreateQuiz: (qBn: String, qEn: String, optsBn: List<String>, optsEn: List<String>, correctIdx: Int, expBn: String, expEn: String) -> Unit,
    onAddResource: (videoId: String, titleBn: String, titleEn: String, type: String, url: String) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(tr(lang, "অ্যাডমিন প্যানেল", "Admin Control Panel")) },
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
            if (!isAdminUser) {
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.14f))) {
                        Text(
                            text = tr(
                                lang,
                                "অ্যাডমিন অ্যাক্সেসের জন্য অনুমোদিত গুগল অ্যাকাউন্ট (kabir020314@gmail.com) দিয়ে লগইন করুন।",
                                "Sign in with the verified admin account (kabir020314@gmail.com) to publish changes to Firestore."
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }

            // Analytics Overview
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(tr(lang, "চ্যানেল অ্যানালিটিক্স", "Basic Analytics"), style = MaterialTheme.typography.titleMedium)
                        Text("${tr(lang, "সাবস্ক্রাইবার", "Subscribers")}: ${formatCount(subscriberCount)}")
                        Text("${tr(lang, "মোট ভিউ", "Total Views")}: ${formatCount(totalViews)}")
                        Text("${tr(lang, "টপ ভিডিও", "Top Video")}: ${videos.maxByOrNull { it.viewCount }?.titleEn.orEmpty()}")
                    }
                }
            }

            // Pin Home Announcement & Push Notice
            item {
                var titleBn by remember { mutableStateOf("") }
                var titleEn by remember { mutableStateOf("") }
                var bodyBn by remember { mutableStateOf("") }
                var bodyEn by remember { mutableStateOf("") }
                Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(tr(lang, "অ্যানাউন্সমেন্ট ও পুশ নোটিশ", "Pin Announcement / Push Alert"), style = MaterialTheme.typography.titleMedium)
                        OutlinedTextField(value = titleBn, onValueChange = { titleBn = it }, label = { Text("Title (Bangla)") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = titleEn, onValueChange = { titleEn = it }, label = { Text("Title (English)") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = bodyBn, onValueChange = { bodyBn = it }, label = { Text("Body (Bangla)") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = bodyEn, onValueChange = { bodyEn = it }, label = { Text("Body (English)") }, modifier = Modifier.fillMaxWidth())
                        Button(
                            enabled = isAdminUser && titleBn.isNotBlank() && bodyBn.isNotBlank(),
                            onClick = {
                                onPublishAnnouncement(titleBn, titleEn.ifBlank { titleBn }, bodyBn, bodyEn.ifBlank { bodyBn })
                                titleBn = ""; titleEn = ""; bodyBn = ""; bodyEn = ""
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(tr(lang, "পাবলিশ করুন", "Publish Banner"))
                        }
                    }
                }
            }

            // Create Poll
            item {
                var qBn by remember { mutableStateOf("") }
                var qEn by remember { mutableStateOf("") }
                var opt1 by remember { mutableStateOf("") }
                var opt2 by remember { mutableStateOf("") }
                Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(tr(lang, "নতুন পোল তৈরি করুন", "Create Topic Poll"), style = MaterialTheme.typography.titleMedium)
                        OutlinedTextField(value = qBn, onValueChange = { qBn = it }, label = { Text("Question (Bangla)") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = qEn, onValueChange = { qEn = it }, label = { Text("Question (English)") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = opt1, onValueChange = { opt1 = it }, label = { Text("Option 1") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = opt2, onValueChange = { opt2 = it }, label = { Text("Option 2") }, modifier = Modifier.fillMaxWidth())
                        Button(
                            enabled = isAdminUser && qBn.isNotBlank() && opt1.isNotBlank() && opt2.isNotBlank(),
                            onClick = {
                                onCreatePoll(qBn, qEn.ifBlank { qBn }, listOf(opt1, opt2))
                                qBn = ""; qEn = ""; opt1 = ""; opt2 = ""
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(tr(lang, "পোল তৈরি করুন", "Create Poll"))
                        }
                    }
                }
            }

            // Add Resource to Video
            item {
                var videoId by remember { mutableStateOf(videos.firstOrNull()?.id ?: "") }
                var resTitle by remember { mutableStateOf("") }
                var resType by remember { mutableStateOf("PDF") }
                var resUrl by remember { mutableStateOf("") }
                Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(tr(lang, "ভিডিওতে রিসোর্স যোগ করুন", "Add Video Resource"), style = MaterialTheme.typography.titleMedium)
                        OutlinedTextField(value = videoId, onValueChange = { videoId = it }, label = { Text("Video ID") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = resTitle, onValueChange = { resTitle = it }, label = { Text("Resource Title") }, modifier = Modifier.fillMaxWidth())
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("PDF", "CODE", "TOOL", "LINK").forEach { t ->
                                FilterChip(selected = resType == t, onClick = { resType = t }, label = { Text(t) })
                            }
                        }
                        OutlinedTextField(value = resUrl, onValueChange = { resUrl = it }, label = { Text("URL") }, modifier = Modifier.fillMaxWidth())
                        Button(
                            enabled = isAdminUser && videoId.isNotBlank() && resTitle.isNotBlank() && resUrl.length >= 4,
                            onClick = {
                                onAddResource(videoId, resTitle, resTitle, resType, resUrl)
                                resTitle = ""; resUrl = ""
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(tr(lang, "রিসোর্স যুক্ত করুন", "Attach Resource"))
                        }
                    }
                }
            }

            // Create Quiz Question
            item {
                var qText by remember { mutableStateOf("") }
                var a0 by remember { mutableStateOf("") }
                var a1 by remember { mutableStateOf("") }
                var a2 by remember { mutableStateOf("") }
                var a3 by remember { mutableStateOf("") }
                var correctIdx by remember { mutableIntStateOf(0) }
                var explanation by remember { mutableStateOf("") }
                Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(tr(lang, "কুইজ প্রশ্ন যোগ করুন", "Add Quiz Question"), style = MaterialTheme.typography.titleMedium)
                        OutlinedTextField(value = qText, onValueChange = { qText = it }, label = { Text("Question") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = a0, onValueChange = { a0 = it }, label = { Text("Option 1") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = a1, onValueChange = { a1 = it }, label = { Text("Option 2") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = a2, onValueChange = { a2 = it }, label = { Text("Option 3") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = a3, onValueChange = { a3 = it }, label = { Text("Option 4") }, modifier = Modifier.fillMaxWidth())
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            (0..3).forEach { i ->
                                FilterChip(selected = correctIdx == i, onClick = { correctIdx = i }, label = { Text("Ans #${i + 1}") })
                            }
                        }
                        OutlinedTextField(value = explanation, onValueChange = { explanation = it }, label = { Text("Explanation") }, modifier = Modifier.fillMaxWidth())
                        Button(
                            enabled = isAdminUser && qText.isNotBlank() && listOf(a0, a1, a2, a3).all { it.isNotBlank() } && explanation.isNotBlank(),
                            onClick = {
                                val opts = listOf(a0, a1, a2, a3)
                                onCreateQuiz(qText, qText, opts, opts, correctIdx, explanation, explanation)
                                qText = ""; a0 = ""; a1 = ""; a2 = ""; a3 = ""; explanation = ""
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(tr(lang, "কুইজ সেভ করুন", "Save Quiz"))
                        }
                    }
                }
            }

            if (requests.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(4.dp))
                    Text(tr(lang, "দর্শকদের রিকোয়েস্ট ও ফিডব্যাক", "Viewer Requests & Feedback"), style = MaterialTheme.typography.titleMedium)
                }
                items(requests, key = { it.id }) { req ->
                    Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                        Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                            Text("${req.requestType}: ${req.topic}", style = MaterialTheme.typography.titleSmall)
                            Text("${req.requesterName} • ${req.details}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}
