package com.example.igate.presentation.teacher

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
import com.example.igate.domain.model.StudentRosterItem
import com.example.igate.theme.AcademicNegative
import com.example.igate.theme.AcademicSuccess
import com.example.igate.theme.AcademicWarning
import com.example.igate.theme.BrandBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherStudentsScreen(onBackClick: () -> Unit = {}) {
    val batches = listOf("All Batches", "GATE CS Alpha 2027", "GATE CS Beta 2027", "GATE ME 2027")
    var selectedBatch by remember { mutableStateOf(batches[0]) }
    var searchQuery by remember { mutableStateOf("") }

    val students = remember {
        listOf(
            StudentRosterItem("s1", "Ravi Shankar", "ravi@igate.com", "GATE CS Alpha 2027", GateBranch.CS, 94, 88, 2, "Today"),
            StudentRosterItem("s2", "Priya Mehta", "priya@igate.com", "GATE CS Alpha 2027", GateBranch.CS, 89, 92, 0, "1 hr ago"),
            StudentRosterItem("s3", "Ankit Gupta", "ankit@igate.com", "GATE CS Beta 2027", GateBranch.CS, 72, 65, 5, "Yesterday"),
            StudentRosterItem("s4", "Sneha Patel", "sneha@igate.com", "GATE CS Beta 2027", GateBranch.CS, 95, 91, 1, "Today"),
            StudentRosterItem("s5", "Rahul Verma", "rahul@igate.com", "GATE ME 2027", GateBranch.ME, 60, 58, 8, "3 days ago"),
            StudentRosterItem("s6", "Deepa Iyer", "deepa@igate.com", "GATE CS Alpha 2027", GateBranch.CS, 88, 85, 0, "Today"),
            StudentRosterItem("s7", "Karthik Nair", "karthik@igate.com", "GATE CS Beta 2027", GateBranch.CS, 45, 51, 12, "5 days ago"),
        )
    }

    val filtered = students.filter { s ->
        (selectedBatch == batches[0] || s.batchName == selectedBatch) &&
        (searchQuery.isBlank() || s.name.contains(searchQuery, ignoreCase = true))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Student Roster", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StudentStatChip("${students.size}", "Total", BrandBlue, Modifier.weight(1f))
                    StudentStatChip("${students.count { it.attendancePercent >= 80 }}", "Consistent", AcademicSuccess, Modifier.weight(1f))
                    StudentStatChip("${students.count { it.pendingDoubts > 0 }}", "Need Help", AcademicWarning, Modifier.weight(1f))
                    StudentStatChip("${students.count { it.attendancePercent < 60 }}", "At Risk", AcademicNegative, Modifier.weight(1f))
                }
            }

            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search students...", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    leadingIcon = { Icon(Icons.Filled.Search, null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true
                )
            }

            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(batches.size) { idx ->
                        val b = batches[idx]
                        val isSel = b == selectedBatch
                        Surface(
                            modifier = Modifier.clip(RoundedCornerShape(20.dp)).clickable { selectedBatch = b },
                            color = if (isSel) BrandBlue else MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text(
                                b,
                                fontSize = 12.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            item { Text("${filtered.size} Students", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }

            items(filtered.size) { idx -> StudentRosterCard(filtered[idx]) }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun StudentStatChip(value: String, label: String, color: Color, modifier: Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(10.dp), color = color.copy(alpha = 0.08f)) {
        Column(Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.Black, color = color)
            Text(label, fontSize = 9.sp, color = color.copy(alpha = 0.8f), fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun StudentRosterCard(student: StudentRosterItem) {
    val attendanceColor = when {
        student.attendancePercent >= 80 -> AcademicSuccess
        student.attendancePercent >= 60 -> AcademicWarning
        else -> AcademicNegative
    }
    val accuracyColor = when {
        student.avgAccuracy >= 80 -> AcademicSuccess
        student.avgAccuracy >= 60 -> AcademicWarning
        else -> AcademicNegative
    }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(40.dp).clip(CircleShape).background(BrandBlue.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) { Text(student.name.first().uppercase(), fontWeight = FontWeight.Bold, color = BrandBlue, fontSize = 16.sp) }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(student.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text(student.email, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Last active", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(student.lastActive, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricPill("Attendance", "${student.attendancePercent}%", attendanceColor, Modifier.weight(1f))
                MetricPill("Accuracy", "${student.avgAccuracy}%", accuracyColor, Modifier.weight(1f))
                MetricPill("Doubts", if (student.pendingDoubts > 0) "${student.pendingDoubts}" else "0 ?", if (student.pendingDoubts > 0) AcademicWarning else AcademicSuccess, Modifier.weight(1f))
            }
            Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(6.dp)) {
                Text("${student.batchName} � ${student.branch.code}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
            }
        }
    }
}

@Composable
private fun MetricPill(label: String, value: String, color: Color, modifier: Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(8.dp), color = color.copy(alpha = 0.08f)) {
        Column(Modifier.padding(vertical = 6.dp, horizontal = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = color)
            Text(label, fontSize = 9.sp, color = color.copy(alpha = 0.75f))
        }
    }
}
