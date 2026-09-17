package com.example.igate.presentation.teacher

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.example.igate.theme.AcademicSuccess
import com.example.igate.theme.AcademicWarning
import com.example.igate.theme.BrandBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherTestCreatorScreen(onBackClick: () -> Unit = {}, onPublish: () -> Unit = {}) {
    var testTitle by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("Topic Test") }
    var selectedSubject by remember { mutableStateOf("Data Structures") }
    var durationMinutes by remember { mutableStateOf("30") }

    val testTypes = listOf("Topic Test", "Subject Test", "Full Mock", "PYQ Practice")
    val subjects = listOf("Data Structures", "Algorithms", "Operating Systems", "DBMS", "Computer Networks", "Engineering Mathematics")

    // Mock question bank
    val mockQuestions = remember {
        listOf(
            Triple("DSA-001", "What is the time complexity of Dijkstra's algorithm using a min-heap?", "MCQ"),
            Triple("DSA-002", "Which of the following is true about Red-Black Trees?", "MSQ"),
            Triple("DSA-003", "The number of edges in a complete graph with n vertices is ___.", "NAT"),
            Triple("DSA-004", "Topological sort can be applied on which type of graph?", "MCQ"),
            Triple("DSA-005", "In a BST with n nodes, the average case height is ___.", "NAT"),
        )
    }

    val selectedQuestions = remember { mutableStateListOf<String>() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Test Creator", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } },
                actions = {
                    Button(
                        onClick = onPublish,
                        enabled = testTitle.isNotBlank() && selectedQuestions.isNotEmpty(),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, null, Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Publish", fontWeight = FontWeight.Bold)
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
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Test meta
            item {
                Text("Test Configuration", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = testTitle,
                    onValueChange = { testTitle = it },
                    label = { Text("Test Title") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Filled.EditNote, null) }
                )
            }

            // Test type selector
            item {
                Text("Test Type", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    testTypes.take(2).forEach { type ->
                        val isSel = type == selectedType
                        Surface(
                            onClick = { selectedType = type },
                            modifier = Modifier.weight(1f),
                            color = if (isSel) BrandBlue else MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(10.dp)
                        ) { Box(Modifier.fillMaxWidth().padding(10.dp), Alignment.Center) { Text(type, fontSize = 12.sp, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal, color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface) } }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    testTypes.drop(2).forEach { type ->
                        val isSel = type == selectedType
                        Surface(
                            onClick = { selectedType = type },
                            modifier = Modifier.weight(1f),
                            color = if (isSel) BrandBlue else MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(10.dp)
                        ) { Box(Modifier.fillMaxWidth().padding(10.dp), Alignment.Center) { Text(type, fontSize = 12.sp, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal, color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface) } }
                    }
                }
            }

            // Subject + Duration
            item {
                Text("Subject", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(subjects.size) { idx ->
                        val subj = subjects[idx]
                        val isSel = subj == selectedSubject
                        Surface(
                            onClick = { selectedSubject = subj },
                            color = if (isSel) BrandBlue else MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = subj,
                                fontSize = 12.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = durationMinutes,
                        onValueChange = { durationMinutes = it },
                        label = { Text("Duration (mins)") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Filled.Timer, null, Modifier.size(18.dp)) }
                    )
                }
            }

            // Question bank
            item {
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    Text("Question Bank • $selectedSubject", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Surface(color = BrandBlue.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)) {
                        Text("${selectedQuestions.size} Selected", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BrandBlue, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                    }
                }
            }

            items(mockQuestions.size) { idx ->
                val (id, text, type) = mockQuestions[idx]
                val isSelected = selectedQuestions.contains(id)
                Surface(
                    onClick = {
                        if (isSelected) selectedQuestions.remove(id) else selectedQuestions.add(id)
                    },
                    modifier = Modifier.fillMaxWidth().animateContentSize(),
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) BrandBlue.copy(alpha = 0.06f) else MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(if (isSelected) 1.5.dp else 1.dp, if (isSelected) BrandBlue.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outline)
                ) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = { if (it) selectedQuestions.add(id) else selectedQuestions.remove(id) },
                            colors = CheckboxDefaults.colors(checkedColor = BrandBlue)
                        )
                        Spacer(Modifier.width(8.dp))
                        Column(Modifier.weight(1f)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Surface(color = when(type) { "MCQ" -> BrandBlue.copy(alpha = 0.1f); "MSQ" -> AcademicSuccess.copy(alpha = 0.1f); else -> AcademicWarning.copy(alpha = 0.1f) }, shape = RoundedCornerShape(4.dp)) {
                                    Text(type, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = when(type) { "MCQ" -> BrandBlue; "MSQ" -> AcademicSuccess; else -> AcademicWarning }, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                                Text(id, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(text, style = MaterialTheme.typography.bodySmall, maxLines = 2, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}
