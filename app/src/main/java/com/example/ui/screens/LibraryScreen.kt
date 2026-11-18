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
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.DownloadDone
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.History
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.data.local.LocalHistoryEntity
import com.example.data.local.LocalNoteEntity
import com.example.data.local.OfflineResourceEntity
import com.example.data.model.VideoItem
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.VideoRowCard
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.tr

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    lang: AppLanguage,
    allVideos: List<VideoItem>,
    savedIds: Set<String>,
    history: List<LocalHistoryEntity>,
    notes: List<LocalNoteEntity>,
    offlineResources: List<OfflineResourceEntity>,
    dataSaver: Boolean,
    onBack: () -> Unit,
    onOpenVideo: (String) -> Unit,
    onToggleSave: (VideoItem) -> Unit,
    onDeleteNote: (String) -> Unit,
    onRemoveOfflineResource: (String) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf(
        tr(lang, "সেভ করা", "Saved"),
        tr(lang, "হিস্ট্রি", "History"),
        tr(lang, "ডাউনলোড", "Offline"),
        tr(lang, "নোটস", "Notes")
    )

    val savedVideos = allVideos.filter { savedIds.contains(it.id) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(tr(lang, "আমার লাইব্রেরি", "My Library")) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { idx, label ->
                    Tab(
                        selected = selectedTab == idx,
                        onClick = { selectedTab = idx },
                        text = { Text(label) }
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when (selectedTab) {
                    0 -> {
                        if (savedVideos.isEmpty()) {
                            item {
                                EmptyStateCard(
                                    icon = Icons.Rounded.BookmarkBorder,
                                    title = tr(lang, "কোনো সেভ করা ভিডিও নেই", "No saved videos yet"),
                                    subtitle = tr(lang, "পছন্দের ভিডিও বুকমার্ক করলে এখানে দেখতে পাবেন।", "Bookmark videos to access them quickly here.")
                                )
                            }
                        } else {
                            items(savedVideos, key = { it.id }) { vid ->
                                VideoRowCard(
                                    video = vid,
                                    lang = lang,
                                    isSaved = true,
                                    dataSaver = dataSaver,
                                    onClick = { onOpenVideo(vid.id) },
                                    onToggleSave = { onToggleSave(vid) }
                                )
                            }
                        }
                    }

                    1 -> {
                        if (history.isEmpty()) {
                            item {
                                EmptyStateCard(
                                    icon = Icons.Rounded.History,
                                    title = tr(lang, "ওয়াচ হিস্ট্রি খালি", "Watch history is empty"),
                                    subtitle = tr(lang, "যেকোনো ভিডিও দেখলে এখানে সংরক্ষিত হবে।", "Videos you watch will appear here automatically.")
                                )
                            }
                        } else {
                            items(history, key = { it.videoId }) { h ->
                                val vid = allVideos.firstOrNull { it.id == h.videoId }
                                if (vid != null) {
                                    VideoRowCard(
                                        video = vid,
                                        lang = lang,
                                        isSaved = savedIds.contains(vid.id),
                                        dataSaver = dataSaver,
                                        onClick = { onOpenVideo(vid.id) },
                                        onToggleSave = { onToggleSave(vid) }
                                    )
                                }
                            }
                        }
                    }

                    2 -> {
                        if (offlineResources.isEmpty()) {
                            item {
                                EmptyStateCard(
                                    icon = Icons.Rounded.DownloadDone,
                                    title = tr(lang, "অফলাইন রিসোর্স নেই", "No offline resources saved"),
                                    subtitle = tr(lang, "টিউটোরিয়াল রিসোর্স সেভ করলে অফলাইনেও পড়তে পারবেন।", "Save PDFs, code snippets, or checklists for offline study.")
                                )
                            }
                        } else {
                            items(offlineResources, key = { it.id }) { res ->
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = if (lang == AppLanguage.BANGLA) res.titleBn else res.titleEn,
                                                style = MaterialTheme.typography.titleSmall
                                            )
                                            Text(
                                                text = "${res.resourceType} • ${res.snippetOrNote}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        IconButton(onClick = { onRemoveOfflineResource(res.id) }) {
                                            Icon(Icons.Rounded.Delete, contentDescription = "Delete")
                                        }
                                    }
                                }
                            }
                        }
                    }

                    else -> {
                        if (notes.isEmpty()) {
                            item {
                                EmptyStateCard(
                                    icon = Icons.Rounded.EditNote,
                                    title = tr(lang, "কোনো ব্যক্তিগত নোট নেই", "No personal notes yet"),
                                    subtitle = tr(lang, "ভিডিও প্লেয়ার থেকে টাইমস্ট্যাম্পসহ নোট যোগ করুন।", "Add timestamped study notes directly from any video.")
                                )
                            }
                        } else {
                            items(notes, key = { it.id }) { note ->
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = note.videoTitle,
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            if (!note.timestampLabel.isNullOrBlank()) {
                                                Text(
                                                    text = "@ ${note.timestampLabel}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.tertiary
                                                )
                                            }
                                            Spacer(Modifier.height(4.dp))
                                            Text(text = note.content, style = MaterialTheme.typography.bodyMedium)
                                        }
                                        IconButton(onClick = { onDeleteNote(note.id) }) {
                                            Icon(Icons.Rounded.Delete, contentDescription = "Delete Note")
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
