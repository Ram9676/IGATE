package com.example.igate.domain.model

enum class UserRole {
    STUDENT,
    TEACHER,
    ADMIN
}

enum class GateBranch(val code: String, val displayName: String) {
    CS("CS", "Computer Science & IT"),
    ME("ME", "Mechanical Engineering"),
    EE("EE", "Electrical Engineering"),
    EC("EC", "Electronics & Comm."),
    CE("CE", "Civil Engineering"),
    DA("DA", "Data Science & AI")
}

data class UserProfile(
    val id: String = "u1",
    val name: String = "Rachel Green",
    val email: String = "rachel@igate.com",
    val role: UserRole = UserRole.STUDENT,
    val dob: String = "18 Feb 2002",
    val branch: GateBranch = GateBranch.CS,
    val targetYear: String = "GATE 2027",
    val enrolledBatches: List<String> = listOf("GATE CS Alpha Batch")
)

data class Subject(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val branch: GateBranch = GateBranch.CS,
    val totalLessons: Int = 12,
    val completedLessons: Int = 8,
    val imageUrl: String? = null
)

data class VideoTimestamp(
    val timeSeconds: Int = 0,
    val timeDisplay: String = "00:00",
    val title: String = "",
    val notes: String = ""
)

data class Lesson(
    val id: String = "",
    val subjectId: String = "",
    val topicId: String = "",
    val topicName: String = "",
    val title: String = "",
    val description: String = "",
    val videoUrl: String = "",
    val durationString: String = "45 mins",
    val educatorName: String = "Prof. Arvind Rao (IIT Bombay)",
    val isCompleted: Boolean = false,
    val pdfAttachmentTitle: String? = "Lecture Notes & Formulae.pdf",
    val pdfAssetFileName: String = "mock_pdf_path.pdf",
    val timestamps: List<VideoTimestamp> = emptyList()
)

enum class NoteType {
    PDF, TEXT
}

data class Note(
    val id: String = "",
    val subjectId: String = "",
    val title: String = "",
    val description: String = "",
    val type: NoteType = NoteType.PDF,
    val fileUrlOrContent: String = "",
    val isDownloaded: Boolean = false,
    val sizeKb: Int = 1420
)

data class Institution(
    val id: String = "",
    val name: String = "",
    val logoUrl: String? = null,
    val description: String = ""
)

enum class QuestionType {
    MCQ, // Multiple Choice Question (-0.33 for 1 mark, -0.66 for 2 marks)
    MSQ, // Multiple Select Question (No negative marking, all correct options needed)
    NAT  // Numerical Answer Type (Virtual keyboard input)
}

data class GateQuestion(
    val id: String = "",
    val testId: String = "",
    val questionNumber: Int = 1,
    val questionText: String = "",
    val options: List<String> = emptyList(),
    val correctOptions: List<Int> = listOf(0), // indices for MCQ/MSQ
    val natMinAnswer: Double? = null,
    val natMaxAnswer: Double? = null,
    val questionType: QuestionType = QuestionType.MCQ,
    val marks: Int = 1,
    val negativeMarks: Double = 0.33,
    val explanation: String = ""
)

data class GateTest(
    val id: String = "",
    val title: String = "",
    val subjectName: String = "",
    val branch: GateBranch = GateBranch.CS,
    val durationMinutes: Int = 30,
    val totalMarks: Int = 25,
    val totalQuestions: Int = 10,
    val isMockExam: Boolean = false,
    val testType: String = "Subject Test" // "Full Mock", "Topic Test", "PYQ 2024"
)

data class TestAttemptResult(
    val testId: String = "",
    val testTitle: String = "",
    val score: Double = 0.0,
    val maxScore: Double = 0.0,
    val correctCount: Int = 0,
    val incorrectCount: Int = 0,
    val unattemptedCount: Int = 0,
    val accuracyPercentage: Double = 0.0,
    val percentileRank: Double = 0.0,
    val estimatedGateScore: Int = 0
)

data class Doubt(
    val id: String = "",
    val studentName: String = "",
    val subjectName: String = "",
    val questionTitle: String = "",
    val questionDetail: String = "",
    val answerText: String? = null,
    val answeredByTeacher: String? = null,
    val isResolved: Boolean = false,
    val timestamp: String = "2 hours ago"
)

data class Batch(
    val id: String = "",
    val name: String = "",
    val branch: GateBranch = GateBranch.CS,
    val educatorName: String = "",
    val studentCount: Int = 0,
    val scheduleTime: String = "",
    val syllabusProgressPercentage: Int = 65
)

enum class LiveClassStatus { SCHEDULED, LIVE, ENDED }

data class LiveClass(
    val id: String = "",
    val title: String = "",
    val educatorName: String = "",
    val subjectName: String = "",
    val scheduledAt: String = "Today, 6:00 PM", // Display string e.g. "Today, 6:00 PM"
    val durationMinutes: Int = 90,
    val status: LiveClassStatus = LiveClassStatus.SCHEDULED,
    val joinUrl: String = "",
    val recordingUrl: String = "",
    val watchersCount: Int = 0
)

enum class AssignmentStatus { PENDING, SUBMITTED, GRADED }

data class Assignment(
    val id: String = "",
    val title: String = "",
    val subjectId: String = "",
    val subjectName: String = "",
    val dueDate: String = "",
    val totalMarks: Int = 10,
    val obtainedMarks: Int? = null,
    val status: AssignmentStatus = AssignmentStatus.PENDING,
    val description: String = ""
)

enum class AnnouncementPriority { HIGH, NORMAL }
enum class AnnouncementTarget { ALL, STUDENTS, TEACHERS }

data class Announcement(
    val id: String = "",
    val title: String = "",
    val body: String = "",
    val priority: AnnouncementPriority = AnnouncementPriority.NORMAL,
    val target: AnnouncementTarget = AnnouncementTarget.ALL,
    val postedAt: String = "Just now",
    val postedBy: String = "IGATE Admin"
)

data class PYQPaper(
    val id: String = "",
    val year: Int = 2024,
    val branch: GateBranch = GateBranch.CS,
    val totalQuestions: Int = 65,
    val durationMinutes: Int = 180,
    val sections: List<String> = listOf("General Aptitude", "Engineering Mathematics", "Core Subject")
)

data class CourseProgress(
    val lessonId: String = "",
    val lessonTitle: String = "",
    val percentage: Int = 0,
    val lastWatchedAt: String = "Yesterday"
)

enum class ChatMessageSender { USER, AI }

data class ChatMessage(
    val id: String = "",
    val text: String = "",
    val sender: ChatMessageSender = ChatMessageSender.USER,
    val timestamp: String = ""
)

data class SubscriptionPlan(
    val id: String = "",
    val name: String = "",
    val priceMonthly: Int = 0,
    val features: List<String> = emptyList(),
    val isPopular: Boolean = false
)

data class StudentRosterItem(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val batchName: String = "",
    val branch: GateBranch = GateBranch.CS,
    val attendancePercent: Int = 0,
    val avgAccuracy: Int = 0,
    val pendingDoubts: Int = 0,
    val lastActive: String = "Today"
)

data class MockTestHistoryItem(
    val testId: String = "",
    val testTitle: String = "",
    val date: String = "",
    val score: Double = 0.0,
    val maxScore: Double = 0.0,
    val rank: Int = 0,
    val totalParticipants: Int = 0
)
