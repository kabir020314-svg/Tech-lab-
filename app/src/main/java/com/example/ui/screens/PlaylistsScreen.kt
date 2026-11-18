package com.example.ui.screens

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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.PlaylistPlay
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.data.local.LocalHistoryEntity
import com.example.data.model.PlaylistItem
import com.example.data.model.VideoItem
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.PlaylistProgressCard
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.tr

@Composable
fun PlaylistsScreen(
    lang: AppLanguage,
    playlists: List<PlaylistItem>,
    history: List<LocalHistoryEntity>,
    onOpenPlaylist: (String) -> Unit
) {
    val watchedIds = history.map { it.videoId }.toSet()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = tr(lang, "প্লেলিস্ট ও গল্পের সিরিজ", "Playlists & Story Series"),
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = tr(
                    lang,
                    "ধারাবাহিক টিউটোরিয়াল ও বাংলা গল্প পর্ব অনুযায়ী দেখুন",
                    "Follow structured courses and multi-part Bangla story series"
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (playlists.isEmpty()) {
            item {
                EmptyStateCard(
                    icon = Icons.Rounded.PlaylistPlay,
                    title = tr(lang, "কোনো প্লেলিস্ট নেই", "No playlists yet"),
                    subtitle = tr(lang, "শীঘ্রই নতুন সিরিজ যোগ করা হবে।", "New series will appear here soon.")
                )
            }
        } else {
            items(playlists, key = { it.id }) { playlist ->
                val watchedCount = playlist.videoIds.count { watchedIds.contains(it) }
                PlaylistProgressCard(
                    playlist = playlist,
                    watchedCount = watchedCount,
                    lang = lang,
                    onClick = { onOpenPlaylist(playlist.id) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistDetailScreen(
    lang: AppLanguage,
    playlist: PlaylistItem,
    allVideos: List<VideoItem>,
    history: List<LocalHistoryEntity>,
    onBack: () -> Unit,
    onOpenVideo: (String) -> Unit
) {
    val watchedIds = history.map { it.videoId }.toSet()
    val episodeVideos = allVideos.filter {
        playlist.videoIds.contains(it.id) || it.playlistId == playlist.id
    }.sortedBy { it.partNumber ?: 99 }

    val nextUnwatched = episodeVideos.firstOrNull { !watchedIds.contains(it.id) } ?: episodeVideos.firstOrNull()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (lang == AppLanguage.BANGLA) playlist.titleBn else playlist.titleEn)
                },
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
            if (nextUnwatched != null) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = tr(lang, "পরবর্তী পর্ব সাজেস্ট", "Auto-Suggest Next Part"),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = if (lang == AppLanguage.BANGLA) nextUnwatched.titleBn else nextUnwatched.titleEn,
                                    style = MaterialTheme.typography.titleSmall
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                            Button(onClick = { onOpenVideo(nextUnwatched.id) }) {
                                Text(tr(lang, "চালান", "Play"))
                            }
                        }
                    }
                }
            }

            itemsIndexed(episodeVideos, key = { _, v -> v.id }) { index, video ->
                val isWatched = watchedIds.contains(video.id)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenVideo(video.id) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = tr(lang, "পর্ব ${index + 1}", "Part ${index + 1}"),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (lang == AppLanguage.BANGLA) video.titleBn else video.titleEn,
                                style = MaterialTheme.typography.titleSmall
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = video.durationLabel,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            imageVector = if (isWatched) Icons.Rounded.CheckCircle else Icons.Rounded.PlayCircle,
                            contentDescription = null,
                            tint = if (isWatched) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
