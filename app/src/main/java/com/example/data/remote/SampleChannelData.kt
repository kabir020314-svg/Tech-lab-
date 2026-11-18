package com.example.data.remote

import com.example.data.model.BadgeItem
import com.example.data.model.NotificationItem
import com.example.data.model.PlaylistItem
import com.example.data.model.QuizQuestionItem
import com.example.data.model.ResourceType
import com.example.data.model.TechTipItem
import com.example.data.model.ToolDirectoryItem
import com.example.data.model.VideoItem
import com.example.data.model.VideoResourceItem

object SampleChannelData {

    val videos = listOf(
        VideoItem(
            id = "dQw4w9WgXcQ",
            titleBn = "Android Jetpack Compose ও AI দিয়ে সম্পূর্ণ বাংলা অ্যাপ বানান (পর্ব ১)",
            titleEn = "Build a Complete Android App with Jetpack Compose & AI (Part 1)",
            descriptionBn = "এই টিউটোরিয়ালে আমরা শিখবো কীভাবে Material 3 এবং Jetpack Compose দিয়ে আধুনিক অ্যান্ড্রয়েড অ্যাপ তৈরি করা যায়। সোর্স কোড ও PDF নিচের রিসোর্সে পাবেন।",
            descriptionEn = "Learn how to build a modern Material 3 Android app using Jetpack Compose from scratch. Source code and PDF notes are attached below.",
            category = "Tech Tutorials",
            playlistId = "pl_compose",
            partNumber = 1,
            thumbnailUrl = "https://img.youtube.com/vi/dQw4w9WgXcQ/hqdefault.jpg",
            publishedAtLabel = "২ দিন আগে • 2 days ago",
            publishedEpochMillis = 1794850000000L,
            viewCount = 48200,
            likeCount = 4120,
            durationSeconds = 1120,
            durationLabel = "18:40",
            trending = true
        ),
        VideoItem(
            id = "kJQP7kiw5Fk",
            titleBn = "ঢাকার এক অদৃশ্য কোডার — বাংলা টেক থ্রিলার গল্প (পর্ব ১)",
            titleEn = "The Invisible Coder of Dhaka — Bangla Tech Story (Part 1)",
            descriptionBn = "রাত ১২টার পর ঢাকার একটি ছোট মেস থেকে শুরু হওয়া এক অদ্ভুত সাইবার রহস্যের গল্প। প্রথম পর্বে জানুন তানভীরের হারানো সার্ভারের কাহিনী।",
            descriptionEn = "A gripping Bangla tech mystery starting in a quiet Dhaka hostel after midnight. Episode 1 uncovers Tanvir's missing server.",
            category = "Stories",
            playlistId = "pl_dhaka_story",
            partNumber = 1,
            thumbnailUrl = "https://img.youtube.com/vi/kJQP7kiw5Fk/hqdefault.jpg",
            publishedAtLabel = "৫ দিন আগে • 5 days ago",
            publishedEpochMillis = 1794600000000L,
            viewCount = 92400,
            likeCount = 8750,
            durationSeconds = 1540,
            durationLabel = "25:40",
            trending = true
        ),
        VideoItem(
            id = "9bZkp7q19f0",
            titleBn = "ঢাকার এক অদৃশ্য কোডার — ফায়ারওয়াল ভেদ (পর্ব ২)",
            titleEn = "The Invisible Coder of Dhaka — Breach the Firewall (Part 2)",
            descriptionBn = "গল্পের দ্বিতীয় পর্বে লগ ফাইলের ভেতরে লুকানো সংকেত উদ্ধার করে তানভীর। পরবর্তী পর্বের জন্য প্লেলিস্ট ফলো করুন।",
            descriptionEn = "In Episode 2, Tanvir decodes the hidden signal buried inside server logs. Follow the playlist for Part 3.",
            category = "Stories",
            playlistId = "pl_dhaka_story",
            partNumber = 2,
            thumbnailUrl = "https://img.youtube.com/vi/9bZkp7q19f0/hqdefault.jpg",
            publishedAtLabel = "১ সপ্তাহ আগে • 1 week ago",
            publishedEpochMillis = 1794300000000L,
            viewCount = 74100,
            likeCount = 6920,
            durationSeconds = 1685,
            durationLabel = "28:05",
            trending = true
        ),
        VideoItem(
            id = "3JZ_D3ELwOQ",
            titleBn = "ঢাকার এক অদৃশ্য কোডার — চূড়ান্ত সমাধান (পর্ব ৩)",
            titleEn = "The Invisible Coder of Dhaka — Final Resolution (Part 3)",
            descriptionBn = "তৃতীয় ও শেষ পর্বে রহস্যের জট খুলে যায়। প্রযুক্তির নৈতিক ব্যবহার নিয়ে আমাদের বিশেষ বাংলা গল্প।",
            descriptionEn = "The thrilling conclusion of our Bangla tech story series exploring ethical engineering.",
            category = "Stories",
            playlistId = "pl_dhaka_story",
            partNumber = 3,
            thumbnailUrl = "https://img.youtube.com/vi/3JZ_D3ELwOQ/hqdefault.jpg",
            publishedAtLabel = "২ সপ্তাহ আগে • 2 weeks ago",
            publishedEpochMillis = 1793700000000L,
            viewCount = 68900,
            likeCount = 6100,
            durationSeconds = 1810,
            durationLabel = "30:10",
            trending = false
        ),
        VideoItem(
            id = "L_jWHffIx5E",
            titleBn = "AI দিয়ে ৫ মিনিটে ওয়েবসাইট ও প্রজেক্ট বানানোর সেরা ৫টি ট্রিক",
            titleEn = "5 Smart AI Tricks to Build Websites & Projects in 5 Minutes",
            descriptionBn = "সঠিক প্রম্পট ইঞ্জিনিয়ারিং ও AI টুল ব্যবহার করে কীভাবে কোডিং প্রোডাক্টিভিটি ৩ গুণ বাড়াবেন।",
            descriptionEn = "Boost your coding productivity 3x using practical prompt engineering and modern AI developer tools.",
            category = "Tips",
            playlistId = "pl_ai_tips",
            partNumber = 1,
            thumbnailUrl = "https://img.youtube.com/vi/L_jWHffIx5E/hqdefault.jpg",
            publishedAtLabel = "১০ দিন আগে • 10 days ago",
            publishedEpochMillis = 1794000000000L,
            viewCount = 54300,
            likeCount = 4500,
            durationSeconds = 510,
            durationLabel = "08:30",
            trending = true
        ),
        VideoItem(
            id = "fJ9rUzIMcZQ",
            titleBn = "ফোন স্লো হলে ১ মিনিটে ফাস্ট করার ৩টি সেটিংস #Shorts",
            titleEn = "3 Quick Settings to Speed Up Any Slow Android Phone #Shorts",
            descriptionBn = "অ্যানিমেশন স্কেল এবং ব্যাকগ্রাউন্ড প্রসেস কন্ট্রোল করে ফোনকে সুপার ফাস্ট করুন।",
            descriptionEn = "Make your Android phone noticeably faster in 60 seconds with developer animation settings.",
            category = "Shorts",
            playlistId = "pl_shorts",
            partNumber = 1,
            thumbnailUrl = "https://img.youtube.com/vi/fJ9rUzIMcZQ/hqdefault.jpg",
            publishedAtLabel = "৪ দিন আগে • 4 days ago",
            publishedEpochMillis = 1794700000000L,
            viewCount = 128500,
            likeCount = 11400,
            durationSeconds = 58,
            durationLabel = "00:58",
            trending = true
        ),
        VideoItem(
            id = "hT_nvWreIhg",
            titleBn = "Kotlin Coroutines ও StateFlow সহজ বাংলায় (পর্ব ২)",
            titleEn = "Kotlin Coroutines & StateFlow Made Easy (Part 2)",
            descriptionBn = "অ্যাসিঙ্ক্রোনাস প্রোগ্রামিং, Flow, এবং Room ক্যাশিং সহজ উদাহরণসহ শিখুন।",
            descriptionEn = "Master asynchronous Android programming with Coroutines, StateFlow, and Room caching.",
            category = "Tech Tutorials",
            playlistId = "pl_compose",
            partNumber = 2,
            thumbnailUrl = "https://img.youtube.com/vi/hT_nvWreIhg/hqdefault.jpg",
            publishedAtLabel = "৩ সপ্তাহ আগে • 3 weeks ago",
            publishedEpochMillis = 1793100000000L,
            viewCount = 39800,
            likeCount = 3420,
            durationSeconds = 980,
            durationLabel = "16:20",
            trending = false
        ),
        VideoItem(
            id = "OPf0YbXqDm0",
            titleBn = "রাত জাগা প্রোগ্রামারের ডায়েরি — ছোট গল্প #Shorts",
            titleEn = "Diary of a Night-Owl Programmer — Mini Story #Shorts",
            descriptionBn = "কোডিং আর কফির মাঝে এক তরুণের স্বপ্ন জয়ের ৬০ সেকেন্ডের অনুপ্রেরণামূলক গল্প।",
            descriptionEn = "A 60-second inspiring Bangla story about late-night coding, coffee, and persistence.",
            category = "Shorts",
            playlistId = "pl_shorts",
            partNumber = 2,
            thumbnailUrl = "https://img.youtube.com/vi/OPf0YbXqDm0/hqdefault.jpg",
            publishedAtLabel = "৬ দিন আগে • 6 days ago",
            publishedEpochMillis = 1794500000000L,
            viewCount = 67400,
            likeCount = 5890,
            durationSeconds = 55,
            durationLabel = "00:55",
            trending = false
        )
    )

