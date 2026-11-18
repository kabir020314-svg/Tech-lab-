package com.example.ui.screens

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Comment
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.Fullscreen
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.ThumbUp
import androidx.compose.material.icons.rounded.ThumbUpOffAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.VideoItem
import com.example.data.model.VideoResourceItem
import com.example.ui.components.VideoRowCard
import com.example.ui.components.formatCount
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.AppStrings
import com.example.ui.i18n.tr

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoPlayerScreen(
    lang: AppLanguage,
    video: VideoItem,
    allVideos: List<VideoItem>,
    resources: List<VideoResourceItem>,
    isLiked: Boolean,
    isSaved: Boolean,
    dataSaver: Boolean,
    onBack: () -> Unit,
    onRecordWatch: (VideoItem) -> Unit,
    onToggleLike: (VideoItem) -> Unit,
    onToggleSave: (VideoItem) -> Unit,
    onAddNote: (videoId: String, title: String, content: String, timestamp: String?) -> Unit,
    onSaveResourceOffline: (VideoResourceItem) -> Unit,
    onOpenVideo: (String) -> Unit
) {
    val context = LocalContext.current
    var isFullScreen by remember { mutableStateOf(false) }
    var showNoteDialog by remember { mutableStateOf(false) }
    var expandDescription by remember { mutableStateOf(false) }

    LaunchedEffect(video.id) {
        onRecordWatch(video)
    }

    val attachedResources = resources.filter { it.videoId == video.id }
    val nextPartVideo = remember(video, allVideos) {
        if (video.playlistId != null && video.partNumber != null) {
            allVideos.firstOrNull {
                it.playlistId == video.playlistId && it.partNumber == video.partNumber + 1
            }
        } else null
    }
    val relatedVideos = allVideos.filter { it.id != video.id }.take(4)

    if (isFullScreen) {
        Dialog(
            onDismissRequest = { isFullScreen = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                YouTubeEmbedWebView(
                    videoId = video.id,
                    modifier = Modifier.fillMaxSize()
                )
                IconButton(
                    onClick = { isFullScreen = false },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                ) {
                    Icon(Icons.Rounded.Close, contentDescription = "Exit Fullscreen", tint = Color.White)
                }
            }
        }
    }

    if (showNoteDialog) {
        var noteText by remember { mutableStateOf("") }
        var timestampText by remember { mutableStateOf("02:15") }
        AlertDialog(
            onDismissRequest = { showNoteDialog = false },
            title = { Text(AppStrings.addNote(lang)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = timestampText,
                        onValueChange = { timestampText = it },
                        label = { Text(tr(lang, "সময় (ঐচ্ছিক)", "Timestamp (optional)")) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = noteText,
                        onValueChange = { noteText = it },
                        label = { Text(tr(lang, "আপনার নোট লিখুন", "Write your note")) },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth().testTag("player_note_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (noteText.isNotBlank()) {
                            onAddNote(video.id, video.titleBn, noteText, timestampText)
                            showNoteDialog = false
                        }
                    },
                    modifier = Modifier.testTag("player_note_save_btn")
                ) {
                    Text(AppStrings.save(lang))
                }
            },
            dismissButton = {
                TextButton(onClick = { showNoteDialog = false }) {
                    Text(tr(lang, "বাতিল", "Cancel"))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(tr(lang, "ভিডিও প্লেয়ার", "Video Player")) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { isFullScreen = true }, modifier = Modifier.testTag("player_fullscreen_btn")) {
                        Icon(Icons.Rounded.Fullscreen, contentDescription = "Full Screen")
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
            // In-App YouTube Player + Direct YouTube Fallback
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(215.dp)
                        .background(Color.Black, RoundedCornerShape(18.dp))
                ) {
                    YouTubeEmbedWebView(
                        videoId = video.id,
                        modifier = Modifier.fillMaxSize()
                    )
                    Surface(
                        color = Color.Black.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp)
                            .clickable {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=${video.id}"))
                                context.startActivity(intent)
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Rounded.OpenInNew, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("YouTube", style = MaterialTheme.typography.labelSmall, color = Color.White)
                        }
                    }
                }
            }

            // Title & Stats
            item {
                Text(
                    text = if (lang == AppLanguage.BANGLA) video.titleBn else video.titleEn,
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "${formatCount(video.viewCount)} views • ${video.publishedAtLabel} • ${video.durationLabel}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Action Buttons: Like, Save, Share, Add Note, Comments
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    PlayerActionChip(
                        icon = if (isLiked) Icons.Rounded.ThumbUp else Icons.Rounded.ThumbUpOffAlt,
                        label = "${AppStrings.like(lang)} (${formatCount(video.likeCount + if (isLiked) 1 else 0)})",
                        active = isLiked,
                        onClick = { onToggleLike(video) }
                    )
                    PlayerActionChip(
                        icon = if (isSaved) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,
                        label = if (isSaved) AppStrings.saved(lang) else AppStrings.save(lang),
                        active = isSaved,
                        onClick = { onToggleSave(video) }
                    )
                    PlayerActionChip(
                        icon = Icons.Rounded.EditNote,
                        label = AppStrings.addNote(lang),
                        active = false,
                        onClick = { showNoteDialog = true }
                    )
                    PlayerActionChip(
                        icon = Icons.Rounded.Share,
                        label = AppStrings.share(lang),
                        active = false,
                        onClick = {
                            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "${video.titleBn}\nhttps://www.youtube.com/watch?v=${video.id}"
                                )
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share Video"))
                        }
                    )
                }
            }

            // Next Part Auto-Suggest (for Series)
            if (nextPartVideo != null) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenVideo(nextPartVideo.id) }
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = tr(lang, "পরবর্তী পর্ব সাজেস্ট করা হচ্ছে", "Next Part Auto-Suggest"),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = if (lang == AppLanguage.BANGLA) nextPartVideo.titleBn else nextPartVideo.titleEn,
                                    style = MaterialTheme.typography.titleSmall
                                )
                            }
                            Button(onClick = { onOpenVideo(nextPartVideo.id) }) {
                                Text(tr(lang, "দেখুন", "Play"))
                            }
                        }
                    }
                }
            }

            // Description + Comments Link
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = if (lang == AppLanguage.BANGLA) video.descriptionBn else video.descriptionEn,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = if (expandDescription) 20 else 2
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            TextButton(onClick = { expandDescription = !expandDescription }) {
                                Text(if (expandDescription) tr(lang, "কম দেখান", "Show less") else tr(lang, "বিস্তারিত দেখুন", "Read more"))
                            }
                            TextButton(onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=${video.id}#comments"))
                                context.startActivity(intent)
                            }) {
                                Icon(Icons.AutoMirrored.Rounded.Comment, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(tr(lang, "কমেন্টস দেখুন", "Comments"))
                            }
                        }
                    }
                }
            }

            // Attached Resources
            if (attachedResources.isNotEmpty()) {
                item {
                    Text(
                        text = tr(lang, "এই ভিডিওর রিসোর্স ও কোড", "Video Resources & Code"),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                items(attachedResources, key = { it.id }) { res ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Rounded.Description, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (lang == AppLanguage.BANGLA) res.titleBn else res.titleEn,
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    text = "${res.resourceType.label} • ${res.snippetOrNote}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            OutlinedButton(onClick = { onSaveResourceOffline(res) }) {
                                Text(tr(lang, "অফলাইন সেভ", "Save"))
                            }
                        }
                    }
                }
            }

            // Related Videos
            item {
                Text(
                    text = tr(lang, "সম্পর্কিত ভিডিও", "Related Videos"),
                    style = MaterialTheme.typography.titleMedium
                )
            }
            items(relatedVideos, key = { it.id }) { rel ->
                VideoRowCard(
                    video = rel,
                    lang = lang,
                    isSaved = false,
                    dataSaver = dataSaver,
                    onClick = { onOpenVideo(rel.id) },
                    onToggleSave = { onToggleSave(rel) }
                )
            }
        }
    }
}

@Composable
private fun PlayerActionChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    active: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun YouTubeEmbedWebView(videoId: String, modifier: Modifier = Modifier) {
    val html = remember(videoId) {
        """
        <html><body style="margin:0;padding:0;background:#000;">
        <iframe width="100%" height="100%"
          src="https://www.youtube.com/embed/$videoId?playsinline=1&rel=0"
          frameborder="0" allowfullscreen></iframe>
        </body></html>
        """.trimIndent()
    }
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                webViewClient = WebViewClient()
                webChromeClient = WebChromeClient()
                loadDataWithBaseURL("https://www.youtube.com", html, "text/html", "utf-8", null)
            }
        },
        update = { webView ->
            webView.loadDataWithBaseURL("https://www.youtube.com", html, "text/html", "utf-8", null)
        },
        modifier = modifier
    )
}
