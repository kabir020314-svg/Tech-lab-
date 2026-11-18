package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.Quiz
import androidx.compose.material.icons.rounded.Science
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.AppCategory
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.AppStrings
import com.example.ui.i18n.tr
import com.example.ui.theme.DeepMidnight
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.ElectricCyan
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AnimatedSplashScreen(
    lang: AppLanguage,
    onFinished: () -> Unit
) {
    val scale = remember { Animatable(0.65f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch { scale.animateTo(1f, tween(700, easing = FastOutSlowInEasing)) }
        launch { alpha.animateTo(1f, tween(650)) }
        delay(1500)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFF0E2C59), DeepNavy, DeepMidnight)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .scale(scale.value)
                .alpha(alpha.value)
                .padding(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(104.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF0A2463), ElectricCyan.copy(alpha = 0.28f))
                        )
                    )
                    .border(2.dp, ElectricCyan.copy(alpha = 0.7f), RoundedCornerShape(28.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Science,
                    contentDescription = "Kabir Tech Lab",
                    tint = ElectricCyan,
                    modifier = Modifier.size(56.dp)
                )
            }
            Spacer(Modifier.height(22.dp))
            Text(
                text = "Kabir Tech Lab",
                style = MaterialTheme.typography.displaySmall,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = AppStrings.tagline(lang),
                style = MaterialTheme.typography.bodyLarge,
                color = ElectricCyan.copy(alpha = 0.9f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingScreen(
    initialLanguage: AppLanguage,
    onComplete: (languageCode: String, categories: Set<String>) -> Unit
) {
    var selectedLang by remember { mutableStateOf(initialLanguage) }
    var selectedCategories by remember {
        mutableStateOf(setOf("Tech Tutorials", "Stories"))
    }
    val pagerState = rememberPagerState(pageCount = { 3 })
    val scope = rememberCoroutineScope()

    val slides = listOf(
        Triple(
            Icons.Rounded.PlayCircle,
            tr(selectedLang, "সহজ বাংলায় টেক টিউটোরিয়াল", "Tech Tutorials in Clear Bangla"),
            tr(
                selectedLang,
                "অ্যান্ড্রয়েড, AI এবং কোডিং শিখুন ধাপে ধাপে—প্রতিটি ভিডিওর সাথে কোড ও PDF রিসোর্সসহ।",
                "Learn Android, AI, and coding step-by-step with attached source code and PDF cheatsheets."
            )
        ),
        Triple(
            Icons.Rounded.AutoStories,
            tr(selectedLang, "রোমাঞ্চকর বাংলা টেক গল্প", "Gripping Bangla Tech Stories"),
            tr(
                selectedLang,
                "ধারাবাহিক অডিও-ভিজ্যুয়াল গল্প সিরিজ দেখুন পর্ব অনুযায়ী—পরবর্তী পর্বের অটো-সাজেশনসহ।",
                "Watch multi-part Bangla tech story series episode by episode with next-part auto-suggest."
            )
        ),
        Triple(
            Icons.Rounded.Quiz,
            tr(selectedLang, "কুইজ, XP ও স্মার্ট কমিউনিটি", "Quizzes, XP & Smart Community"),
            tr(
                selectedLang,
                "প্রতিদিনের টেক কুইজ খেলুন, ব্যাজ জিতুন এবং প্রশ্নোত্তর ও ভোটে অংশ নিন।",
                "Play daily tech quizzes, earn badges, and join community Q&A and video topic polls."
            )
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(20.dp)
    ) {
        // Language toggle bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AppLanguage.entries.forEach { langOption ->
                    val active = selectedLang == langOption
                    Surface(
                        color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .clickable { selectedLang = langOption }
                            .testTag("onboarding_lang_${langOption.code}")
                    ) {
                        Text(
                            text = langOption.label,
                            style = MaterialTheme.typography.labelLarge,
                            color = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
            }
            TextButton(
                onClick = { onComplete(selectedLang.code, selectedCategories) },
                modifier = Modifier.testTag("onboarding_skip_button")
            ) {
                Text(tr(selectedLang, "এড়িয়ে যান", "Skip"))
            }
        }

        Spacer(Modifier.height(12.dp))

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            val (icon, title, body) = slides[page]
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(210.dp)
                        .clip(RoundedCornerShape(24.dp))
                ) {
                    val imgRes = when (page) {
                        0 -> R.drawable.img_onboarding_tech_1795032434908
                        1 -> R.drawable.img_story_featured_1795032434219
                        else -> R.drawable.img_tech_tip_banner_1795032452539
                    }
                    Image(
                        painter = painterResource(id = imgRes),
                        contentDescription = title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Surface(
                        color = DeepMidnight.copy(alpha = 0.78f),
                        shape = CircleShape,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(12.dp)
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = ElectricCyan,
                            modifier = Modifier
                                .padding(10.dp)
                                .size(26.dp)
                        )
                    }
                }

                Spacer(Modifier.height(22.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                if (page == 2) {
                    Spacer(Modifier.height(18.dp))
                    Text(
                        text = tr(selectedLang, "পছন্দের ক্যাটাগরি বেছে নিন:", "Choose favorite categories:"),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(10.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            AppCategory.TECH,
                            AppCategory.STORIES,
                            AppCategory.TIPS,
                            AppCategory.SHORTS
                        ).forEach { cat ->
                            val isSelected = selectedCategories.contains(cat.key)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedCategories = if (isSelected && selectedCategories.size > 1) {
                                        selectedCategories - cat.key
                                    } else {
                                        selectedCategories + cat.key
                                    }
                                },
                                label = {
                                    Text(if (selectedLang == AppLanguage.BANGLA) cat.bnLabel else cat.enLabel)
                                },
                                leadingIcon = if (isSelected) {
                                    { Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                } else null,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                    selectedLabelColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    }
                }
            }
        }

        // Page indicators + CTA
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(3) { idx ->
                val active = pagerState.currentPage == idx
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .height(8.dp)
                        .width(if (active) 24.dp else 8.dp)
                        .clip(CircleShape)
                        .background(if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline)
                )
            }
        }

        Spacer(Modifier.height(18.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (pagerState.currentPage > 0) {
                OutlinedButton(
                    onClick = {
                        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
                    },
                    modifier = Modifier.weight(1f).height(50.dp)
                ) {
                    Text(tr(selectedLang, "পেছনে", "Back"))
                }
            }
            Button(
                onClick = {
                    if (pagerState.currentPage < 2) {
                        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                    } else {
                        onComplete(selectedLang.code, selectedCategories)
                    }
                },
                modifier = Modifier
                    .weight(2f)
                    .height(50.dp)
                    .testTag("onboarding_next_button"),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(
                    if (pagerState.currentPage < 2) tr(selectedLang, "পরবর্তী", "Next")
                    else tr(selectedLang, "শুরু করুন", "Get Started")
                )
                Spacer(Modifier.width(6.dp))
                Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
            }
        }
    }
}