    val playlists = listOf(
        PlaylistItem(
            id = "pl_dhaka_story",
            titleBn = "ঢাকার এক অদৃশ্য কোডার (সম্পূর্ণ সিরিজ)",
            titleEn = "The Invisible Coder of Dhaka (Full Series)",
            descriptionBn = "৩ পর্বের রোমাঞ্চকর বাংলা টেক থ্রিলার গল্প সিরিজ।",
            descriptionEn = "A 3-part suspenseful Bangla tech thriller story series.",
            category = "Stories",
            seriesType = "story_series",
            thumbnailUrl = "https://img.youtube.com/vi/kJQP7kiw5Fk/hqdefault.jpg",
            totalVideos = 3,
            videoIds = listOf("kJQP7kiw5Fk", "9bZkp7q19f0", "3JZ_D3ELwOQ")
        ),
        PlaylistItem(
            id = "pl_compose",
            titleBn = "আধুনিক অ্যান্ড্রয়েড ও Jetpack Compose বাংলা কোর্স",
            titleEn = "Modern Android & Jetpack Compose Course",
            descriptionBn = "শুরু থেকে অ্যাডভান্সড অ্যাপ ডেভেলপমেন্ট স্টেপ-বাই-স্টেপ গাইড।",
            descriptionEn = "Step-by-step modern Android development guide from beginner to pro.",
            category = "Tech Tutorials",
            seriesType = "playlist",
            thumbnailUrl = "https://img.youtube.com/vi/dQw4w9WgXcQ/hqdefault.jpg",
            totalVideos = 2,
            videoIds = listOf("dQw4w9WgXcQ", "hT_nvWreIhg")
        ),
        PlaylistItem(
            id = "pl_ai_tips",
            titleBn = "স্মার্ট AI ও প্রোডাক্টিভিটি টিপস",
            titleEn = "Smart AI & Productivity Tips",
            descriptionBn = "দৈনন্দিন কাজে সময় বাঁচানোর সেরা টেক টিপস।",
            descriptionEn = "Practical AI workflows and developer productivity hacks.",
            category = "Tips",
            seriesType = "playlist",
            thumbnailUrl = "https://img.youtube.com/vi/L_jWHffIx5E/hqdefault.jpg",
            totalVideos = 1,
            videoIds = listOf("L_jWHffIx5E")
        ),
        PlaylistItem(
            id = "pl_shorts",
            titleBn = "৬০ সেকেন্ডে টেক ও গল্প (Shorts)",
            titleEn = "60-Second Tech & Mini Stories",
            descriptionBn = "দ্রুত শেখার শর্টস এবং অনুপ্রেরণামূলক মিনি গল্প।",
            descriptionEn = "Quick tech tricks and bite-sized inspirational stories.",
            category = "Shorts",
            seriesType = "playlist",
            thumbnailUrl = "https://img.youtube.com/vi/fJ9rUzIMcZQ/hqdefault.jpg",
            totalVideos = 2,
            videoIds = listOf("fJ9rUzIMcZQ", "OPf0YbXqDm0")
        )
    )

