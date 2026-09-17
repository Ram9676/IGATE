package com.example.igate.domain.repository

import com.example.igate.domain.model.*
import kotlinx.coroutines.flow.Flow

interface IgateRepository {
    // Subjects & Branches
    fun getAllSubjects(): Flow<List<Subject>>
    fun getSubjectsForBranch(branch: GateBranch): Flow<List<Subject>>
    
    // Lessons & Notes
    fun getLessonsForSubject(subjectId: String): Flow<List<Lesson>>
    fun getRecentLessons(): Flow<List<Lesson>>
    fun getNotesForSubject(subjectId: String): Flow<List<Note>>
    fun getPinnedOrDownloadedNotes(): Flow<List<Note>>
    fun getAllInstitutions(): Flow<List<Institution>>
    
    suspend fun insertLesson(lesson: Lesson)
    suspend fun insertNote(note: Note)
    suspend fun syncWithCloud()
    suspend fun seedMockDataIfEmpty()

    // GATE Test Series & Practice (Unacademy / MADE EASY / Testbook)
    fun getAvailableTests(branch: GateBranch = GateBranch.CS): Flow<List<GateTest>>
    fun getQuestionsForTest(testId: String): Flow<List<GateQuestion>>
    suspend fun submitTestAttempt(result: TestAttemptResult)
    fun getRecentTestResults(): Flow<List<TestAttemptResult>>

    // Doubt Clearance (PW Drona / Educator)
    fun getDoubts(): Flow<List<Doubt>>
    suspend fun postDoubt(subjectName: String, title: String, detail: String, studentName: String)
    suspend fun answerDoubt(doubtId: String, answer: String, teacherName: String)

    // Batches & Management (Classplus / Teachmint)
    fun getBatches(): Flow<List<Batch>>
    suspend fun createBatch(batch: Batch)

    // User Profile & Role Management
    val currentUserProfile: Flow<UserProfile>
    suspend fun updateUserProfile(profile: UserProfile)
    suspend fun switchRole(role: UserRole)
}
