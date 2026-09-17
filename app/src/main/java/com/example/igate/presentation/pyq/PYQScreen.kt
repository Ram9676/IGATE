package com.example.igate.presentation.pyq

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.igate.domain.model.GateBranch
import com.example.igate.domain.model.PYQPaper
import com.example.igate.theme.AcademicSuccess
import com.example.igate.theme.AcademicWarning
import com.example.igate.theme.BrandBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PYQScreen(
    onBackClick: () -> Unit = {},
    onStartPYQ: (PYQPaper) -> Unit = {}
) {
    var selectedBranch by remember { mutableStateOf(GateBranch.CS) }
    var selectedYear by remember { mutableStateOf<Int?>(null) }

    val years = (2016..2024).toList().reversed()
    val papers = remember(selectedBranch) {
        years.map { year ->
            PYQPaper(
                id = "pyq_${selectedBranch.code}_$year",
                year = year,
                branch = selectedBranch,
                totalQuestions = 65,
                durationMinutes = 180
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Previous Year Questions", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text("GATE Official Papers", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
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
            item {
                Text("Select Branch", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(10.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(GateBranch.values().size) { index ->
                        val branch = GateBranch.values()[index]
                        val isSelected = branch == selectedBranch
                        Surface(
                            onClick = { selectedBranch = branch },
                            color = if (isSelected) BrandBlue else MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(branch.code, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface)
                                Text(branch.displayName.take(8), fontSize = 9.sp, color = if (isSelected) Color.White.copy(alpha = 0.75f) else MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    PYQStatCard("Papers", "${papers.size}", BrandBlue, Modifier.weight(1f))
                    PYQStatCard("Questions", "${papers.size * 65}", AcademicSuccess, Modifier.weight(1f))
                    PYQStatCard("Duration", "3 hrs", AcademicWarning, Modifier.weight(1f))
                }
            }

            item {
                Text("GATE ${selectedBranch.code} Papers", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            items(papers.size) { index ->
                val paper = papers[index]
                val isExpanded = selectedYear == paper.year
                Surface(
                    onClick = { selectedYear = if (isExpanded) null else paper.year },
                    modifier = Modifier.fillMaxWidth().animateContentSize(),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(if (isExpanded) 1.5.dp else 1.dp, if (isExpanded) BrandBlue else MaterialTheme.colorScheme.outline)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(if (isExpanded) BrandBlue.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant),
                                    contentAlignment = Alignment.Center
                                ) { Text("${paper.year}", fontSize = 13.sp, fontWeight = FontWeight.Black, color = if (isExpanded) BrandBlue else MaterialTheme.colorScheme.onSurface) }
                                Spacer(Modifier.width(14.dp))
                                Column {
                                    Text("GATE ${paper.year} � ${paper.branch.displayName}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    Text("${paper.totalQuestions} Qs � ${paper.durationMinutes} mins � 100 Marks", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Icon(if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        AnimatedVisibility(visible = isExpanded) {
                            Column(Modifier.padding(top = 16.dp)) {
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                                Spacer(Modifier.height(14.dp))
                                Text("Sections", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(Modifier.height(8.dp))
                                paper.sections.forEach { section ->
                                    Row(Modifier.padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Box(Modifier.size(6.dp).clip(CircleShape).background(BrandBlue))
                                        Spacer(Modifier.width(8.dp))
                                        Text(section, style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                                Spacer(Modifier.height(16.dp))
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    OutlinedButton(onClick = { onStartPYQ(paper) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) {
                                        Icon(Icons.Filled.Visibility, null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text("Review", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                    }
                                    Button(onClick = { onStartPYQ(paper) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)) {
                                        Icon(Icons.Filled.PlayArrow, null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text("Attempt", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun PYQStatCard(label: String, value: String, color: Color, modifier: Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(12.dp), color = color.copy(alpha = 0.08f), border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.2f))) {
        Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.Black, color = color)
            Text(label, fontSize = 10.sp, color = color.copy(alpha = 0.8f), fontWeight = FontWeight.Medium)
        }
    }
}
