package com.example.ui.screens

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.model.VideoItem
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.VideoRowCard
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.tr
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(
    lang: AppLanguage,
    allVideos: List<VideoItem>,
    savedIds: Set<String>,
    recentSearches: List<String>,
    dataSaver: Boolean,
    onBack: () -> Unit,
    onSearchSubmitted: (String) -> Unit,
    onClearRecent: () -> Unit,
    onOpenVideo: (String) -> Unit,
    onToggleSave: (VideoItem) -> Unit
) {
    var query by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val voiceLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spoken = result.data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                ?.firstOrNull()
            if (!spoken.isNullOrBlank()) {
                query = spoken
                onSearchSubmitted(spoken)
            }
        }
    }

    val suggestions = remember(lang) {
        if (lang == AppLanguage.BANGLA) {
            listOf("Jetpack Compose", "বাংলা গল্প", "AI ট্রিক", "Coroutines", "Shorts")
        } else {
            listOf("Jetpack Compose", "Dhaka Coder", "AI Tricks", "Coroutines", "Shorts")
        }
    }

    val results = remember(query, allVideos) {
        if (query.isBlank()) emptyList()
        else {
            val q = query.trim().lowercase()
            allVideos.filter {
                it.titleBn.lowercase().contains(q) ||
                    it.titleEn.lowercase().contains(q) ||
                    it.category.lowercase().contains(q) ||
                    it.descriptionBn.lowercase().contains(q) ||
                    it.descriptionEn.lowercase().contains(q)
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(tr(lang, "ভিডিও খুঁজুন", "Search Kabir Tech Lab")) },
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
            item {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text(tr(lang, "টিউটোরিয়াল, গল্প বা টপিক লিখুন...", "Search tutorials, stories, topics...")) },
                    leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                    trailingIcon = {
                        Row {
                            if (query.isNotEmpty()) {
                                IconButton(onClick = { query = "" }) {
                                    Icon(Icons.Rounded.Clear, contentDescription = "Clear")
                                }
                            }
                            IconButton(
                                onClick = {
                                    try {
                                        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                            putExtra(RecognizerIntent.EXTRA_LANGUAGE, if (lang == AppLanguage.BANGLA) "bn-BD" else "en-US")
                                        }
                                        voiceLauncher.launch(intent)
                                    } catch (_: Exception) {
                                        scope.launch {
                                            snackbarHostState.showSnackbar(
                                                tr(lang, "এই ডিভাইসে ভয়েস সার্চ পাওয়া যায়নি", "Voice search is unavailable on this device")
                                            )
                                        }
                                    }
                                },
                                modifier = Modifier.testTag("search_voice_btn")
                            ) {
                                Icon(Icons.Rounded.Mic, contentDescription = tr(lang, "ভয়েস সার্চ", "Voice Search"))
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().testTag("search_input")
                )
            }

            if (query.isBlank()) {
                item {
                    Text(tr(lang, "সাজেস্টেড টপিক", "Suggested Searches"), style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(8.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        suggestions.forEach { s ->
                            AssistChip(
                                onClick = {
                                    query = s
                                    onSearchSubmitted(s)
                                },
                                label = { Text(s) }
                            )
                        }
                    }
                }

                if (recentSearches.isNotEmpty()) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(tr(lang, "সাম্প্রতিক সার্চ", "Recent Searches"), style = MaterialTheme.typography.titleSmall)
                            TextButton(onClick = onClearRecent) {
                                Text(tr(lang, "মুছুন", "Clear"))
                            }
                        }
                    }
                    items(recentSearches) { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { query = item }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Rounded.History, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.width(12.dp))
                            Text(item, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            } else if (results.isEmpty()) {
                item {
                    EmptyStateCard(
                        icon = Icons.Rounded.Search,
                        title = tr(lang, "কোনো ফলাফল পাওয়া যায়নি", "No results for \"$query\""),
                        subtitle = tr(lang, "অন্য কীওয়ার্ড দিয়ে চেষ্টা করুন।", "Try searching another topic or category.")
                    )
                }
            } else {
                items(results, key = { it.id }) { vid ->
                    VideoRowCard(
                        video = vid,
                        lang = lang,
                        isSaved = savedIds.contains(vid.id),
                        dataSaver = dataSaver,
                        onClick = {
                            onSearchSubmitted(query)
                            onOpenVideo(vid.id)
                        },
                        onToggleSave = { onToggleSave(vid) }
                    )
                }
            }
        }
    }
}
