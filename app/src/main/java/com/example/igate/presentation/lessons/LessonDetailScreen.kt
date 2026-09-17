package com.example.igate.presentation.lessons

import android.content.Intent
import android.net.Uri
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.igate.IgateApplication
import com.example.igate.domain.model.Lesson
import com.example.igate.domain.model.VideoTimestamp
import com.example.igate.theme.BrandBlue
import com.example.igate.theme.AcademicSuccess
import com.example.igate.data.analytics.AnalyticsTracker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonDetailScreen(
    lessonId: String,
    lessonTitle: String,
    onBackClick: () -> Unit,
    onPdfClick: (String, String) -> Unit = { _, _ -> }
) {
    val decodedTitle = remember(lessonTitle) {
        try { java.net.URLDecoder.decode(lessonTitle, "UTF-8") } catch (e: Exception) { lessonTitle }
    }

    LaunchedEffect(lessonId) {
        AnalyticsTracker.logLessonViewed(lessonId, "gate_syllabus", decodedTitle)
    }

    val context = LocalContext.current
    val repository = (context.applicationContext as IgateApplication).container.igateRepository
    val recentLessons by repository.getRecentLessons().collectAsState(initial = emptyList())

    // Find current lesson or build high-yield default
    val currentLesson = remember(recentLessons, lessonId) {
        recentLessons.firstOrNull { it.id == lessonId || it.title == decodedTitle } ?: Lesson(
            id = lessonId,
            subjectId = "s1",
            topicName = "Engineering Concepts",
            title = decodedTitle,
            description = "Detailed syllabus video lecture with chapter timestamps and explanatory notes.",
            videoUrl = "https://storage.googleapis.com/exoplayer-test-media-0/BigBuckBunny_320x180.mp4",
            durationString = "48 mins",
            educatorName = "Prof. Arvind Rao (IIT Bombay)",
            isCompleted = false,
            pdfAttachmentTitle = "$decodedTitle Formula Notes.pdf",
            pdfAssetFileName = "mock_pdf_path.pdf",
            timestamps = listOf(
                VideoTimestamp(0, "00:00", "Introduction & Topic Prerequisites", "Fundamental formulas and GATE exam weightage analysis"),
                VideoTimestamp(390, "06:30", "Core Theorem & Mathematical Derivation", "Step-by-step mathematical proof and definitions"),
                VideoTimestamp(945, "15:45", "Worked Example & Algorithm Dry Run", "Complete trace on standard GATE test graph/matrix"),
                VideoTimestamp(1700, "28:20", "GATE 2-Mark Previous Year Question", "Tricks and shortcuts to solve in under 90 seconds"),
                VideoTimestamp(2415, "40:15", "Summary & Key Formulas for Revision", "High-yield revision cheat-sheet notes")
            )
        )
    }

    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Timers / Chapters", "About", "Materials", "Discussion")
    var jumpMessage by remember { mutableStateOf<String?>(null) }

    // Instantiate ExoPlayer
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            val streamUrl = if (currentLesson.videoUrl.isNotBlank() && !currentLesson.videoUrl.contains("youtube.com") && !currentLesson.videoUrl.contains("youtu.be")) {
                currentLesson.videoUrl
            } else {
                "https://storage.googleapis.com/exoplayer-test-media-0/BigBuckBunny_320x180.mp4"
            }
            val mediaItem = MediaItem.fromUri(streamUrl)
            setMediaItem(mediaItem)
            prepare()
            playWhenReady = false
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = decodedTitle,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // 1. Cinematic Video Player Section
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .background(Color.Black)
                ) {
                    AndroidView(
                        factory = { ctx ->
                            PlayerView(ctx).apply {
                                player = exoPlayer
                                layoutParams = FrameLayout.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )
                                useController = true
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // Jump notification banner
            if (jumpMessage != null) {
                item {
                    Surface(
                        color = BrandBlue,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.PlayCircle, null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = jumpMessage ?: "",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { jumpMessage = null }, modifier = Modifier.size(20.dp)) {
                                Icon(Icons.Filled.Close, null, tint = Color.White, modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }
            }

            // YouTube link action if applicable
            if (currentLesson.videoUrl.contains("youtube.com") || currentLesson.videoUrl.contains("youtu.be")) {
                item {
                    Surface(
                        color = Color(0xFFFF0000).copy(alpha = 0.1f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.OndemandVideo, null, tint = Color(0xFFFF0000), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("YouTube Lecture Source", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            FilledTonalButton(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(currentLesson.videoUrl))
                                    context.startActivity(intent)
                                },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Open on YouTube", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // 2. Tab Navigation
            item {
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary,
                    divider = { HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)) }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = {
                                Text(
                                    text = title,
                                    fontSize = 12.sp,
                                    fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        )
                    }
                }
            }

            // 3. Tab Content
            when (selectedTabIndex) {
                0 -> {
                    // Tab 0: Video Chapters & Topic Timers
                    item {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Video Chapters & Topics",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Surface(
                                    color = BrandBlue.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "${currentLesson.timestamps.size} Segments",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BrandBlue,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Tap any timestamp to jump ExoPlayer directly to that explanation topic",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    items(currentLesson.timestamps) { ts ->
                        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                            Surface(
                                onClick = {
                                    val targetMillis = ts.timeSeconds * 1000L
                                    exoPlayer.seekTo(targetMillis)
                                    exoPlayer.play()
                                    jumpMessage = "Jumped to ${ts.timeDisplay}: ${ts.title}"
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        color = BrandBlue,
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Filled.PlayArrow, null, tint = Color.White, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = ts.timeDisplay,
                                                color = Color.White,
                                                fontWeight = FontWeight.Black,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = ts.title,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        if (ts.notes.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = ts.notes,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                    Icon(
                                        imageVector = Icons.Filled.FastForward,
                                        contentDescription = "Seek to time",
                                        tint = BrandBlue,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // Tab 1: About the lesson & Educator
                    item {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = currentLesson.title,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Text(
                                        text = "HD 1080p",
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${currentLesson.durationString} • ${currentLesson.topicName}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // Instructor Profile Card
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(CircleShape)
                                            .background(BrandBlue),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = currentLesson.educatorName.firstOrNull()?.toString() ?: "P",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = currentLesson.educatorName,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleSmall
                                        )
                                        Text(
                                            text = "GATE Senior Faculty • Subject Matter Expert",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            Text(
                                text = "Syllabus Overview",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = currentLesson.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }

                2 -> {
                    // Tab 2: Materials & PDF Downloads
                    item {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = "Lecture Notes & Formula Documents",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Built-in PDF document viewer for exam preparation",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            ResourceCard(
                                title = currentLesson.pdfAttachmentTitle ?: "Lecture Notes & Formulae.pdf",
                                subtitle = "PDF • 2.4 MB • Complete formulas & proofs",
                                onClick = {
                                    onPdfClick(
                                        currentLesson.pdfAssetFileName,
                                        currentLesson.pdfAttachmentTitle ?: "Lecture Notes"
                                    )
                                }
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            ResourceCard(
                                title = "GATE Previous Year Question Handout.pdf",
                                subtitle = "PDF • 1.6 MB • 25 Curated GATE Questions",
                                onClick = {
                                    onPdfClick(
                                        "mock_pdf_path2.pdf",
                                        "GATE Previous Year Question Handout"
                                    )
                                }
                            )
                        }
                    }
                }

                3 -> {
                    // Tab 3: Discussion & Doubts
                    item {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = "Lesson Discussion & Faculty Q&A",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Have a question regarding this video or any timestamp? Submit your query to our 24/7 faculty panel.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Filled.QuestionAnswer, null, tint = BrandBlue)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "3 mentor solutions verified for this lesson",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ResourceCard(
    title: String,
    subtitle: String,
    onClick: () -> Unit = {}
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.error.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.PictureAsPdf,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            }
            Button(
                onClick = onClick,
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text("Open PDF", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