    val videoResources = listOf(
        VideoResourceItem(
            id = "res_1",
            videoId = "dQw4w9WgXcQ",
            titleBn = "Jetpack Compose স্টার্টার সোর্স কোড (GitHub)",
            titleEn = "Jetpack Compose Starter Source Code (GitHub)",
            resourceType = ResourceType.CODE,
            url = "https://github.com/android/compose-samples",
            snippetOrNote = "Scaffold + StateFlow + MaterialTheme3 template"
        ),
        VideoResourceItem(
            id = "res_2",
            videoId = "dQw4w9WgXcQ",
            titleBn = "বাংলায় Compose চিটশিট PDF",
            titleEn = "Compose Cheatsheet PDF (Bangla Notes)",
            resourceType = ResourceType.PDF,
            url = "https://developer.android.com/develop/ui/compose/documentation",
            snippetOrNote = "Key modifiers: fillMaxWidth, padding, LazyColumn, remember"
        ),
        VideoResourceItem(
            id = "res_3",
            videoId = "kJQP7kiw5Fk",
            titleBn = "গল্পের সাউন্ডট্র্যাক ও ব্যাকগ্রাউন্ড আর্ট প্যাক",
            titleEn = "Story Soundtrack & Background Pack",
            resourceType = ResourceType.LINK,
            url = "https://kabirtechlab.example.com/stories/dhaka-coder",
            snippetOrNote = "Episode 1-3 timeline & character glossary"
        ),
        VideoResourceItem(
            id = "res_4",
            videoId = "L_jWHffIx5E",
            titleBn = "AI প্রম্পট লাইব্রেরি ও চেকলিস্ট",
            titleEn = "AI Prompt Engineering Checklist",
            resourceType = ResourceType.TOOL,
            url = "https://aistudio.google.com",
            snippetOrNote = "Role + Context + Constraints + Output Schema pattern"
        ),
        VideoResourceItem(
            id = "res_5",
            videoId = "hT_nvWreIhg",
            titleBn = "Coroutines ও Room অফলাইন ক্যাশ গাইড",
            titleEn = "Coroutines & Room Offline Cache Guide",
            resourceType = ResourceType.PDF,
            url = "https://developer.android.com/kotlin/coroutines",
            snippetOrNote = "viewModelScope + stateIn(WhileSubscribed(5000))"
        )
    )

