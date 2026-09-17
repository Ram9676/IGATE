package com.example.igate.presentation.teacher

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.igate.IgateApplication
import com.example.igate.domain.model.VideoTimestamp
import com.example.igate.domain.model.Batch
import com.example.igate.domain.model.Doubt
import com.example.igate.domain.model.GateBranch
import com.example.igate.presentation.IgateViewModelFactory
import com.example.igate.theme.BrandBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherDashboardScreen(
    viewModel: TeacherViewModel = viewModel(
        factory = IgateViewModelFactory((LocalContext.current.applicationContext as IgateApplication).container.igateRepository)
    ),
    onBackClick: () -> Unit = {},
    onStudentsClick: () -> Unit = {},
    onCreateTestClick: () -> Unit = {}
) {
    val batches by viewModel.batches.collectAsState()
    val doubts by viewModel.pendingDoubts.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    var showUploadLectureDialog by remember { mutableStateOf(false) }
    var showUploadPdfDialog by remember { mutableStateOf(false) }
    var activeDoubtToAnswer by remember { mutableStateOf<Doubt?>(null) }

    LaunchedEffect(statusMessage) {
        statusMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearStatus()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("PW Drona / Educator Portal", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text("Prof. Arvind Rao • GATE CS/IT Senior Faculty", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 1. KPI Stats Cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TeacherStatCard(
                        title = "Active Batches",
                        value = batches.size.toString(),
                        icon = Icons.Filled.Group,
                        modifier = Modifier.weight(1f)
                    )
                    TeacherStatCard(
                        title = "Total Learners",
                        value = "1,350",
                        icon = Icons.Filled.School,
                        modifier = Modifier.weight(1f)
                    )
                    TeacherStatCard(
                        title = "Unresolved",
                        value = doubts.count { !it.isResolved }.toString(),
                        icon = Icons.AutoMirrored.Filled.LiveHelp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 2. Quick Content Upload Bar
            item {
                Text(
                    text = "Quick Actions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { showUploadLectureDialog = true },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(imageVector = Icons.Filled.VideoCall, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add Video", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                    FilledTonalButton(
                        onClick = { showUploadPdfDialog = true },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(imageVector = Icons.Filled.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add PDF", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onStudentsClick,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(imageVector = Icons.Filled.Group, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Students", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                    OutlinedButton(
                        onClick = onCreateTestClick,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(imageVector = Icons.Filled.EditNote, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Create Test", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // 3. My Batches Section
            item {
                Text(
                    text = "Assigned GATE Batches",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    batches.forEach { batch ->
                        BatchCard(batch = batch)
                    }
                }
            }

            // 4. Student Doubts Inbox (PW Drona style)
            item {
                Text(
                    text = "Student Doubts Resolution Inbox",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                val pendingList = doubts.filter { !it.isResolved }
                if (pendingList.isEmpty()) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Box(modifier = Modifier.padding(20.dp), contentAlignment = Alignment.Center) {
                            Text("All student doubts are currently resolved!", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        pendingList.forEach { doubt ->
                            TeacherDoubtItem(
                                doubt = doubt,
                                onAnswerClick = { activeDoubtToAnswer = doubt }
                            )
                        }
                    }
                }
            }
        }

        // Upload Video Lecture Dialog
        if (showUploadLectureDialog) {
            UploadLectureDialog(
                onDismiss = { showUploadLectureDialog = false },
                onUpload = { title: String, subjectId: String, topicName: String, duration: String, url: String, pdfTitle: String, timestamps: List<VideoTimestamp> ->
                    viewModel.uploadLecture(
                        title = title,
                        subjectId = subjectId,
                        duration = duration,
                        videoUrl = url,
                        topicName = topicName,
                        pdfTitle = pdfTitle,
                        timestamps = timestamps
                    )
                    showUploadLectureDialog = false
                }
            )
        }

        // Upload PDF Dialog
        if (showUploadPdfDialog) {
            UploadPdfDialog(
                onDismiss = { showUploadPdfDialog = false },
                onUpload = { title, subjectId, url ->
                    viewModel.uploadPdfNote(title, subjectId, url)
                    showUploadPdfDialog = false
                }
            )
        }

        // Answer Doubt Modal
        activeDoubtToAnswer?.let { doubt ->
            AnswerDoubtDialog(
                doubt = doubt,
                onDismiss = { activeDoubtToAnswer = null },
                onSubmitAnswer = { answer ->
                    viewModel.answerDoubt(doubt.id, answer)
                    activeDoubtToAnswer = null
                }
            )
        }
    }
}

@Composable
private fun TeacherStatCard(title: String, value: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Icon(imageVector = icon, contentDescription = null, tint = BrandBlue, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
            Text(text = title, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun BatchCard(batch: Batch) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = batch.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Surface(
                    color = BrandBlue.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "${batch.studentCount} Students",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandBlue,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = "Schedule: ${batch.scheduleTime}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Syllabus Completed", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = "${batch.syllabusProgressPercentage}%", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { batch.syllabusProgressPercentage / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = BrandBlue,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}

@Composable
private fun TeacherDoubtItem(doubt: Doubt, onAnswerClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "${doubt.studentName} • ${doubt.subjectName}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = BrandBlue)
                Text(text = doubt.timestamp, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = doubt.questionTitle, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            Text(text = doubt.questionDetail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = onAnswerClick,
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text("Answer Doubt", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun AnswerDoubtDialog(doubt: Doubt, onDismiss: () -> Unit, onSubmitAnswer: (String) -> Unit) {
    var answerText by remember { mutableStateOf("") }
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(text = "Answer Student Doubt", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "Q: ${doubt.questionTitle}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(text = doubt.questionDetail, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                OutlinedTextField(
                    value = answerText,
                    onValueChange = { answerText = it },
                    label = { Text("Faculty Answer & Formula Explanation") },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    shape = RoundedCornerShape(12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { if (answerText.isNotBlank()) onSubmitAnswer(answerText) },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Submit Verified Solution")
                    }
                }
            }
        }
    }
}

@Composable
private fun UploadLectureDialog(
    onDismiss: () -> Unit,
    onUpload: (title: String, subjectId: String, topicName: String, duration: String, url: String, pdfTitle: String, timestamps: List<VideoTimestamp>) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var subjectId by remember { mutableStateOf("s2") }
    var topicName by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("45 mins") }
    var videoUrl by remember { mutableStateOf("") }
    var pdfTitle by remember { mutableStateOf("") }

    var timestampList by remember {
        mutableStateOf<List<VideoTimestamp>>(
            listOf(
                VideoTimestamp(0, "00:00", "Introduction & Topic Scope", "Prerequisites and exam marking scheme"),
                VideoTimestamp(360, "06:00", "Core Algorithm / Theorem Proof", "In-depth derivation and pseudocode"),
                VideoTimestamp(1200, "20:00", "GATE PYQ Problem Walkthrough", "Solving standard previous year 2-mark question")
            )
        )
    }

    var newTime by remember { mutableStateOf("") }
    var newTopic by remember { mutableStateOf("") }
    var newNote by remember { mutableStateOf("") }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                Text(text = "Publish Video Lecture & Timers", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(text = "YouTube URL & Video Chapter Timestamps for Students", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Lecture Title (e.g. Dijkstra Shortest Paths)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = topicName,
                            onValueChange = { topicName = it },
                            label = { Text("Chapter / Subtopic (e.g. Graph Algorithms)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = videoUrl,
                            onValueChange = { videoUrl = it },
                            label = { Text("Video Link (YouTube URL or MP4 Link)") },
                            placeholder = { Text("https://www.youtube.com/watch?v=... or .mp4") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true
                        )
                    }
                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = duration,
                                onValueChange = { duration = it },
                                label = { Text("Duration") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = pdfTitle,
                                onValueChange = { pdfTitle = it },
                                label = { Text("Attached PDF Title") },
                                placeholder = { Text("Lecture_Notes.pdf") },
                                modifier = Modifier.weight(1.5f),
                                shape = RoundedCornerShape(10.dp),
                                singleLine = true
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Video Chapters / Timers",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Add timestamps indicating when each topic is explained",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    items(timestampList) { ts ->
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = BrandBlue,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = ts.timeDisplay,
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = ts.title, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    if (ts.notes.isNotBlank()) {
                                        Text(text = ts.notes, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                IconButton(
                                    onClick = { timestampList = timestampList.filter { it != ts } },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Filled.Close, null, modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }

                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                                .padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                OutlinedTextField(
                                    value = newTime,
                                    onValueChange = { newTime = it },
                                    label = { Text("Time (e.g. 12:45)") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = newTopic,
                                    onValueChange = { newTopic = it },
                                    label = { Text("Topic Title") },
                                    modifier = Modifier.weight(2f),
                                    singleLine = true
                                )
                            }
                            OutlinedTextField(
                                value = newNote,
                                onValueChange = { newNote = it },
                                label = { Text("Timestamp Notes & Explanations") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            Button(
                                onClick = {
                                    if (newTime.isNotBlank() && newTopic.isNotBlank()) {
                                        val parts = newTime.split(":")
                                        val secs = if (parts.size == 2) {
                                            (parts[0].toIntOrNull() ?: 0) * 60 + (parts[1].toIntOrNull() ?: 0)
                                        } else 0
                                        timestampList = timestampList + VideoTimestamp(
                                            timeSeconds = secs,
                                            timeDisplay = newTime,
                                            title = newTopic,
                                            notes = newNote
                                        )
                                        newTime = ""
                                        newTopic = ""
                                        newNote = ""
                                    }
                                },
                                modifier = Modifier.align(Alignment.End),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Filled.Add, null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add Chapter Mark", fontSize = 11.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                onUpload(
                                    title,
                                    subjectId,
                                    topicName,
                                    duration,
                                    videoUrl,
                                    pdfTitle,
                                    timestampList
                                )
                            }
                        },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Publish Lecture")
                    }
                }
            }
        }
    }
}

@Composable
private fun UploadPdfDialog(onDismiss: () -> Unit, onUpload: (String, String, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var subjectId by remember { mutableStateOf("s1") }
    var fileUrl by remember { mutableStateOf("mock_pdf_path.pdf") }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(text = "Upload Study Material", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("PDF Document Title") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = subjectId,
                        onValueChange = { subjectId = it },
                        label = { Text("Subject ID (s1: Math)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { if (title.isNotBlank()) onUpload(title, subjectId, fileUrl) },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Add to Library")
                    }
                }
            }
        }
    }
}
