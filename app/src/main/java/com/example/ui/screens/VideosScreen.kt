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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ViewList
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.VideoLibrary
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.model.AppCategory
import com.example.data.model.VideoDurationFilter
import com.example.data.model.VideoItem
import com.example.data.model.VideoSortOption
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.SkeletonVideoList
import com.example.ui.components.VideoGridCard
import com.example.ui.components.VideoRowCard
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.AppStrings
import com.example.ui.i18n.tr

@Composable
fun VideosScreen(
    lang: AppLanguage,
    videos: List<VideoItem>,
    totalAvailableCount: Int,
    savedIds: Set<String>,
    selectedCategory: AppCategory,
    sortOption: VideoSortOption,
    durationFilter: VideoDurationFilter,
    isGridMode: Boolean,
    isLoading: Boolean,
    dataSaver: Boolean,
    onSelectCategory: (AppCategory) -> Unit,
    onSelectSort: (VideoSortOption) -> Unit,
    onSelectDuration: (VideoDurationFilter) -> Unit,
    onToggleGridMode: () -> Unit,
    onLoadMore: () -> Unit,
    onOpenVideo: (String) -> Unit,
    onToggleSave: (VideoItem) -> Unit
) {
    val listState = rememberLazyListState()
    var showSortMenu by remember { mutableStateOf(false) }
    var showDurationMenu by remember { mutableStateOf(false) }

    val reachedBottom by remember {
        derivedStateOf {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisible >= (listState.layoutInfo.totalItemsCount - 2).coerceAtLeast(1)
        }
    }

    LaunchedEffect(reachedBottom) {
        if (reachedBottom && videos.size < totalAvailableCount) {
            onLoadMore()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = tr(lang, "সকল ভিডিও (${videos.size})", "All Videos (${videos.size})"),
                style = MaterialTheme.typography.headlineSmall
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Sort dropdown
                FilterChip(
                    selected = sortOption == VideoSortOption.MOST_VIEWED,
                    onClick = { showSortMenu = true },
                    label = { Text(if (lang == AppLanguage.BANGLA) sortOption.bnLabel else sortOption.enLabel) }
                )
                DropdownMenu(expanded = showSortMenu, onDismissRequest = { showSortMenu = false }) {
                    VideoSortOption.entries.forEach { opt ->
                        DropdownMenuItem(
                            text = { Text(if (lang == AppLanguage.BANGLA) opt.bnLabel else opt.enLabel) },
                            onClick = { onSelectSort(opt); showSortMenu = false }
                        )
                    }
                }
                IconButton(onClick = onToggleGridMode, modifier = Modifier.testTag("videos_grid_toggle")) {
                    Icon(
                        imageVector = if (isGridMode) Icons.AutoMirrored.Rounded.ViewList else Icons.Rounded.GridView,
                        contentDescription = tr(lang, "গ্রিড/লিস্ট", "Toggle grid or list")
                    )
                }
            }
        }

        // Category + Duration filter bar
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(AppCategory.entries) { cat ->
                FilterChip(
                    selected = selectedCategory == cat,
                    onClick = { onSelectCategory(cat) },
                    label = { Text(if (lang == AppLanguage.BANGLA) cat.bnLabel else cat.enLabel) }
                )
            }
            item {
                FilterChip(
                    selected = durationFilter != VideoDurationFilter.ALL,
                    onClick = { showDurationMenu = true },
                    label = { Text(if (lang == AppLanguage.BANGLA) durationFilter.bnLabel else durationFilter.enLabel) }
                )
                DropdownMenu(expanded = showDurationMenu, onDismissRequest = { showDurationMenu = false }) {
                    VideoDurationFilter.entries.forEach { dur ->
                        DropdownMenuItem(
                            text = { Text(if (lang == AppLanguage.BANGLA) dur.bnLabel else dur.enLabel) },
                            onClick = { onSelectDuration(dur); showDurationMenu = false }
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (isLoading && videos.isEmpty()) {
                item { SkeletonVideoList(4) }
            } else if (videos.isEmpty()) {
                item {
                    EmptyStateCard(
                        icon = Icons.Rounded.VideoLibrary,
                        title = tr(lang, "কোনো ভিডিও পাওয়া যায়নি", "No matching videos"),
                        subtitle = tr(lang, "ফিল্টার পরিবর্তন করে আবার দেখুন।", "Try clearing category or duration filters."),
                        actionLabel = tr(lang, "ফিল্টার রিসেট", "Reset Filters"),
                        onAction = {
                            onSelectCategory(AppCategory.ALL)
                            onSelectDuration(VideoDurationFilter.ALL)
                        }
                    )
                }
            } else if (isGridMode) {
                items(videos.chunked(2)) { pair ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        pair.forEach { vid ->
                            VideoGridCard(
                                video = vid,
                                lang = lang,
                                isSaved = savedIds.contains(vid.id),
                                dataSaver = dataSaver,
                                onClick = { onOpenVideo(vid.id) },
                                onToggleSave = { onToggleSave(vid) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (pair.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            } else {
                items(videos, key = { it.id }) { vid ->
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

            if (videos.size < totalAvailableCount) {
                item {
                    OutlinedButton(
                        onClick = onLoadMore,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(AppStrings.loadMore(lang))
                    }
                }
            }
        }
    }
}