    val toolsDirectory = listOf(
        ToolDirectoryItem(
            id = "tool_1",
            name = "Google AI Studio",
            categoryBn = "এআই টুলস",
            categoryEn = "AI Tools",
            descriptionBn = "দ্রুত প্রোটোটাইপিং, প্রম্পট টেস্টিং এবং অ্যাপ বিল্ডিংয়ের সেরা ফ্রি AI ওয়ার্কস্পেস।",
            descriptionEn = "Fast AI prototyping, prompt testing, and app building workspace.",
            url = "https://aistudio.google.com",
            tag = "AI"
        ),
        ToolDirectoryItem(
            id = "tool_2",
            name = "Android Studio",
            categoryBn = "ডেভেলপমেন্ট",
            categoryEn = "Dev Tools",
            descriptionBn = "Kotlin ও Jetpack Compose দিয়ে অ্যান্ড্রয়েড অ্যাপ তৈরির অফিসিয়াল IDE।",
            descriptionEn = "Official IDE for Kotlin and Jetpack Compose Android development.",
            url = "https://developer.android.com/studio",
            tag = "IDE"
        ),
        ToolDirectoryItem(
            id = "tool_3",
            name = "Firebase Console",
            categoryBn = "ক্লাউড ও ব্যাকএন্ড",
            categoryEn = "Cloud & Backend",
            descriptionBn = "Auth, Firestore এবং Cloud Messaging এর জন্য রিয়েল-টাইম ব্যাকএন্ড।",
            descriptionEn = "Real-time backend for Auth, Firestore, and Cloud Messaging.",
            url = "https://console.firebase.google.com",
            tag = "Backend"
        ),
        ToolDirectoryItem(
            id = "tool_4",
            name = "Avro / Ridmik Keyboard",
            categoryBn = "বাংলা টুলস",
            categoryEn = "Bangla Tools",
            descriptionBn = "সহজ ও দ্রুত বাংলা টাইপিংয়ের জন্য সবার প্রিয় ফোনেটিক কীবোর্ড।",
            descriptionEn = "Fast phonetic Bangla typing tool for desktop and mobile.",
            url = "https://www.omicronlab.com/avro-keyboard.html",
            tag = "Bangla"
        ),
        ToolDirectoryItem(
            id = "tool_5",
            name = "Excalidraw",
            categoryBn = "ডিজাইন ও ডায়াগ্রাম",
            categoryEn = "Design & Diagrams",
            descriptionBn = "টিউটোরিয়াল এবং সিস্টেম আর্কিটেকচার বোঝানোর জন্য চমৎকার হোয়াইটবোর্ড টুল।",
            descriptionEn = "Clean whiteboard tool for system architecture diagrams.",
            url = "https://excalidraw.com",
            tag = "Design"
        )
    )

