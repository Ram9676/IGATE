package com.example.igate.presentation.analytics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.igate.theme.AcademicSuccess
import com.example.igate.theme.AcademicWarning
import com.example.igate.theme.BrandBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("GATE Performance Analytics", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Serious GATE Academic Metrics Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AcademicMetricCard(
                        modifier = Modifier.weight(1f),
                        title = "Est. GATE Score",
                        value = "742",
                        subtitle = "Out of 1000",
                        icon = Icons.Filled.School,
                        color = BrandBlue
                    )
                    AcademicMetricCard(
                        modifier = Modifier.weight(1f),
                        title = "All India Rank",
                        value = "97.4%ile",
                        subtitle = "Top 2.6% Aspirants",
                        icon = Icons.Filled.Assessment,
                        color = AcademicSuccess
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AcademicMetricCard(
                        modifier = Modifier.weight(1f),
                        title = "Hours Studied",
                        value = "124 hrs",
                        subtitle = "Past 30 Days",
                        icon = Icons.Filled.Timer,
                        color = MaterialTheme.colorScheme.primary
                    )
                    AcademicMetricCard(
                        modifier = Modifier.weight(1f),
                        title = "Overall Accuracy",
                        value = "86.5%",
                        subtitle = "Across all Mocks",
                        icon = Icons.Filled.Assessment,
                        color = AcademicSuccess
                    )
                }
            }

            // Canvas Bar Chart: Weekly Study Hours
            item {
                Text(
                    text = "Weekly Study Hours (Mon - Sun)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(18.dp)),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    val primaryColor = BrandBlue
                    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant
                    val dataPoints = listOf(4.5f, 6.0f, 3.5f, 7.5f, 5.0f, 6.5f, 8.0f)
                    val maxData = 10f

                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp)
                    ) {
                        val canvasWidth = size.width
                        val canvasHeight = size.height - 20f

                        val barWidth = canvasWidth / (dataPoints.size * 2f)
                        val gap = barWidth

                        dataPoints.forEachIndexed { index, value ->
                            val xOffset = (index * (barWidth + gap)) + (gap / 2f)
                            val barHeight = (value / maxData) * canvasHeight
                            val yOffset = canvasHeight - barHeight

                            // Background Track
                            drawRoundRect(
                                color = surfaceVariantColor,
                                topLeft = Offset(xOffset, 0f),
                                size = Size(barWidth, canvasHeight),
                                cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
                            )

                            // Active Bar
                            drawRoundRect(
                                color = primaryColor,
                                topLeft = Offset(xOffset, yOffset),
                                size = Size(barWidth, barHeight),
                                cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
                            )
                        }
                    }
                }
            }

            // Subject-wise Accuracy Breakdown
            item {
                Text(
                    text = "Subject-Wise Accuracy & Preparedness",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(12.dp))

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SubjectAccuracyBar(subject = "Data Structures & Algorithms", accuracy = 88)
                    SubjectAccuracyBar(subject = "Operating Systems", accuracy = 76)
                    SubjectAccuracyBar(subject = "Database Management Systems", accuracy = 92)
                    SubjectAccuracyBar(subject = "Engineering Mathematics", accuracy = 82)
                    SubjectAccuracyBar(subject = "Theory of Computation", accuracy = 70)
                    SubjectAccuracyBar(subject = "Computer Networks", accuracy = 65)
                    SubjectAccuracyBar(subject = "Compiler Design", accuracy = 58)
                }
            }

            // Mock Test History Table
            item {
                Text("Mock Test History", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column {
                        // Header
                        Row(
                            modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant).padding(horizontal = 16.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Test", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(2f))
                            Text("Score", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            Text("Rank", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        }

                        val mockHistory = listOf(
                            Triple("GATE Mock 4 - Full", "67.3 / 100", "AIR 234"),
                            Triple("GATE Mock 3 - Full", "59.0 / 100", "AIR 512"),
                            Triple("DSA Topic Test", "88%", "AIR 45"),
                            Triple("OS Subject Test", "74%", "AIR 189"),
                            Triple("GATE Mock 2 - Full", "53.7 / 100", "AIR 820"),
                        )
                        mockHistory.forEachIndexed { i, (name, score, rank) ->
                            if (i > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(name, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(2f), maxLines = 2)
                                Text(score, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = BrandBlue, modifier = Modifier.weight(1f))
                                Text(rank, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = AcademicSuccess, modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            // Weak Areas
            item {
                val weakSubjects = listOf("Compiler Design (58%)", "Computer Networks (65%)", "Theory of Computation (70%)")
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = androidx.compose.ui.graphics.Color(0xFFFFF3E0),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AcademicWarning.copy(alpha = 0.3f))
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(androidx.compose.material.icons.Icons.Filled.Warning, null, tint = AcademicWarning, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Weak Areas — Needs Focus", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = AcademicWarning)
                        }
                        weakSubjects.forEach { subject ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier.size(6.dp).clip(androidx.compose.foundation.shape.CircleShape).background(AcademicWarning)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(subject, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }

            // National Rank Simulation
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = BrandBlue.copy(alpha = 0.06f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BrandBlue.copy(alpha = 0.2f))
                ) {
                    Column(Modifier.padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(androidx.compose.material.icons.Icons.Filled.EmojiEvents, null, tint = BrandBlue, modifier = Modifier.size(32.dp))
                        Text("Simulated All India Rank", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = BrandBlue)
                        Text("AIR  ~  234", fontSize = 28.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
                        Text("Top 2.6% of 9,35,000 GATE CS aspirants", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(4.dp))
                        Surface(color = BrandBlue.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)) {
                            Text("Based on last 5 mock test performance", fontSize = 10.sp, color = BrandBlue, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AcademicMetricCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    color: Color
) {
    Surface(
        modifier = modifier.clip(RoundedCornerShape(16.dp)),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            }
            Column {
                Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                Text(text = subtitle, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun SubjectAccuracyBar(subject: String, accuracy: Int) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = subject, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Text(text = "$accuracy%", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BrandBlue)
            }
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { accuracy / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (accuracy >= 80) AcademicSuccess else BrandBlue,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}
