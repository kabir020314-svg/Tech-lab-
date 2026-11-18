package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.Campaign
import androidx.compose.material.icons.rounded.FolderSpecial
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.LocalHistoryEntity
import com.example.data.model.AnnouncementDoc
import com.example.data.model.AppCategory
import com.example.data.model.VideoItem
import com.example.data.remote.SampleChannelData
import com.example.ui.components.FriendlyErrorBanner
import com.example.ui.components.KabirLabLogoBadge
import com.example.ui.components.SkeletonVideoList
import com.example.ui.components.VideoRowCard
import com.example.ui.components.VideoThumbnailImage
import com.example.ui.components.formatCount
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.AppStrings
import com.example.ui.i18n.tr

@Composable
fun HomeScreen(
    lang: AppLanguage,
    videos: List<VideoItem>,
    history: List<LocalHistoryEntity>,
    savedIds: Set<String>,
    selectedCategory: AppCategory,
    announcements: List<AnnouncementDoc>,
    isLoading: Boolean,
    errorMessage: String?,
    dataSaver: Boolean,
    subscriberCount: Long,
    onToggleLanguage: () -> Unit,
    onSelectCategory: (AppCategory) -> Unit,
    onOpenVideo: (String) -> Unit,
    onToggleSave: (VideoItem) -> Unit,
    onOpenSearch: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenLibrary: () -> Unit,
    onOpenResources: () -> Unit,
    onRetry: () -> Unit
) {
    val latestVideo = videos.firstOrNull()
    val filteredTrending = videos.filter {
        selectedCategory == AppCategory.ALL || it.category.equals(selectedCategory.key, true)
    }
    val featuredStory = videos.firstOrNull { it.category == "Stories" } ?: latestVideo
    val activeAnnouncement = announcements.firstOrNull { it.active }
    val tip = SampleChannelData.todaysTechTip

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header with Logo, Language toggle, Search, Notifications, Profile
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                KabirLabLogoBadge(modifier = Modifier.size(42.dp))
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = AppStrings.appTitle(lang),
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${formatCount(subscriberCount)} ${tr(lang, "সাবস্ক্রাইবার", "subscribers")}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .clickable(onClick = onToggleLanguage)
                        .testTag("home_lang_toggle")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Rounded.Translate, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(lang.label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    }
                }
                IconButton(onClick = onOpenSearch, modifier = Modifier.testTag("home_search_btn")) {
                    Icon(Icons.Rounded.Search, contentDescription = tr(lang, "খুঁজুন", "Search"))
                }
                IconButton(onClick = onOpenNotifications, modifier = Modifier.testTag("home_notif_btn")) {
                    Icon(Icons.Rounded.Notifications, contentDescription = tr(lang, "নোটিফিকেশন", "Notifications"))
                }
                IconButton(onClick = onOpenProfile, modifier = Modifier.testTag("home_profile_btn")) {
                    Icon(Icons.Rounded.Person, contentDescription = tr(lang, "প্রোফাইল", "Profile"))
                }
            }
        }

        // Announcement Banner
        if (activeAnnouncement != null) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Rounded.Campaign, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (lang == AppLanguage.BANGLA) activeAnnouncement.titleBn else activeAnnouncement.titleEn,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = if (lang == AppLanguage.BANGLA) activeAnnouncement.bodyBn else activeAnnouncement.bodyEn,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }

        // Error Banner
        if (errorMessage != null) {
            item {
                FriendlyErrorBanner(message = errorMessage, lang = lang, onRetry = onRetry)
            }
        }

        // Hero Banner (Latest Video)
        if (latestVideo != null) {
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenVideo(latestVideo.id) }
                        .testTag("home_hero_card")
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_hero_channel_1795032434952),
                            contentDescription = latestVideo.titleEn,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color.Transparent, Color(0xCC040D1A), Color(0xF2040D1A))
                                    )
                                )
                        )
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(16.dp)
                        ) {
                            Surface(
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = tr(lang, "সর্বশেষ ভিডিও • ${latestVideo.durationLabel}", "LATEST • ${latestVideo.durationLabel}"),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = if (lang == AppLanguage.BANGLA) latestVideo.titleBn else latestVideo.titleEn,
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(Modifier.height(10.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Button(
                                    onClick = { onOpenVideo(latestVideo.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Icon(Icons.Rounded.PlayArrow, contentDescription = null)
                                    Spacer(Modifier.width(4.dp))
                                    Text(AppStrings.watchNow(lang))
                                }
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    text = "${formatCount(latestVideo.viewCount)} views",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Quick Shortcut Bar (Library + Tutorials & Resources)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = onOpenLibrary)
                        .testTag("home_library_shortcut"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Rounded.FolderSpecial, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(tr(lang, "লাইব্রেরি", "My Library"), style = MaterialTheme.typography.titleSmall)
                            Text(tr(lang, "সেভ, হিস্ট্রি ও নোট", "Saved & Notes"), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = onOpenResources)
                        .testTag("home_resources_shortcut"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(tr(lang, "রিসোর্স ও টুলস", "Tools & PDF"), style = MaterialTheme.typography.titleSmall)
                            Text(tr(lang, "কোড ও AI ডিরেক্টরি", "Code & AI Links"), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        // Continue Watching row
        if (history.isNotEmpty()) {
            item {
                Text(
                    text = AppStrings.continueWatching(lang),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(history, key = { it.videoId }) { item ->
                        val matchedVideo = videos.firstOrNull { it.id == item.videoId }
                        Card(
                            modifier = Modifier
                                .width(210.dp)
                                .clickable { onOpenVideo(item.videoId) },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column {
                                if (matchedVideo != null) {
                                    VideoThumbnailImage(
                                        video = matchedVideo,
                                        dataSaver = dataSaver,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(104.dp)
                                    )
                                }
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = if (matchedVideo != null) {
                                            if (lang == AppLanguage.BANGLA) matchedVideo.titleBn else matchedVideo.titleEn
                                        } else item.title,
                                        style = MaterialTheme.typography.labelLarge,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(Modifier.height(6.dp))
                                    LinearProgressIndicator(
                                        progress = { item.progressPercent / 100f },
                                        modifier = Modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(3.dp))
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Category Filter Chips
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(AppCategory.entries) { cat ->
                    val selected = selectedCategory == cat
                    FilterChip(
                        selected = selected,
                        onClick = { onSelectCategory(cat) },
                        label = { Text(if (lang == AppLanguage.BANGLA) cat.bnLabel else cat.enLabel) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.22f),
                            selectedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }
        }

        // Today's Tech Tip Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Box(modifier = Modifier.fillMaxWidth().height(92.dp)) {
                        Image(
                            painter = painterResource(id = R.drawable.img_tech_tip_banner_1795032452539),
                            contentDescription = "Tech Tip",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.45f))
                                .padding(14.dp),
                            contentAlignment = Alignment.BottomStart
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.Lightbulb, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = AppStrings.todaysTechTip(lang),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White
                                )
                            }
                        }
                    }
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = if (lang == AppLanguage.BANGLA) tip.titleBn else tip.titleEn,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = if (lang == AppLanguage.BANGLA) tip.bodyBn else tip.bodyEn,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        // Featured Story Card
        if (featuredStory != null) {
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenVideo(featuredStory.id) },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f))
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.img_story_featured_1795032434219),
                            contentDescription = "Featured Bangla Story",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(96.dp)
                                .clip(RoundedCornerShape(14.dp))
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.AutoStories, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = AppStrings.featuredStory(lang),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = if (lang == AppLanguage.BANGLA) featuredStory.titleBn else featuredStory.titleEn,
                                style = MaterialTheme.typography.titleSmall,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            TextButton(
                                onClick = { onOpenVideo(featuredStory.id) },
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text(tr(lang, "পর্ব ১ থেকে শুনুন →", "Listen from Part 1 →"))
                            }
                        }
                    }
                }
            }
        }

        // Trending Videos
        item {
            Text(
                text = AppStrings.trendingVideos(lang),
                style = MaterialTheme.typography.titleMedium
            )
        }

        if (isLoading && videos.isEmpty()) {
            item { SkeletonVideoList(3) }
        } else {
            items(filteredTrending.take(5), key = { it.id }) { video ->
                VideoRowCard(
                    video = video,
                    lang = lang,
                    isSaved = savedIds.contains(video.id),
                    dataSaver = dataSaver,
                    onClick = { onOpenVideo(video.id) },
                    onToggleSave = { onToggleSave(video) }
                )
            }
        }
    }
}