    val todaysTechTip = TechTipItem(
        id = "tip_daily_1",
        titleBn = "অ্যান্ড্রয়েড অ্যাপ ফাস্ট রাখার গোপন টিপস",
        titleEn = "Secret to Snappy Android Lists",
        bodyBn = "LazyColumn ব্যবহার করার সময় প্রতিটি আইটেমে সবসময় ইউনিক `key = { it.id }` দিন। এতে অপ্রয়োজনীয় রিকম্পোজিশন কমে যায় এবং স্ক্রলিং মাখনের মতো স্মুথ হয়!",
        bodyEn = "Always pass a stable `key = { it.id }` inside LazyColumn items to minimize recompositions and keep scrolling butter-smooth.",
        tag = "Compose Pro Tip"
    )

    val quizzes = listOf(
        QuizQuestionItem(
            id = "q_1",
            questionBn = "Jetpack Compose-এ লম্বা তালিকা স্মুথভাবে দেখাতে কোন কম্পোনেন্ট ব্যবহার করা হয়?",
            questionEn = "Which component renders long scrollable lists efficiently in Jetpack Compose?",
            optionsBn = listOf("LinearLayout", "LazyColumn", "ScrollView", "RecyclerViewAdapter"),
            optionsEn = listOf("LinearLayout", "LazyColumn", "ScrollView", "RecyclerViewAdapter"),
            correctIndex = 1,
            explanationBn = "LazyColumn কেবল স্ক্রিনে দৃশ্যমান আইটেমগুলোই রেন্ডার করে, তাই মেমরি ও পারফরম্যান্স দারুণ থাকে।",
            explanationEn = "LazyColumn composes and lays out only the items currently visible on screen.",
            xpReward = 25
        ),
        QuizQuestionItem(
            id = "q_2",
            questionBn = "অ্যান্ড্রয়েডে কনফিগারেশন চেঞ্জ (যেমন স্ক্রিন রোটেশন) হলেও স্টেট ধরে রাখে কোনটি?",
            questionEn = "Which class survives configuration changes like screen rotation in Android?",
            optionsBn = listOf("Toast", "ViewModel", "Intent", "Drawable"),
            optionsEn = listOf("Toast", "ViewModel", "Intent", "Drawable"),
            correctIndex = 1,
            explanationBn = "ViewModel অ্যাক্টিভিটি রিক্রিয়েট হলেও মেমোরিতে স্টেট নিরাপদ রাখে।",
            explanationEn = "ViewModel retains UI state safely across configuration changes.",
            xpReward = 25
        ),
        QuizQuestionItem(
            id = "q_3",
            questionBn = "বাংলায় যুক্তাক্ষর ও ফন্ট সুন্দর দেখাতে কোন গুগল ফন্ট জনপ্রিয়?",
            questionEn = "Which Google Font is widely recommended for crisp Bangla UI typography?",
            optionsBn = listOf("Hind Siliguri", "Courier New", "Impact", "Wingdings"),
            optionsEn = listOf("Hind Siliguri", "Courier New", "Impact", "Wingdings"),
            correctIndex = 0,
            explanationBn = "Hind Siliguri এবং Noto Sans Bengali আধুনিক মোবাইল UI-এর জন্য অত্যন্ত পরিষ্কার বাংলা ফন্ট।",
            explanationEn = "Hind Siliguri and Noto Sans Bengali offer clean legibility for Bangla UI.",
            xpReward = 25
        )
    )

