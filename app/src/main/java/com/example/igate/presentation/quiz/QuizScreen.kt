package com.example.igate.presentation.quiz

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.igate.IgateApplication
import com.example.igate.domain.model.GateQuestion
import com.example.igate.domain.model.QuestionType
import com.example.igate.domain.model.TestAttemptResult
import com.example.igate.presentation.IgateViewModelFactory
import com.example.igate.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    onBackClick: () -> Unit,
    viewModel: QuizViewModel = viewModel(
        factory = IgateViewModelFactory((LocalContext.current.applicationContext as IgateApplication).container.igateRepository)
    )
) {
    val isSubmitted by viewModel.isTestSubmitted.collectAsState()
    val testResult by viewModel.testResult.collectAsState()
    val questions by viewModel.questions.collectAsState()
    val currentIndex by viewModel.currentQuestionIndex.collectAsState()
    val selectedOptions by viewModel.selectedOptions.collectAsState()
    val natAnswers by viewModel.natAnswers.collectAsState()
    val markedForReview by viewModel.markedForReview.collectAsState()
    val remainingSeconds by viewModel.remainingSeconds.collectAsState()
    val testTitle by viewModel.testTitle.collectAsState()

    var showPaletteSheet by remember { mutableStateOf(false) }

    if (isSubmitted && testResult != null) {
        GateScorecardScreen(
            result = testResult!!,
            questions = questions,
            onBackClick = onBackClick
        )
        return
    }

    val currentQ = viewModel.currentQuestion

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = testTitle,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Text(
                            text = "GATE Computer Based Test (CBT)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.Filled.Close, contentDescription = "Close")
                    }
                },
                actions = {
                    // Remaining Timer Pill
                    val mins = remainingSeconds / 60
                    val secs = remainingSeconds % 60
                    val timerStr = String.format("%02d:%02d", mins, secs)

                    Surface(
                        modifier = Modifier.clip(RoundedCornerShape(16.dp)),
                        color = if (mins < 5) AcademicNegative.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Timer,
                                contentDescription = null,
                                tint = if (mins < 5) AcademicNegative else BrandBlue,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = timerStr,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace,
                                color = if (mins < 5) AcademicNegative else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Palette toggle button
                    IconButton(onClick = { showPaletteSheet = true }) {
                        Icon(
                            imageVector = Icons.Filled.GridView,
                            contentDescription = "Question Palette",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        bottomBar = {
            // Test Navigation & Action Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Mark for Review
                    OutlinedButton(
                        onClick = { viewModel.toggleMarkForReview() },
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = if (markedForReview.contains(currentIndex)) AcademicWarning else MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Text(
                            text = if (markedForReview.contains(currentIndex)) "Marked ✓" else "Review",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Clear Response
                    TextButton(
                        onClick = { viewModel.clearResponse() }
                    ) {
                        Text("Clear", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    // Save & Next or Submit
                    if (currentIndex == questions.size - 1) {
                        Button(
                            onClick = { viewModel.submitTest() },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AcademicSuccess)
                        ) {
                            Text("Submit Test", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    } else {
                        Button(
                            onClick = { viewModel.nextQuestion() },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Save & Next", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        if (currentQ == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Question Meta Header: Question number + Type badges + Marks
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Question ${currentIndex + 1} of ${questions.size}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        TypeBadge(label = currentQ.questionType.name, color = BrandBlue)
                        TypeBadge(label = "+${currentQ.marks} Marks", color = AcademicSuccess)
                        if (currentQ.negativeMarks > 0) {
                            TypeBadge(label = "-${currentQ.negativeMarks}", color = AcademicNegative)
                        }
                    }
                }
            }

            // Question Statement Card
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Text(
                        text = currentQ.questionText,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 26.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(20.dp)
                    )
                }
            }

            // Answer Input Section
            item {
                when (currentQ.questionType) {
                    QuestionType.MCQ -> {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            currentQ.options.forEachIndexed { optIndex, optionText ->
                                val isSelected = selectedOptions[currentIndex]?.contains(optIndex) == true
                                OptionCard(
                                    label = ('A' + optIndex).toString(),
                                    text = optionText,
                                    isSelected = isSelected,
                                    isMultiple = false,
                                    onClick = { viewModel.selectOption(optIndex) }
                                )
                            }
                        }
                    }
                    QuestionType.MSQ -> {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = "Notice: Multiple options may be correct. No negative marking.",
                                fontSize = 12.sp,
                                color = BrandBlue,
                                fontWeight = FontWeight.SemiBold
                            )
                            currentQ.options.forEachIndexed { optIndex, optionText ->
                                val isSelected = selectedOptions[currentIndex]?.contains(optIndex) == true
                                OptionCard(
                                    label = ('A' + optIndex).toString(),
                                    text = optionText,
                                    isSelected = isSelected,
                                    isMultiple = true,
                                    onClick = { viewModel.selectOption(optIndex) }
                                )
                            }
                        }
                    }
                    QuestionType.NAT -> {
                        val currentText = natAnswers[currentIndex] ?: ""
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = "Numerical Answer Type (NAT): Enter decimal or integer value.",
                                fontSize = 12.sp,
                                color = BrandBlue,
                                fontWeight = FontWeight.SemiBold
                            )
                            OutlinedTextField(
                                value = currentText,
                                onValueChange = { viewModel.setNatAnswer(it) },
                                label = { Text("Your Numerical Answer") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                singleLine = true
                            )
                        }
                    }
                }
            }
        }

        // Question Palette Modal Bottom Sheet
        if (showPaletteSheet) {
            ModalBottomSheet(
                onDismissRequest = { showPaletteSheet = false },
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Text(
                        text = "Question Navigation Palette",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(5),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(questions.size) { idx ->
                            val status = viewModel.getQuestionStatus(idx)
                            val isCurrent = idx == currentIndex

                            val bgColor = when (status) {
                                QuestionStatus.ANSWERED -> AcademicSuccess
                                QuestionStatus.MARKED_FOR_REVIEW -> AcademicWarning
                                QuestionStatus.ANSWERED_AND_MARKED -> Color(0xFF8B5CF6)
                                QuestionStatus.NOT_VISITED -> MaterialTheme.colorScheme.surfaceVariant
                            }

                            val textColor = if (status == QuestionStatus.NOT_VISITED) {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            } else Color.White

                            Box(
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(bgColor)
                                    .border(
                                        width = if (isCurrent) 2.dp else 0.dp,
                                        color = if (isCurrent) BrandBlue else Color.Transparent,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable {
                                        viewModel.jumpToQuestion(idx)
                                        showPaletteSheet = false
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = (idx + 1).toString(),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = textColor
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            showPaletteSheet = false
                            viewModel.submitTest()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AcademicSuccess)
                    ) {
                        Text("Submit Test Now", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun TypeBadge(label: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.12f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun OptionCard(
    label: String,
    text: String,
    isSelected: Boolean,
    isMultiple: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() },
        color = if (isSelected) BrandBlue.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) BrandBlue else MaterialTheme.colorScheme.outline
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(if (isMultiple) RoundedCornerShape(6.dp) else CircleShape)
                    .background(if (isSelected) BrandBlue else MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

// Authentic GATE Scorecard Screen (Strictly Academic - No XP / points)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GateScorecardScreen(
    result: TestAttemptResult,
    questions: List<GateQuestion>,
    onBackClick: () -> Unit
) {
    var showSolutions by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("GATE Scorecard & Analysis", fontWeight = FontWeight.Bold) },
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
                .padding(innerPadding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Score Summary Card
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = result.testTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Score Display
                        Text(
                            text = String.format("%.2f", result.score),
                            fontSize = 44.sp,
                            fontWeight = FontWeight.Black,
                            color = BrandBlue
                        )
                        Text(
                            text = "Out of ${result.maxScore.toInt()} Total Marks",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(20.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                        Spacer(modifier = Modifier.height(16.dp))

                        // Metric Trio
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            ScoreMetric(title = "Estimated AIR", value = "${result.percentileRank}%ile")
                            ScoreMetric(title = "GATE Score", value = "${result.estimatedGateScore}/1000")
                            ScoreMetric(title = "Accuracy", value = "${result.accuracyPercentage}%")
                        }
                    }
                }
            }

            // Questions Breakdown
            item {
                Text(
                    text = "Attempt Breakdown",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    BreakdownCard(
                        count = result.correctCount,
                        label = "Correct",
                        color = AcademicSuccess,
                        modifier = Modifier.weight(1f)
                    )
                    BreakdownCard(
                        count = result.incorrectCount,
                        label = "Incorrect",
                        color = AcademicNegative,
                        modifier = Modifier.weight(1f)
                    )
                    BreakdownCard(
                        count = result.unattemptedCount,
                        label = "Unattempted",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Action: View Step-by-Step Solutions
            item {
                Button(
                    onClick = { showSolutions = !showSolutions },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(if (showSolutions) "Hide Detailed Solutions" else "View Step-by-Step Solutions")
                }
            }

            // Detailed Solutions List
            if (showSolutions) {
                items(questions.size) { index ->
                    val q = questions[index]
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Q${index + 1}: ${q.questionText}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Explanation:",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                color = BrandBlue
                            )
                            Text(
                                text = q.explanation,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}

@Composable
private fun ScoreMetric(title: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun BreakdownCard(count: Int, label: String, color: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = color.copy(alpha = 0.1f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = count.toString(), fontSize = 20.sp, fontWeight = FontWeight.Black, color = color)
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = color)
        }
    }
}
