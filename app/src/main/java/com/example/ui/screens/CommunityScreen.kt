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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Comment
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.Forum
import androidx.compose.material.icons.rounded.GroupAdd
import androidx.compose.material.icons.rounded.HowToVote
import androidx.compose.material.icons.rounded.ThumbUp
import androidx.compose.material.icons.rounded.VideoCall
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.config.AppConfig
import com.example.data.firebase.UiState
import com.example.data.model.CommunityPostDoc
import com.example.data.model.CommunityReplyDoc
import com.example.data.model.PollDoc
import com.example.ui.components.EmptyStateCard
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.AppStrings
import com.example.ui.i18n.tr
import kotlinx.coroutines.flow.Flow

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CommunityScreen(
    lang: AppLanguage,
    isSignedIn: Boolean,
    currentUserId: String?,
    isAdmin: Boolean,
    postsState: UiState<List<CommunityPostDoc>>,
    pollsState: UiState<List<PollDoc>>,
    observeReplies: (String) -> Flow<List<CommunityReplyDoc>>,
    onRequireSignIn: () -> Unit,
    onCreatePost: (title: String, body: String, category: String, onDone: (Boolean) -> Unit) -> Unit,
    onLikePost: (CommunityPostDoc) -> Unit,
    onReportPost: (CommunityPostDoc) -> Unit,
    onDeletePost: (String) -> Unit,
    onAddReply: (CommunityPostDoc, String, (Boolean) -> Unit) -> Unit,
    onVotePoll: (PollDoc, Int) -> Unit,
    onSubmitJoin: (fullName: String, contactInfo: String, interests: List<String>, (Boolean) -> Unit) -> Unit,
    onSubmitVideoRequest: (topic: String, details: String, (Boolean) -> Unit) -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    var showNewPostDialog by remember { mutableStateOf(false) }
    var activeReplyPost by remember { mutableStateOf<CommunityPostDoc?>(null) }

    if (showNewPostDialog) {
        var title by remember { mutableStateOf("") }
        var body by remember { mutableStateOf("") }
        var category by remember { mutableStateOf("Tech Tutorials") }
        AlertDialog(
            onDismissRequest = { showNewPostDialog = false },
            title = { Text(tr(lang, "প্রশ্ন বা পোস্ট লিখুন", "Ask a Question / Post")) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text(tr(lang, "শিরোনাম", "Title")) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = body,
                        onValueChange = { body = it },
                        label = { Text(tr(lang, "বিস্তারিত লিখুন", "Details")) },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("Tech Tutorials", "Stories", "Tips").forEach { cat ->
                            FilterChip(
                                selected = category == cat,
                                onClick = { category = cat },
                                label = { Text(cat) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.trim().length >= 2 && body.trim().length >= 2) {
                            onCreatePost(title, body, category) { ok ->
                                if (ok) showNewPostDialog = false
                            }
                        }
                    }
                ) {
                    Text(tr(lang, "পোস্ট করুন", "Publish"))
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewPostDialog = false }) {
                    Text(tr(lang, "বাতিল", "Cancel"))
                }
            }
        )
    }

    activeReplyPost?.let { post ->
        val replies by observeReplies(post.id).collectAsStateWithLifecycle(initialValue = emptyList())
        var replyText by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { activeReplyPost = null },
            title = { Text(post.title, maxLines = 2) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(post.body, style = MaterialTheme.typography.bodySmall)
                    if (replies.isEmpty()) {
                        Text(
                            text = tr(lang, "এখনও কোনো উত্তর নেই।", "No replies yet."),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        replies.take(5).forEach { r ->
                            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                                Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                                    Text(r.authorName, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                    Text(r.text, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                    OutlinedTextField(
                        value = replyText,
                        onValueChange = { replyText = it },
                        label = { Text(tr(lang, "উত্তর লিখুন...", "Write a reply...")) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (!isSignedIn) {
                        onRequireSignIn()
                    } else if (replyText.isNotBlank()) {
                        onAddReply(post, replyText) { ok ->
                            if (ok) replyText = ""
                        }
                    }
                }) {
                    Text(tr(lang, "পাঠান", "Reply"))
                }
            },
            dismissButton = {
                TextButton(onClick = { activeReplyPost = null }) {
                    Text(tr(lang, "বন্ধ করুন", "Close"))
                }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Social Groups Quick Links
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val links = listOf(
                "WhatsApp" to AppConfig.WHATSAPP_GROUP_URL,
                "Telegram" to AppConfig.TELEGRAM_GROUP_URL,
                "Facebook" to AppConfig.FACEBOOK_PAGE_URL,
                "Instagram" to AppConfig.INSTAGRAM_URL
            )
            items(links) { (label, url) ->
                AssistChip(
                    onClick = {
                        runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
                    },
                    label = { Text(label) }
                )
            }
        }

        TabRow(selectedTabIndex = selectedTab) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text(tr(lang, "ফিড", "Feed")) })
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text(tr(lang, "পোল", "Polls")) })
            Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text(tr(lang, "জয়েন / রিকোয়েস্ট", "Join & Request")) })
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when (selectedTab) {
                0 -> {
                    item {
                        Button(
                            onClick = {
                                if (!isSignedIn) onRequireSignIn() else showNewPostDialog = true
                            },
                            modifier = Modifier.fillMaxWidth().testTag("community_new_post_btn")
                        ) {
                            Icon(Icons.Rounded.Add, contentDescription = null)
                            Spacer(Modifier.width(6.dp))
                            Text(tr(lang, "নতুন প্রশ্ন বা পোস্ট করুন", "Ask Question / New Post"))
                        }
                    }
                    when (postsState) {
                        is UiState.Loading -> {
                            item { Text(tr(lang, "লোড হচ্ছে...", "Loading feed...")) }
                        }
                        is UiState.Error -> {
                            item { Text(postsState.message, color = MaterialTheme.colorScheme.error) }
                        }
                        is UiState.Success -> {
                            if (postsState.data.isEmpty()) {
                                item {
                                    EmptyStateCard(
                                        icon = Icons.Rounded.Forum,
                                        title = tr(lang, "কোনো পোস্ট নেই", "No posts yet"),
                                        subtitle = tr(lang, "প্রথম প্রশ্নটি আপনিই করুন!", "Start the first discussion!")
                                    )
                                }
                            } else {
                                items(postsState.data, key = { it.id }) { post ->
                                    Card(
                                        shape = RoundedCornerShape(18.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f))
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(post.authorName, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                                                Surface(
                                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                                                    shape = RoundedCornerShape(8.dp)
                                                ) {
                                                    Text(
                                                        text = post.category,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                            Spacer(Modifier.height(6.dp))
                                            Text(post.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                                            Spacer(Modifier.height(4.dp))
                                            Text(post.body, style = MaterialTheme.typography.bodySmall)
                                            if (post.reported) {
                                                Spacer(Modifier.height(4.dp))
                                                Text(
                                                    text = tr(lang, "মডারেশনের জন্য রিপোর্ট করা হয়েছে", "Flagged for moderation"),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.error
                                                )
                                            }
                                            Spacer(Modifier.height(8.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row {
                                                    TextButton(onClick = { if (!isSignedIn) onRequireSignIn() else onLikePost(post) }) {
                                                        Icon(Icons.Rounded.ThumbUp, contentDescription = null, modifier = Modifier.size(16.dp))
                                                        Spacer(Modifier.width(4.dp))
                                                        Text("${post.likesCount}")
                                                    }
                                                    TextButton(onClick = { activeReplyPost = post }) {
                                                        Icon(Icons.AutoMirrored.Rounded.Comment, contentDescription = null, modifier = Modifier.size(16.dp))
                                                        Spacer(Modifier.width(4.dp))
                                                        Text("${post.repliesCount}")
                                                    }
                                                }
                                                Row {
                                                    IconButton(onClick = { if (!isSignedIn) onRequireSignIn() else onReportPost(post) }) {
                                                        Icon(Icons.Rounded.Flag, contentDescription = tr(lang, "রিপোর্ট", "Report"), modifier = Modifier.size(18.dp))
                                                    }
                                                    if (isSignedIn && (post.userId == currentUserId || isAdmin)) {
                                                        IconButton(onClick = { onDeletePost(post.id) }) {
                                                            Icon(Icons.Rounded.Delete, contentDescription = "Delete", modifier = Modifier.size(18.dp))
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                1 -> {
                    when (pollsState) {
                        is UiState.Loading -> item { Text(tr(lang, "পোল লোড হচ্ছে...", "Loading polls...")) }
                        is UiState.Error -> item { Text(pollsState.message, color = MaterialTheme.colorScheme.error) }
                        is UiState.Success -> {
                            if (pollsState.data.isEmpty()) {
                                item {
                                    EmptyStateCard(
                                        icon = Icons.Rounded.HowToVote,
                                        title = tr(lang, "কোনো সক্রিয় পোল নেই", "No active polls"),
                                        subtitle = tr(lang, "শীঘ্রই নতুন টপিক ভোট শুরু হবে।", "Check back soon for new video topic polls.")
                                    )
                                }
                            } else {
                                items(pollsState.data, key = { it.id }) { poll ->
                                    val totalVotes = poll.voteCounts.sum().coerceAtLeast(1L)
                                    Card(
                                        shape = RoundedCornerShape(18.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.78f))
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                            Text(
                                                text = if (lang == AppLanguage.BANGLA) poll.questionBn else poll.questionEn,
                                                style = MaterialTheme.typography.titleMedium
                                            )
                                            poll.options.forEachIndexed { idx, opt ->
                                                val votes = poll.voteCounts.getOrElse(idx) { 0L }
                                                val pct = (votes.toFloat() / totalVotes.toFloat()).coerceIn(0f, 1f)
                                                Column(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clickable {
                                                            if (!isSignedIn) onRequireSignIn() else onVotePoll(poll, idx)
                                                        }
                                                ) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween
                                                    ) {
                                                        Text(opt, style = MaterialTheme.typography.bodyMedium)
                                                        Text("${(pct * 100).toInt()}% ($votes)", style = MaterialTheme.typography.labelMedium)
                                                    }
                                                    Spacer(Modifier.height(4.dp))
                                                    LinearProgressIndicator(
                                                        progress = { pct },
                                                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp))
                                                    )
                                                }
                                            }
                                            Text(
                                                text = tr(lang, "ভোট দিতে যেকোনো অপশনে ট্যাপ করুন", "Tap an option to cast your vote"),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                else -> {
                    item {
                        var fullName by remember { mutableStateOf("") }
                        var contactInfo by remember { mutableStateOf("") }
                        var interests by remember { mutableStateOf(setOf("Tech Tutorials")) }

                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.78f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Rounded.GroupAdd, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(Modifier.width(8.dp))
                                    Text(tr(lang, "কমিউনিটি মেম্বারশিপ ফর্ম", "Join Kabir Tech Lab Community"), style = MaterialTheme.typography.titleMedium)
                                }
                                OutlinedTextField(
                                    value = fullName,
                                    onValueChange = { fullName = it },
                                    label = { Text(tr(lang, "আপনার নাম", "Full Name")) },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                OutlinedTextField(
                                    value = contactInfo,
                                    onValueChange = { contactInfo = it },
                                    label = { Text(tr(lang, "ফোন নম্বর বা ইমেইল", "Phone or Email")) },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    listOf("Tech Tutorials", "Stories", "Tips", "AI Tools").forEach { item ->
                                        val selected = interests.contains(item)
                                        FilterChip(
                                            selected = selected,
                                            onClick = {
                                                interests = if (selected) interests - item else interests + item
                                            },
                                            label = { Text(item) }
                                        )
                                    }
                                }
                                Button(
                                    onClick = {
                                        if (!isSignedIn) {
                                            onRequireSignIn()
                                        } else if (fullName.trim().length >= 2 && contactInfo.trim().length >= 3) {
                                            onSubmitJoin(fullName, contactInfo, interests.toList()) { ok ->
                                                if (ok) { fullName = ""; contactInfo = "" }
                                            }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().testTag("community_join_submit")
                                ) {
                                    Text(tr(lang, "জয়েন করুন", "Submit Join Form"))
                                }
                            }
                        }
                    }

                    item {
                        var topic by remember { mutableStateOf("") }
                        var details by remember { mutableStateOf("") }

                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.78f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Rounded.VideoCall, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                                    Spacer(Modifier.width(8.dp))
                                    Text(tr(lang, "নতুন ভিডিও রিকোয়েস্ট করুন", "Request a Video Topic"), style = MaterialTheme.typography.titleMedium)
                                }
                                OutlinedTextField(
                                    value = topic,
                                    onValueChange = { topic = it },
                                    label = { Text(tr(lang, "ভিডিওর বিষয়", "Topic Title")) },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                OutlinedTextField(
                                    value = details,
                                    onValueChange = { details = it },
                                    label = { Text(tr(lang, "কী কী জানতে চান?", "What should it cover?")) },
                                    minLines = 3,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                OutlinedButton(
                                    onClick = {
                                        if (!isSignedIn) {
                                            onRequireSignIn()
                                        } else if (topic.trim().length >= 2 && details.trim().length >= 2) {
                                            onSubmitVideoRequest(topic, details) { ok ->
                                                if (ok) { topic = ""; details = "" }
                                            }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().testTag("community_request_submit")
                                ) {
                                    Text(tr(lang, "রিকোয়েস্ট পাঠান", "Send Video Request"))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