    val badges = listOf(
        BadgeItem("b_1", "প্রথম যাত্রা", "First Explorer", "অ্যাপে যুক্ত হয়ে ৫০ XP অর্জন করেছেন", "Joined the lab and earned 50 XP", 50),
        BadgeItem("b_2", "কুইজ মাস্টার", "Quiz Learner", "১০০ XP অর্জন করেছেন", "Reached 100 XP milestone", 100),
        BadgeItem("b_3", "গল্প ও টেক প্রিয়", "Lab Champion", "২০০ XP অর্জন করেছেন", "Reached 200 XP milestone", 200),
        BadgeItem("b_4", "কবির টেক লিজেন্ড", "Tech Lab Legend", "৫০০ XP অর্জন করেছেন", "Reached 500 XP milestone", 500)
    )

    val defaultNotifications = listOf(
        NotificationItem(
            id = "n_1",
            type = "upload",
            titleBn = "নতুন ভিডিও আপলোড হয়েছে!",
            titleEn = "New Video Uploaded!",
            bodyBn = "Android Jetpack Compose ও AI দিয়ে সম্পূর্ণ বাংলা অ্যাপ বানান (পর্ব ১) এখন দেখুন।",
            bodyEn = "Watch 'Build a Complete Android App with Jetpack Compose & AI (Part 1)' now.",
            timeLabel = "২ ঘণ্টা আগে • 2h ago",
            targetVideoId = "dQw4w9WgXcQ"
        ),
        NotificationItem(
            id = "n_2",
            type = "tip",
            titleBn = "আজকের টেক টিপ",
            titleEn = "Daily Tech Tip",
            bodyBn = "LazyColumn-এ `key` ব্যবহার করে অ্যাপের স্ক্রলিং পারফরম্যান্স দ্বিগুণ করুন।",
            bodyEn = "Use stable keys in LazyColumn to double your list scrolling smoothness.",
            timeLabel = "আজ সকাল ৯টা • 9:00 AM",
            targetVideoId = null
        ),
        NotificationItem(
            id = "n_3",
            type = "admin",
            titleBn = "কমিউনিটি পোল লাইভ!",
            titleEn = "Community Poll Live!",
            bodyBn = "পরের বাংলা গল্প বা টিউটোরিয়াল কোন বিষয়ে হবে? কমিউনিটি ট্যাবে ভোট দিন।",
            bodyEn = "Vote on the next Bangla story or tutorial topic in the Community tab.",
            timeLabel = "গতকাল • Yesterday",
            targetVideoId = null
        )
    )
}
