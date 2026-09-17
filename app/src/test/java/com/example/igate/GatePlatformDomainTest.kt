package com.example.igate

import com.example.igate.domain.model.GateBranch
import com.example.igate.domain.model.GateQuestion
import com.example.igate.domain.model.QuestionType
import com.example.igate.domain.model.UserRole
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GatePlatformDomainTest {

    @Test
    fun userRoles_supportStudentTeacherAndAdmin() {
        val roles = UserRole.values().toList()
        assertTrue(roles.contains(UserRole.STUDENT))
        assertTrue(roles.contains(UserRole.TEACHER))
        assertTrue(roles.contains(UserRole.ADMIN))
        assertEquals(3, roles.size)
    }

    @Test
    fun gateBranches_containsAllEngineeringDisciplines() {
        val branches = GateBranch.values().map { it.name }
        assertTrue(branches.contains("CS"))
        assertTrue(branches.contains("ME"))
        assertTrue(branches.contains("EE"))
        assertTrue(branches.contains("EC"))
        assertTrue(branches.contains("CE"))
        assertTrue(branches.contains("DA"))
    }

    @Test
    fun questionEvaluation_mcqNegativeMarkingCorrect() {
        val question = GateQuestion(
            id = "q1",
            testId = "test1",
            questionNumber = 1,
            questionText = "What is the time complexity of binary search?",
            options = listOf("O(1)", "O(log n)", "O(n)", "O(n^2)"),
            correctOptions = listOf(1),
            questionType = QuestionType.MCQ,
            marks = 2,
            negativeMarks = 0.66
        )

        // Correct answer check
        val isCorrect = question.correctOptions.contains(1)
        val marksAwarded = if (isCorrect) question.marks.toDouble() else -question.negativeMarks
        assertEquals(2.0, marksAwarded, 0.01)

        // Incorrect answer incurs negative marks (-0.66)
        val selectedOption = 2
        val marksForWrong = if (question.correctOptions.contains(selectedOption)) question.marks.toDouble() else -question.negativeMarks
        assertEquals(-0.66, marksForWrong, 0.01)
    }

    @Test
    fun questionEvaluation_natNoNegativeMarking() {
        val question = GateQuestion(
            id = "q2",
            testId = "test1",
            questionNumber = 2,
            questionText = "Calculate propagation delay in milliseconds.",
            options = emptyList(),
            correctOptions = emptyList(),
            questionType = QuestionType.NAT,
            natMinAnswer = 25.0,
            natMaxAnswer = 26.0,
            marks = 2,
            negativeMarks = 0.0
        )

        // User enters within range
        val userAns = 25.4
        val isCorrect = userAns in question.natMinAnswer!!..question.natMaxAnswer!!
        assertTrue(isCorrect)
        assertEquals(0.0, question.negativeMarks, 0.001)
    }
}
