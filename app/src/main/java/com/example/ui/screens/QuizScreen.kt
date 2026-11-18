package com.example.ui.screens

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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.LeaderboardEntryDoc
import com.example.data.model.QuizQuestionItem
import com.example.data.remote.SampleChannelData
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.tr
import com.example.ui.theme.AmberHighlight
import com.example.ui.theme.EmeraldSuccess

@Composable
fun QuizScreen(
    lang: AppLanguage,
    questions: List<QuizQuestionItem>,
    xpPoints: Int,
    streakDays: Int,
    answeredQuizIds: Set<String>,
    leaderboard: List<LeaderboardEntryDoc>,
    onAnswerQuestion: (QuizQuestionItem, Int) -> Unit,
    onDailyCheckIn: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var currentIndex by remember { mutableIntStateOf(0) }
    var selectedOption by remember { mutableStateOf<Int?>(null) }

    val safeQuestions = if (questions.isEmpty()) SampleChannelData.quizzes else questions
    val currentQuestion = safeQuestions[currentIndex % safeQuestions.size]
    val alreadyAnswered = answeredQuizIds.contains(currentQuestion.id) || selectedOption != null

    Column(modifier = Modifier.fillMaxSize()) {
        // XP + Streak Header
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Bolt, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(6.dp))
                    Column {
                        Text("$xpPoints XP", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(tr(lang, "মোট পয়েন্ট", "Total XP"), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.LocalFireDepartment, contentDescription = null, tint = AmberHighlight)
                    Spacer(Modifier.width(4.dp))
                    Column {
                        Text("$streakDays ${tr(lang, "দিন", "Days")}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(tr(lang, "স্ট্রিক", "Streak"), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Button(onClick = onDailyCheckIn, modifier = Modifier.testTag("quiz_checkin_btn")) {
                    Text(tr(lang, "চেক-ইন", "Check-in"))
                }
            }
        }

        TabRow(selectedTabIndex = selectedTab) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text(tr(lang, "ডেইলি কুইজ", "Daily Quiz")) })
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text(tr(lang, "ব্যাজ", "Badges")) })
            Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text(tr(lang, "লিডারবোর্ড", "Leaderboard")) })
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when (selectedTab) {
                0 -> {
                    item {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = tr(lang, "প্রশ্ন ${(currentIndex % safeQuestions.size) + 1} / ${safeQuestions.size}", "Question ${(currentIndex % safeQuestions.size) + 1} / ${safeQuestions.size}"),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "+${currentQuestion.xpReward} XP",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = AmberHighlight
                                    )
                                }
                                Text(
                                    text = if (lang == AppLanguage.BANGLA) currentQuestion.questionBn else currentQuestion.questionEn,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                val opts = if (lang == AppLanguage.BANGLA) currentQuestion.optionsBn else currentQuestion.optionsEn
                                opts.forEachIndexed { idx, optText ->
                                    val isSelected = selectedOption == idx
                                    val isCorrect = idx == currentQuestion.correctIndex
                                    val borderColor = when {
                                        !alreadyAnswered -> MaterialTheme.colorScheme.outline
                                        isCorrect -> EmeraldSuccess
                                        isSelected -> MaterialTheme.colorScheme.error
                                        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = if (alreadyAnswered && isCorrect) EmeraldSuccess.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surface,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .border(1.5.dp, borderColor, RoundedCornerShape(14.dp))
                                            .clickable(enabled = !alreadyAnswered) {
                                                selectedOption = idx
                                                onAnswerQuestion(currentQuestion, idx)
                                            }
                                            .testTag("quiz_option_$idx")
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(14.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(optText, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                                            if (alreadyAnswered && isCorrect) {
                                                Icon(Icons.Rounded.CheckCircle, contentDescription = "Correct", tint = EmeraldSuccess)
                                            }
                                        }
                                    }
                                }

                                if (alreadyAnswered) {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text(
                                            text = if (lang == AppLanguage.BANGLA) currentQuestion.explanationBn else currentQuestion.explanationEn,
                                            style = MaterialTheme.typography.bodySmall,
                                            modifier = Modifier.padding(12.dp)
                                        )
                                    }
                                    Button(
                                        onClick = {
                                            selectedOption = null
                                            currentIndex = (currentIndex + 1) % safeQuestions.size
                                        },
                                        modifier = Modifier.fillMaxWidth().testTag("quiz_next_btn")
                                    ) {
                                        Text(tr(lang, "পরবর্তী প্রশ্ন", "Next Question"))
                                    }
                                }
                            }
                        }
                    }
                }

                1 -> {
                    itemsIndexed(SampleChannelData.badges) { _, badge ->
                        val unlocked = xpPoints >= badge.requiredXp
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (unlocked) MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        ) {
                            Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(if (unlocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Rounded.WorkspacePremium,
                                        contentDescription = null,
                                        tint = if (unlocked) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (lang == AppLanguage.BANGLA) badge.titleBn else badge.titleEn,
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                    Text(
                                        text = if (lang == AppLanguage.BANGLA) badge.descriptionBn else badge.descriptionEn,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = if (unlocked) tr(lang, "অর্জিত", "Unlocked") else "${badge.requiredXp} XP",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (unlocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                else -> {
                    itemsIndexed(leaderboard, key = { _, e -> e.id.ifBlank { e.userId } }) { idx, entry ->
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f))
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "#${idx + 1}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (idx == 0) AmberHighlight else MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(Modifier.width(14.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(entry.displayName, style = MaterialTheme.typography.titleSmall)
                                    Text(
                                        text = "${entry.streakDays} ${tr(lang, "দিনের স্ট্রিক", "day streak")} • ${entry.badgesCount} ${tr(lang, "ব্যাজ", "badges")}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Icon(Icons.Rounded.EmojiEvents, contentDescription = null, tint = AmberHighlight, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("${entry.xpPoints} XP", style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }
                }
            }
        }
    }
}
