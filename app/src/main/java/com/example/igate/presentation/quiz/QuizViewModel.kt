package com.example.igate.presentation.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.igate.domain.model.*
import com.example.igate.domain.repository.IgateRepository
import com.example.igate.data.analytics.AnalyticsTracker
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class QuestionStatus {
    NOT_VISITED,
    ANSWERED,
    MARKED_FOR_REVIEW,
    ANSWERED_AND_MARKED
}

class QuizViewModel(
    private val repository: IgateRepository? = null
) : ViewModel() {

    private val _testTitle = MutableStateFlow("All India Open GATE Mock Test")
    val testTitle: StateFlow<String> = _testTitle.asStateFlow()

    private val _questions = MutableStateFlow<List<GateQuestion>>(emptyList())
    val questions: StateFlow<List<GateQuestion>> = _questions.asStateFlow()

    private val _currentQuestionIndex = MutableStateFlow(0)
    val currentQuestionIndex: StateFlow<Int> = _currentQuestionIndex.asStateFlow()

    // MCQ/MSQ selected options: QuestionIndex -> Set of option indices
    private val _selectedOptions = MutableStateFlow<Map<Int, Set<Int>>>(emptyMap())
    val selectedOptions: StateFlow<Map<Int, Set<Int>>> = _selectedOptions.asStateFlow()

    // NAT input: QuestionIndex -> String text
    private val _natAnswers = MutableStateFlow<Map<Int, String>>(emptyMap())
    val natAnswers: StateFlow<Map<Int, String>> = _natAnswers.asStateFlow()

    // Marked for review flag
    private val _markedForReview = MutableStateFlow<Set<Int>>(emptySet())
    val markedForReview: StateFlow<Set<Int>> = _markedForReview.asStateFlow()

    // Timer (in seconds)
    private val _remainingSeconds = MutableStateFlow(30 * 60)
    val remainingSeconds: StateFlow<Int> = _remainingSeconds.asStateFlow()
    private var timerJob: Job? = null

    private val _isTestSubmitted = MutableStateFlow(false)
    val isTestSubmitted: StateFlow<Boolean> = _isTestSubmitted.asStateFlow()

    private val _testResult = MutableStateFlow<TestAttemptResult?>(null)
    val testResult: StateFlow<TestAttemptResult?> = _testResult.asStateFlow()

    init {
        loadTest("t1")
        startTimer()
    }

    fun loadTest(testId: String) {
        AnalyticsTracker.logTestStarted(testId, "GATE CS")
        viewModelScope.launch {
            if (repository != null) {
                repository.getQuestionsForTest(testId).collect { list ->
                    _questions.value = list
                }
            } else {
                _questions.value = fallbackQuestions()
            }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_remainingSeconds.value > 0 && !_isTestSubmitted.value) {
                delay(1000)
                _remainingSeconds.value -= 1
            }
            if (_remainingSeconds.value <= 0 && !_isTestSubmitted.value) {
                submitTest()
            }
        }
    }

    val currentQuestion: GateQuestion?
        get() = _questions.value.getOrNull(_currentQuestionIndex.value)

    fun selectOption(optionIndex: Int) {
        val q = currentQuestion ?: return
        val currentMap = _selectedOptions.value.toMutableMap()
        val currentSet = currentMap[_currentQuestionIndex.value]?.toMutableSet() ?: mutableSetOf()

        if (q.questionType == QuestionType.MSQ) {
            if (currentSet.contains(optionIndex)) {
                currentSet.remove(optionIndex)
            } else {
                currentSet.add(optionIndex)
            }
            currentMap[_currentQuestionIndex.value] = currentSet
        } else {
            // Single choice MCQ
            currentMap[_currentQuestionIndex.value] = setOf(optionIndex)
        }
        _selectedOptions.value = currentMap
    }

    fun setNatAnswer(answer: String) {
        val current = _natAnswers.value.toMutableMap()
        current[_currentQuestionIndex.value] = answer
        _natAnswers.value = current
    }

    fun toggleMarkForReview() {
        val current = _markedForReview.value.toMutableSet()
        val idx = _currentQuestionIndex.value
        if (current.contains(idx)) {
            current.remove(idx)
        } else {
            current.add(idx)
        }
        _markedForReview.value = current
    }

    fun clearResponse() {
        val idx = _currentQuestionIndex.value
        val opt = _selectedOptions.value.toMutableMap()
        opt.remove(idx)
        _selectedOptions.value = opt

        val nat = _natAnswers.value.toMutableMap()
        nat.remove(idx)
        _natAnswers.value = nat
    }

    fun jumpToQuestion(index: Int) {
        if (index in _questions.value.indices) {
            _currentQuestionIndex.value = index
        }
    }

    fun nextQuestion() {
        if (_currentQuestionIndex.value < _questions.value.size - 1) {
            _currentQuestionIndex.value += 1
        }
    }

    fun previousQuestion() {
        if (_currentQuestionIndex.value > 0) {
            _currentQuestionIndex.value -= 1
        }
    }

    fun getQuestionStatus(index: Int): QuestionStatus {
        val hasOptionAnswer = (_selectedOptions.value[index]?.isNotEmpty() == true)
        val hasNatAnswer = (_natAnswers.value[index]?.isNotBlank() == true)
        val isAnswered = hasOptionAnswer || hasNatAnswer
        val isMarked = _markedForReview.value.contains(index)

        return when {
            isAnswered && isMarked -> QuestionStatus.ANSWERED_AND_MARKED
            isAnswered -> QuestionStatus.ANSWERED
            isMarked -> QuestionStatus.MARKED_FOR_REVIEW
            else -> QuestionStatus.NOT_VISITED
        }
    }

    fun submitTest() {
        timerJob?.cancel()
        val qList = _questions.value
        var score = 0.0
        var correctCount = 0
        var incorrectCount = 0
        var unattemptedCount = 0
        var maxPossibleMarks = 0.0

        qList.forEachIndexed { index, question ->
            maxPossibleMarks += question.marks
            when (question.questionType) {
                QuestionType.MCQ -> {
                    val userSelected = _selectedOptions.value[index]?.firstOrNull()
                    if (userSelected == null) {
                        unattemptedCount++
                    } else if (question.correctOptions.contains(userSelected)) {
                        score += question.marks
                        correctCount++
                    } else {
                        score -= question.negativeMarks
                        incorrectCount++
                    }
                }
                QuestionType.MSQ -> {
                    val userSelected = _selectedOptions.value[index] ?: emptySet()
                    if (userSelected.isEmpty()) {
                        unattemptedCount++
                    } else if (userSelected == question.correctOptions.toSet()) {
                        score += question.marks
                        correctCount++
                    } else {
                        // MSQ has 0 negative marks in GATE
                        incorrectCount++
                    }
                }
                QuestionType.NAT -> {
                    val userVal = _natAnswers.value[index]?.toDoubleOrNull()
                    if (userVal == null) {
                        unattemptedCount++
                    } else {
                        val min = question.natMinAnswer ?: Double.MIN_VALUE
                        val max = question.natMaxAnswer ?: Double.MAX_VALUE
                        if (userVal in min..max) {
                            score += question.marks
                            correctCount++
                        } else {
                            // NAT has 0 negative marks in GATE
                            incorrectCount++
                        }
                    }
                }
            }
        }

        // Clamp score to >= 0 for realism
        val finalScore = maxOf(0.0, String.format("%.2f", score).toDouble())
        val attemptedCount = correctCount + incorrectCount
        val accuracy = if (attemptedCount > 0) (correctCount.toDouble() / attemptedCount) * 100.0 else 0.0
        val percentile = 90.0 + (finalScore / maxPossibleMarks) * 9.9
        val estimatedGate = (finalScore / maxPossibleMarks * 850 + 150).toInt()

        val result = TestAttemptResult(
            testId = "t1",
            testTitle = _testTitle.value,
            score = finalScore,
            maxScore = maxPossibleMarks,
            correctCount = correctCount,
            incorrectCount = incorrectCount,
            unattemptedCount = unattemptedCount,
            accuracyPercentage = String.format("%.1f", accuracy).toDouble(),
            percentileRank = String.format("%.1f", percentile).toDouble(),
            estimatedGateScore = estimatedGate
        )

        _testResult.value = result
        _isTestSubmitted.value = true
        AnalyticsTracker.logTestCompleted(result.testId, result.score, result.accuracyPercentage, result.percentileRank)

        viewModelScope.launch {
            repository?.submitTestAttempt(result)
        }
    }

    private fun fallbackQuestions(): List<GateQuestion> {
        return listOf(
            GateQuestion(
                id = "q1",
                testId = "t1",
                questionNumber = 1,
                questionText = "Consider a min-heap containing n elements. What is the worst-case time complexity to find the maximum element in this min-heap?",
                options = listOf("Θ(1)", "Θ(log n)", "Θ(n)", "Θ(n log n)"),
                correctOptions = listOf(2),
                questionType = QuestionType.MCQ,
                marks = 1,
                negativeMarks = 0.33,
                explanation = "In a min-heap, the root contains the minimum element. The maximum element must reside in one of the leaf nodes, which comprise ⌈n/2⌉ nodes. Searching among them requires scanning Θ(n) elements."
            ),
            GateQuestion(
                id = "q2",
                testId = "t1",
                questionNumber = 2,
                questionText = "Which of the following problems are DECIDABLE for Context-Free Languages (CFL)? (Select all that apply)",
                options = listOf(
                    "Emptiness problem (Is L(G) = ∅?)",
                    "Finiteness problem (Is L(G) finite?)",
                    "Equivalence problem (Is L(G1) = L(G2)?)",
                    "Universality problem (Is L(G) = Σ*?)"
                ),
                correctOptions = listOf(0, 1),
                questionType = QuestionType.MSQ,
                marks = 2,
                negativeMarks = 0.0,
                explanation = "For Context-Free Languages, Emptiness, Membership, and Finiteness are decidable. Equivalence, Universality, and Disjointness are undecidable."
            ),
            GateQuestion(
                id = "q3",
                testId = "t1",
                questionNumber = 3,
                questionText = "A cache memory has 64-byte blocks and is 4-way set-associative. The total size of the cache data is 32 KB. The main memory is byte-addressable with a 32-bit address. What is the number of bits in the TAG field?",
                options = emptyList(),
                natMinAnswer = 19.0,
                natMaxAnswer = 19.0,
                questionType = QuestionType.NAT,
                marks = 2,
                negativeMarks = 0.0,
                explanation = "Block size = 64 B = 2^6 B (Offset = 6 bits). Cache size = 32 KB = 2^15 B. Number of blocks = 2^15 / 2^6 = 2^9 = 512. Since it is 4-way set-associative, Number of sets = 512 / 4 = 128 = 2^7 (Index = 7 bits). Tag bits = 32 - (7 + 6) = 19 bits."
            )
        )
    }
}
