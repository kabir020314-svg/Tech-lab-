package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.data.model.ResourceType
import com.example.data.model.VideoItem
import com.example.data.model.VideoResourceItem
import com.example.data.remote.SampleChannelData
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.tr

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResourcesAndToolsScreen(
    lang: AppLanguage,
    videos: List<VideoItem>,
    resources: List<VideoResourceItem>,
    onBack: () -> Unit,
    onSaveOffline: (VideoResourceItem) -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedToolTag by remember { mutableStateOf("All") }

    val tools = SampleChannelData.toolsDirectory
    val toolTags = listOf("All", "AI", "IDE", "Backend", "Bangla", "Design")
    val filteredTools = if (selectedToolTag == "All") tools else tools.filter { it.tag == selectedToolTag }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(tr(lang, "টিউটোরিয়াল রিসোর্স ও টুলস", "Tutorials, Resources & Tools")) },
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
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text(tr(lang, "ভিডিও রিসোর্স", "Video Resources")) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text(tr(lang, "টুলস ও AI ডিরেক্টরি", "Tools & Links")) }
                )
            }

            if (selectedTab == 0) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(resources, key = { it.id }) { res ->
                        val linkedVideo = videos.firstOrNull { it.id == res.videoId }
                        val icon = when (res.resourceType) {
                            ResourceType.PDF -> Icons.Rounded.PictureAsPdf
                            ResourceType.CODE -> Icons.Rounded.Code
                            ResourceType.TOOL -> Icons.Rounded.Build
                            ResourceType.LINK -> Icons.Rounded.Link
                        }
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.78f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = if (lang == AppLanguage.BANGLA) res.titleBn else res.titleEn,
                                            style = MaterialTheme.typography.titleSmall
                                        )
                                        if (linkedVideo != null) {
                                            Text(
                                                text = if (lang == AppLanguage.BANGLA) linkedVideo.titleBn else linkedVideo.titleEn,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                    Surface(
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = res.resourceType.label,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                                if (res.snippetOrNote.isNotBlank()) {
                                    Spacer(Modifier.height(6.dp))
                                    Text(
                                        text = res.snippetOrNote,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(Modifier.height(10.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(onClick = { onSaveOffline(res) }) {
                                        Icon(Icons.Rounded.Download, contentDescription = null, modifier = Modifier.height(16.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text(tr(lang, "অফলাইন সেভ", "Save Offline"))
                                    }
                                    OutlinedButton(onClick = {
                                        runCatching {
                                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(res.url)))
                                        }
                                    }) {
                                        Icon(Icons.Rounded.OpenInNew, contentDescription = null, modifier = Modifier.height(16.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text(tr(lang, "লিংক খুলুন", "Open"))
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(toolTags) { tag ->
                                FilterChip(
                                    selected = selectedToolTag == tag,
                                    onClick = { selectedToolTag = tag },
                                    label = { Text(tag) }
                                )
                            }
                        }
                    }
                    items(filteredTools, key = { it.id }) { tool ->
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.78f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(tool.name, style = MaterialTheme.typography.titleMedium)
                                    Text(
                                        text = if (lang == AppLanguage.BANGLA) tool.categoryBn else tool.categoryEn,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = if (lang == AppLanguage.BANGLA) tool.descriptionBn else tool.descriptionEn,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                IconButton(onClick = {
                                    runCatching {
                                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(tool.url)))
                                    }
                                }) {
                                    Icon(Icons.Rounded.OpenInNew, contentDescription = "Open ${tool.name}")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
