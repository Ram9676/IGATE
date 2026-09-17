package com.example.igate.data.repository

import com.example.igate.domain.model.*
import com.example.igate.domain.repository.IgateRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.util.Log

class FirestoreRepositoryImpl : IgateRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Internal fallback states for when the user is not signed in or not present in DB
    private val defaultProfile = UserProfile(
        id = "demo_student",
        name = "Demo Student",
        email = "demo@igate.com",
        role = UserRole.STUDENT,
        dob = "01 Jan 2000",
        branch = GateBranch.CS,
        targetYear = "GATE 2025",
        enrolledBatches = emptyList()
    )

    override val currentUserProfile: Flow<UserProfile> = callbackFlow {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val listener = db.collection("users").document(currentUser.uid)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        Log.e("FirestoreRepo", "Error fetching user profile", e)
                        trySend(defaultProfile)
                        return@addSnapshotListener
                    }
                    if (snapshot != null && snapshot.exists()) {
                        val profile = snapshot.toObject(UserProfile::class.java)
                        if (profile != null) {
                            trySend(profile)
                        } else {
                            trySend(defaultProfile)
                        }
                    } else {
                        // Document doesn't exist, maybe they just signed up via Google
                        trySend(defaultProfile)
                    }
                }
            awaitClose { listener.remove() }
        } else {
            // Not signed in, send default
            trySend(defaultProfile)
            awaitClose { }
        }
    }

    override suspend fun updateUserProfile(profile: UserProfile) {
        val user = auth.currentUser ?: return
        db.collection("users").document(user.uid).set(profile).await()
    }

    override suspend fun switchRole(role: UserRole) {
        val user = auth.currentUser ?: return
        db.collection("users").document(user.uid).update("role", role.name).await()
    }

    override fun getAllSubjects(): Flow<List<Subject>> = callbackFlow {
        val listener = db.collection("subjects").addSnapshotListener { snapshot, e ->
            if (e != null) return@addSnapshotListener
            val list = snapshot?.documents?.mapNotNull { it.toObject(Subject::class.java) } ?: emptyList()
            trySend(list)
        }
        awaitClose { listener.remove() }
    }

    override fun getSubjectsForBranch(branch: GateBranch): Flow<List<Subject>> = callbackFlow {
        val listener = db.collection("subjects").whereEqualTo("branch", branch.name).addSnapshotListener { snapshot, e ->
            if (e != null) return@addSnapshotListener
            val list = snapshot?.documents?.mapNotNull { it.toObject(Subject::class.java) } ?: emptyList()
            trySend(list)
        }
        awaitClose { listener.remove() }
    }

    override fun getLessonsForSubject(subjectId: String): Flow<List<Lesson>> = callbackFlow {
        val listener = db.collection("lessons").whereEqualTo("subjectId", subjectId).addSnapshotListener { snapshot, e ->
            if (e != null) return@addSnapshotListener
            val list = snapshot?.documents?.mapNotNull { it.toObject(Lesson::class.java) } ?: emptyList()
            trySend(list)
        }
        awaitClose { listener.remove() }
    }

    override fun getRecentLessons(): Flow<List<Lesson>> = callbackFlow {
        val listener = db.collection("lessons").addSnapshotListener { snapshot, e ->
            if (e != null) return@addSnapshotListener
            val list = snapshot?.documents?.mapNotNull { it.toObject(Lesson::class.java) } ?: emptyList()
            trySend(list)
        }
        awaitClose { listener.remove() }
    }

    override fun getNotesForSubject(subjectId: String): Flow<List<Note>> = callbackFlow {
        val listener = db.collection("notes").whereEqualTo("subjectId", subjectId).addSnapshotListener { snapshot, e ->
            if (e != null) return@addSnapshotListener
            val list = snapshot?.documents?.mapNotNull { it.toObject(Note::class.java) } ?: emptyList()
            trySend(list)
        }
        awaitClose { listener.remove() }
    }

    override fun getPinnedOrDownloadedNotes(): Flow<List<Note>> = callbackFlow {
        val listener = db.collection("notes").whereEqualTo("isPinned", true).addSnapshotListener { snapshot, e ->
            if (e != null) return@addSnapshotListener
            val list = snapshot?.documents?.mapNotNull { it.toObject(Note::class.java) } ?: emptyList()
            trySend(list)
        }
        awaitClose { listener.remove() }
    }

    override fun getAllInstitutions(): Flow<List<Institution>> = callbackFlow {
        val listener = db.collection("institutions").addSnapshotListener { snapshot, e ->
            if (e != null) return@addSnapshotListener
            val list = snapshot?.documents?.mapNotNull { it.toObject(Institution::class.java) } ?: emptyList()
            trySend(list)
        }
        awaitClose { listener.remove() }
    }

    override suspend fun insertLesson(lesson: Lesson) {
        db.collection("lessons").document(lesson.id).set(lesson).await()
    }

    override suspend fun insertNote(note: Note) {
        db.collection("notes").document(note.id).set(note).await()
    }

    override suspend fun syncWithCloud() {
        // Not strictly needed since we are directly reading from cloud now
    }

    override fun getAvailableTests(branch: GateBranch): Flow<List<GateTest>> = callbackFlow {
        val listener = db.collection("tests").whereEqualTo("branch", branch.name).addSnapshotListener { snapshot, e ->
            if (e != null) return@addSnapshotListener
            val list = snapshot?.documents?.mapNotNull { it.toObject(GateTest::class.java) } ?: emptyList()
            trySend(list)
        }
        awaitClose { listener.remove() }
    }

    override fun getQuestionsForTest(testId: String): Flow<List<GateQuestion>> = callbackFlow {
        val listener = db.collection("questions").whereEqualTo("testId", testId).addSnapshotListener { snapshot, e ->
            if (e != null) return@addSnapshotListener
            val list = snapshot?.documents?.mapNotNull { it.toObject(GateQuestion::class.java) } ?: emptyList()
            trySend(list)
        }
        awaitClose { listener.remove() }
    }

    override suspend fun submitTestAttempt(result: TestAttemptResult) {
        db.collection("test_results").add(result).await()
    }

    override fun getRecentTestResults(): Flow<List<TestAttemptResult>> = callbackFlow {
        val user = auth.currentUser
        if (user != null) {
            val listener = db.collection("test_results").whereEqualTo("userId", user.uid).addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener
                val list = snapshot?.documents?.mapNotNull { it.toObject(TestAttemptResult::class.java) } ?: emptyList()
                trySend(list)
            }
            awaitClose { listener.remove() }
        } else {
            trySend(emptyList())
            awaitClose { }
        }
    }

    override fun getDoubts(): Flow<List<Doubt>> = callbackFlow {
        val listener = db.collection("doubts").addSnapshotListener { snapshot, e ->
            if (e != null) return@addSnapshotListener
            val list = snapshot?.documents?.mapNotNull { it.toObject(Doubt::class.java) } ?: emptyList()
            trySend(list)
        }
        awaitClose { listener.remove() }
    }

    override suspend fun postDoubt(subjectName: String, title: String, detail: String, studentName: String) {
        val newDoubt = Doubt(
            id = "d_${System.currentTimeMillis()}",
            studentName = studentName,
            subjectName = subjectName,
            questionTitle = title,
            questionDetail = detail,
            isResolved = false,
            timestamp = "Just now"
        )
        db.collection("doubts").document(newDoubt.id).set(newDoubt).await()
    }

    override suspend fun answerDoubt(doubtId: String, answer: String, teacherName: String) {
        db.collection("doubts").document(doubtId).update(
            "answerText", answer,
            "answeredByTeacher", teacherName,
            "isResolved", true
        ).await()
    }

    override fun getBatches(): Flow<List<Batch>> = callbackFlow {
        val listener = db.collection("batches").addSnapshotListener { snapshot, e ->
            if (e != null) return@addSnapshotListener
            val list = snapshot?.documents?.mapNotNull { it.toObject(Batch::class.java) } ?: emptyList()
            trySend(list)
        }
        awaitClose { listener.remove() }
    }

    override suspend fun createBatch(batch: Batch) {
        db.collection("batches").document(batch.id).set(batch).await()
    }

    override suspend fun seedMockDataIfEmpty() {
        // To prevent over-seeding, we only seed if the subjects collection is empty
        val subjectsCount = db.collection("subjects").get().await().size()
        if (subjectsCount == 0) {
            // Seed Subjects covering standard GATE syllabus
            val subjects = listOf(
                Subject(id="s1", name="Engineering Mathematics", description="Linear Algebra, Calculus, Probability & Statistics", branch=GateBranch.CS),
                Subject(id="s2", name="Data Structures & Algorithms", description="Asymptotic Analysis, Trees, Graphs, Dynamic Programming", branch=GateBranch.CS),
                Subject(id="s3", name="Operating Systems", description="Process Management, Synchronization, Virtual Memory, File Systems", branch=GateBranch.CS),
                Subject(id="s4", name="Database Management Systems", description="Relational Algebra, SQL, Normalization, Transactions & Concurrency", branch=GateBranch.CS),
                Subject(id="s5", name="Theory of Computation", description="Regular Expressions, Finite Automata, Context-Free Grammars, Turing Machines", branch=GateBranch.CS),
                Subject(id="s6", name="Computer Networks", description="OSI & TCP/IP, IP Addressing, Routing Algorithms, TCP/UDP Flow Control", branch=GateBranch.CS)
            )
            for (sub in subjects) db.collection("subjects").document(sub.id).set(sub)

            // Seed Lessons
            val lessons = listOf(
                Lesson(id="l1", subjectId="s1", title="Eigenvalues and Cayley-Hamilton Theorem", description="Comprehensive GATE shortcut methods", videoUrl="https://example.com/math1.mp4", durationString="48 mins", isCompleted=true),
                Lesson(id="l2", subjectId="s1", title="Probability Distributions & Bayes Theorem", description="Conditional probability with GATE PYQs", videoUrl="https://example.com/math2.mp4", durationString="52 mins", isCompleted=false),
                Lesson(id="l3", subjectId="s2", title="Graph Algorithms: Dijkstra & Floyd-Warshall", description="Time complexity analysis & shortest paths", videoUrl="https://example.com/dsa1.mp4", durationString="55 mins", isCompleted=true),
                Lesson(id="l4", subjectId="s2", title="Dynamic Programming: Matrix Chain Multiplication", description="Optimal substructure and recurrence formulation", videoUrl="https://example.com/dsa2.mp4", durationString="62 mins", isCompleted=false),
                Lesson(id="l5", subjectId="s3", title="Deadlock Prevention, Avoidance & Banker's Algorithm", description="Resource allocation graphs & safe sequences", videoUrl="https://example.com/os1.mp4", durationString="45 mins", isCompleted=false),
                Lesson(id="l6", subjectId="s4", title="Lossless Join Decomposition & Dependency Preservation", description="Testing for BCNF and 3NF minimal covers", videoUrl="https://example.com/dbms1.mp4", durationString="50 mins", isCompleted=false),
                Lesson(id="l7", subjectId="s5", title="Decidability and Halting Problem", description="Reduction techniques and Turing recognizability", videoUrl="https://example.com/toc1.mp4", durationString="40 mins", isCompleted=false),
                Lesson(id="l8", subjectId="s6", title="Subnetting, CIDR & Variable Length Subnet Masks", description="IPv4 packet fragmentation and header calculation", videoUrl="https://example.com/cn1.mp4", durationString="58 mins", isCompleted=false)
            )
            for (les in lessons) db.collection("lessons").document(les.id).set(les)

            // Seed High-Yield Notes & Formula Sheets
            val notes = listOf(
                Note(id="n1", subjectId="s1", title="Engineering Mathematics Formula Handbook", description="Calculus, Linear Algebra, Vector Calculus quick sheets", type=NoteType.PDF, fileUrlOrContent="mock_pdf_path.pdf", isDownloaded=true),
                Note(id="n2", subjectId="s2", title="Master Algorithm Recurrences & Master Theorem", description="Quick reference tables for time complexity bounds", type=NoteType.PDF, fileUrlOrContent="mock_pdf_path2.pdf", isDownloaded=true),
                Note(id="n3", subjectId="s3", title="OS Synchronization & Classical Problems Notes", description="Dining philosophers, Producer-Consumer semaphore code", type=NoteType.TEXT, fileUrlOrContent="Semaphore and Mutex implementations in C", isDownloaded=false),
                Note(id="n4", subjectId="s4", title="GATE DBMS SQL Queries & Normal Forms Summary", description="Comprehensive 1NF to BCNF rules cheat sheet", type=NoteType.PDF, fileUrlOrContent="mock_pdf_path.pdf", isDownloaded=true),
                Note(id="n5", subjectId="s5", title="Closure Properties of Formal Languages Table", description="Union, intersection, complementation closure summary", type=NoteType.PDF, fileUrlOrContent="mock_pdf_path2.pdf", isDownloaded=false)
            )
            for (note in notes) db.collection("notes").document(note.id).set(note)

            // Seed Institutions
            val institutions = listOf(
                Institution("i1", "IGATE Central Campus", null, "Official GATE CS/IT and Mechanical Preparation Center"),
                Institution("i2", "IIT Madras Research Park Hub", null, "Joint Faculty Mentorship and Advanced Mock Series"),
                Institution("i3", "IISc Bangalore Prep Wing", null, "Special Focus on Data Science & AI Curriculum")
            )
            for (inst in institutions) db.collection("institutions").document(inst.id).set(inst)

            // Initialize Mock Tests
            val tests = listOf(
                GateTest("t1", "All India Open GATE Mock 1", "Comprehensive Syllabus", GateBranch.CS, 60, 50, 25, true, "Full Mock"),
                GateTest("t2", "Algorithms & Data Structures Speed Test", "Data Structures & Algorithms", GateBranch.CS, 30, 25, 10, false, "Subject Test"),
                GateTest("t3", "Engineering Mathematics & Aptitude Drill", "Engineering Mathematics", GateBranch.CS, 35, 30, 12, false, "Subject Test"),
                GateTest("t4", "Operating Systems Core Concepts & PYQs", "Operating Systems", GateBranch.CS, 40, 30, 15, false, "Topic Test"),
                GateTest("t5", "GATE 2024 Official Paper Simulation", "All Subjects", GateBranch.CS, 180, 100, 65, true, "PYQ 2024")
            )
            for (test in tests) db.collection("tests").document(test.id).set(test)

            // Initialize Seed Doubts
            val seedDoubts = listOf(
                Doubt(
                    id = "d1",
                    studentName = "Rahul Verma",
                    subjectName = "Data Structures & Algorithms",
                    questionTitle = "Is Dijkstra guaranteed to fail with negative edges or just negative cycles?",
                    questionDetail = "In a directed graph with a single negative edge but no negative weight cycle, can Dijkstra's algorithm produce an incorrect shortest path?",
                    answerText = "Dijkstra's greedy choice assumes that once a vertex is marked visited/extracted from priority queue, its distance is final. With negative edges, this assumption fails even without cycles. Bellman-Ford must be used.",
                    answeredByTeacher = "Prof. Arvind Rao (IIT Bombay)",
                    isResolved = true,
                    timestamp = "1 hour ago"
                ),
                Doubt(
                    id = "d2",
                    studentName = "Rachel Green",
                    subjectName = "Operating Systems",
                    questionTitle = "Safe state vs Deadlock-free state distinction?",
                    questionDetail = "If a system is in an unsafe state, does that mean deadlock has already occurred?",
                    answerText = "No! An unsafe state is not necessarily deadlocked. It only means the system cannot guarantee avoidance of deadlock if processes request maximum resources.",
                    answeredByTeacher = "Dr. Meenakshi Sundaram",
                    isResolved = true,
                    timestamp = "3 hours ago"
                ),
                Doubt(
                    id = "d3",
                    studentName = "Aditya K.",
                    subjectName = "Database Management Systems",
                    questionTitle = "Testing 3NF without checking all candidate keys?",
                    questionDetail = "For X -> Y, if Y is a prime attribute, do we still need to compute the closure of X?",
                    answerText = null,
                    answeredByTeacher = null,
                    isResolved = false,
                    timestamp = "15 mins ago"
                )
            )
            for (doubt in seedDoubts) db.collection("doubts").document(doubt.id).set(doubt)

            // Initialize Seed Batches
            val seedBatches = listOf(
                Batch("b1", "GATE 2027 CS - Alpha Star Batch", GateBranch.CS, "Prof. Arvind Rao & Team", 420, "Mon-Fri 07:00 AM - 09:30 AM", 68),
                Batch("b2", "Weekend Comprehensive Batch - CS/IT", GateBranch.CS, "Dr. Meenakshi Sundaram", 280, "Sat-Sun 09:00 AM - 02:00 PM", 45),
                Batch("b3", "GATE 2026 Rank Booster Test Series Batch", GateBranch.CS, "Prof. K. Sen", 650, "Daily 06:00 PM - 08:00 PM", 82)
            )
            for (batch in seedBatches) db.collection("batches").document(batch.id).set(batch)
            
            // Seed a few questions for test t1 just as an example
            val questions = listOf(
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
                    testId = "t2",
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
                )
            )
            for (q in questions) db.collection("questions").document(q.id).set(q)
        }
    }
}
