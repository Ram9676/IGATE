package com.example.igate.presentation.live

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.igate.domain.model.LiveClass
import com.example.igate.domain.model.LiveClassStatus
import com.example.igate.theme.AcademicNegative
import com.example.igate.theme.AcademicSuccess
import com.example.igate.theme.AcademicWarning
import com.example.igate.theme.BrandBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveClassScreen(
    onBackClick: () -> Unit = {}
) {
    val liveClasses = remember {
        listOf(
            LiveClass("lc1", "Dijkstra & Bellman-Ford � Complete Derivation", "Prof. Arvind Rao", "Algorithms", "Today, 5:30 PM", 90, LiveClassStatus.LIVE, watchersCount = 2847),
            LiveClass("lc2", "OS Deadlock � Banker's Algorithm Deep Dive", "Prof. Suresh Mehta", "Operating Systems", "Today, 7:00 PM", 75, LiveClassStatus.SCHEDULED),
            LiveClass("lc3", "ER Diagrams to Relational Schema", "Dr. Priya Nair", "DBMS", "Tomorrow, 6:00 PM", 60, LiveClassStatus.SCHEDULED),
            LiveClass("lc4", "Engineering Maths � Linear Algebra PYQ Session", "Prof. Arvind Rao", "Engineering Maths", "Yesterday, 5:30 PM", 90, LiveClassStatus.ENDED, recordingUrl = "recorded"),
            LiveClass("lc5", "Theory of Computation � Pumping Lemma", "Dr. Kiran Joshi", "Theory of Computation", "2 days ago", 80, LiveClassStatus.ENDED, recordingUrl = "recorded"),
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Live & Scheduled Classes", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text("Unacademy-style live sessions", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = { IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Live Now classes
            val liveNow = liveClasses.filter { it.status == LiveClassStatus.LIVE }
            if (liveNow.isNotEmpty()) {
                item { Text("??  LIVE NOW", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = AcademicNegative) }
                items(liveNow.size) { LiveClassCard(liveNow[it]) }
            }

            // Upcoming
            val upcoming = liveClasses.filter { it.status == LiveClassStatus.SCHEDULED }
            item { Text("??  Upcoming Sessions", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold) }
            items(upcoming.size) { LiveClassCard(upcoming[it]) }

            // Recordings
            val ended = liveClasses.filter { it.status == LiveClassStatus.ENDED }
            item { Text("??  Past Recordings", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold) }
            items(ended.size) { LiveClassCard(ended[it]) }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun LiveClassCard(liveClass: LiveClass) {
    val isLive = liveClass.status == LiveClassStatus.LIVE
    val isEnded = liveClass.status == LiveClassStatus.ENDED

    // Pulsing animation for LIVE dot
    val infiniteTransition = rememberInfiniteTransition(label = "live_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
        label = "pulse_alpha"
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            if (isLive) 1.5.dp else 1.dp,
            if (isLive) AcademicNegative.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outline
        )
    ) {
        Column {
            if (isLive) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(listOf(AcademicNegative.copy(alpha = 0.08f), AcademicNegative.copy(alpha = 0.02f)))
                        )
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(8.dp).clip(CircleShape).background(AcademicNegative).alpha(pulseAlpha))
                        Spacer(Modifier.width(6.dp))
                        Text("LIVE", fontSize = 11.sp, fontWeight = FontWeight.Black, color = AcademicNegative, letterSpacing = 1.sp)
                        Spacer(Modifier.width(12.dp))
                        Text("${liveClass.watchersCount.formatNumber()} watching", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            Column(modifier = Modifier.padding(16.dp)) {
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.Top) {
                    Column(Modifier.weight(1f)) {
                        Text(liveClass.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, maxLines = 2)
                        Spacer(Modifier.height(4.dp))
                        Text(liveClass.educatorName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(color = BrandBlue.copy(alpha = 0.1f), shape = RoundedCornerShape(6.dp)) {
                                Text(liveClass.subjectName, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = BrandBlue, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                            Spacer(Modifier.width(8.dp))
                            Text(liveClass.scheduledAt, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Box(
                        modifier = Modifier.size(48.dp).clip(RoundedCornerShape(14.dp)).background(
                            when (liveClass.status) {
                                LiveClassStatus.LIVE -> AcademicNegative.copy(alpha = 0.1f)
                                LiveClassStatus.SCHEDULED -> BrandBlue.copy(alpha = 0.08f)
                                LiveClassStatus.ENDED -> MaterialTheme.colorScheme.surfaceVariant
                            }
                        ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            when (liveClass.status) {
                                LiveClassStatus.LIVE -> Icons.Filled.LiveTv
                                LiveClassStatus.SCHEDULED -> Icons.Filled.CalendarToday
                                LiveClassStatus.ENDED -> Icons.Filled.PlayCircle
                            },
                            null,
                            tint = when (liveClass.status) {
                                LiveClassStatus.LIVE -> AcademicNegative
                                LiveClassStatus.SCHEDULED -> BrandBlue
                                LiveClassStatus.ENDED -> MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (isLive) {
                        Button(
                            onClick = {},
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AcademicNegative)
                        ) {
                            Icon(Icons.Filled.VideoCall, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Join Live Class", fontWeight = FontWeight.Bold)
                        }
                    } else if (isEnded) {
                        OutlinedButton(
                            onClick = {},
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Filled.PlayArrow, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Watch Recording", fontWeight = FontWeight.SemiBold)
                        }
                    } else {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedButton(onClick = {}, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) {
                                Icon(Icons.Filled.NotificationsActive, null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Remind Me", fontSize = 13.sp)
                            }
                            Button(onClick = {}, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) {
                                Text("Set Reminder", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun Int.formatNumber(): String = when {
    this >= 1000 -> "${this / 1000}.${(this % 1000) / 100}K"
    else -> "$this"
}
