package com.example.igate.data.repository

import com.example.igate.domain.model.*
import com.example.igate.domain.repository.IgateRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import android.util.Log

class FirestoreRepositoryImpl : IgateRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // ── Pre-configured Fallback Data (Guarantees zero-blank screen & resilient offline operation) ──

    private val defaultProfile = UserProfile(
        id = "demo_student",
        name = "Rachel Green",
        email = "rachel.green@igate.edu",
        role = UserRole.STUDENT,
        dob = "18 Feb 2002",
        branch = GateBranch.CS,
        targetYear = "GATE 2027",
        enrolledBatches = listOf("b1")
    )

    private val defaultSubjects = listOf(
        // CS & IT
        Subject(id="s1", name="Engineering Mathematics", description="Linear Algebra, Calculus, Probability & Statistics", branch=GateBranch.CS, totalLessons=16, completedLessons=10),
        Subject(id="s2", name="Data Structures & Algorithms", description="Asymptotic Analysis, Trees, Graphs, Dynamic Programming", branch=GateBranch.CS, totalLessons=24, completedLessons=14),
        Subject(id="s3", name="Operating Systems", description="Process Management, Synchronization, Virtual Memory, File Systems", branch=GateBranch.CS, totalLessons=18, completedLessons=9),
        Subject(id="s4", name="Database Management Systems", description="Relational Algebra, SQL, Normalization, Transactions & Concurrency", branch=GateBranch.CS, totalLessons=14, completedLessons=8),
        Subject(id="s5", name="Theory of Computation", description="Regular Expressions, Finite Automata, Context-Free Grammars, Turing Machines", branch=GateBranch.CS, totalLessons=15, completedLessons=7),
        Subject(id="s6", name="Computer Networks", description="OSI & TCP/IP, IP Addressing, Routing Algorithms, TCP/UDP Flow Control", branch=GateBranch.CS, totalLessons=16, completedLessons=6),
        Subject(id="s7", name="Compiler Design", description="Lexical Analysis, LL/LR Parsers, Syntax Directed Translation, Code Generation", branch=GateBranch.CS, totalLessons=12, completedLessons=4),
        Subject(id="s8", name="Digital Logic & COA", description="Boolean Algebra, Combinational Circuits, Pipelining, Cache Mapping", branch=GateBranch.CS, totalLessons=15, completedLessons=5),

        // Data Science & AI (DA)
        Subject(id="s_da1", name="Probability & Mathematical Statistics", description="Random Variables, Hypothesis Testing, Distributions", branch=GateBranch.DA, totalLessons=18, completedLessons=8),
        Subject(id="s_da2", name="Linear Algebra & Calculus for ML", description="SVD, PCA, Vector Spaces, Multivariable Calculus", branch=GateBranch.DA, totalLessons=16, completedLessons=6),
        Subject(id="s_da3", name="Machine Learning Models", description="Supervised & Unsupervised Learning, Decision Trees, SVM, Ensembles", branch=GateBranch.DA, totalLessons=22, completedLessons=11),
        Subject(id="s_da4", name="Deep Learning & Neural Networks", description="Backpropagation, CNNs, RNNs, Attention & Transformers", branch=GateBranch.DA, totalLessons=20, completedLessons=7),
        Subject(id="s_da5", name="Data Structures & Python", description="Python for Data Science, Arrays, Hashing, Graph Searching", branch=GateBranch.DA, totalLessons=15, completedLessons=9),

        // Mechanical Engineering (ME)
        Subject(id="s_me1", name="Engineering Mechanics & SOM", description="Stress, Strain, Shear Force, Bending Moment, Deflection", branch=GateBranch.ME, totalLessons=20, completedLessons=10),
        Subject(id="s_me2", name="Thermodynamics & Applied Thermal", description="Laws of Thermodynamics, Steam Power, IC Engines, Refrigeration", branch=GateBranch.ME, totalLessons=22, completedLessons=12),
        Subject(id="s_me3", name="Fluid Mechanics & Hydraulics", description="Fluid Statics, Kinematics, Bernoulli's, Boundary Layer", branch=GateBranch.ME, totalLessons=18, completedLessons=8),
        Subject(id="s_me4", name="Manufacturing Science & Production", description="Casting, Forming, Welding, Machining, Metrology", branch=GateBranch.ME, totalLessons=24, completedLessons=9),

        // Electrical Engineering (EE)
        Subject(id="s_ee1", name="Electric Circuits & Networks", description="Network Theorems, Transient Analysis, AC Sinusoids, Two-Port Networks", branch=GateBranch.EE, totalLessons=18, completedLessons=11),
        Subject(id="s_ee2", name="Control Systems", description="Root Locus, Bode Plots, State Space, Stability Analysis", branch=GateBranch.EE, totalLessons=16, completedLessons=8),
        Subject(id="s_ee3", name="Electrical Machines & Power Systems", description="Transformers, Induction Motors, Fault Analysis, Stability", branch=GateBranch.EE, totalLessons=25, completedLessons=10),

        // Electronics & Comm. (EC)
        Subject(id="s_ec1", name="Signals & Systems", description="Fourier Series/Transforms, Laplace, Z-Transforms, LTI Systems", branch=GateBranch.EC, totalLessons=18, completedLessons=9),
        Subject(id="s_ec2", name="Communications Systems", description="Analog & Digital Modulation, PCM, Noise Analysis, Information Theory", branch=GateBranch.EC, totalLessons=20, completedLessons=7),
        Subject(id="s_ec3", name="Electromagnetic Fields", description="Maxwell's Equations, Wave Propagation, Transmission Lines", branch=GateBranch.EC, totalLessons=15, completedLessons=6),

        // Civil Engineering (CE)
        Subject(id="s_ce1", name="Structural Analysis & RCC", description="Indeterminate Structures, Limit State Design, Prestressed Concrete", branch=GateBranch.CE, totalLessons=22, completedLessons=11),
        Subject(id="s_ce2", name="Geotechnical Engineering", description="Soil Mechanics, Shear Strength, Foundation Design, Earth Pressure", branch=GateBranch.CE, totalLessons=20, completedLessons=9),
        Subject(id="s_ce3", name="Environmental & Transportation Engg", description="Water Supply, Waste Water, Highway Geometric Design, Traffic", branch=GateBranch.CE, totalLessons=18, completedLessons=7)
    )

    private val defaultLessons = listOf(
        Lesson(
            id="l1",
            subjectId="s1",
            topicId="t_la",
            topicName="Linear Algebra",
            title="Eigenvalues and Cayley-Hamilton Theorem",
            description="Comprehensive GATE shortcut methods, characteristic polynomials and inverse matrix computation.",
            videoUrl="https://storage.googleapis.com/exoplayer-test-media-0/BigBuckBunny_320x180.mp4",
            durationString="48 mins",
            educatorName="Prof. Arvind Rao (IIT Bombay)",
            isCompleted=true,
            pdfAttachmentTitle="Eigenvalues & Cayley-Hamilton Formula Handbook.pdf",
            pdfAssetFileName="mock_pdf_path.pdf",
            timestamps=listOf(
                VideoTimestamp(0, "00:00", "Introduction & Matrix Fundamentals", "Review of determinant, trace, and characteristic equations det(A - lambda I) = 0"),
                VideoTimestamp(390, "06:30", "Eigenvalues & Eigenvectors Formal Concept", "Definition AX = lambda X, algebraic vs geometric multiplicity properties"),
                VideoTimestamp(945, "15:45", "Cayley-Hamilton Theorem & Matrix Inverse", "Every square matrix satisfies its own characteristic equation. Formula: A^-1 = -1/c0 * (A^(n-1) + ... + c1 I)"),
                VideoTimestamp(1700, "28:20", "GATE 2-Mark PYQ Shortcuts", "Shortcut technique to find A^4 and A^100 in 60 seconds without full matrix multiplication"),
                VideoTimestamp(2415, "40:15", "Summary & Key Formulas for Revision", "Properties: sum of eigenvalues = Trace(A), product = Det(A), eigenvalues of triangular matrix")
            )
        ),
        Lesson(
            id="l2",
            subjectId="s1",
            topicId="t_prob",
            topicName="Probability & Statistics",
            title="Probability Distributions & Bayes Theorem",
            description="Conditional probability, Poisson & Normal distributions with high-yield GATE previous year questions.",
            videoUrl="https://storage.googleapis.com/exoplayer-test-media-0/BigBuckBunny_320x180.mp4",
            durationString="52 mins",
            educatorName="Prof. Arvind Rao (IIT Bombay)",
            isCompleted=false,
            pdfAttachmentTitle="Probability Formulae & Distributions Cheatsheet.pdf",
            pdfAssetFileName="mock_pdf_path2.pdf",
            timestamps=listOf(
                VideoTimestamp(0, "00:00", "Axioms of Probability", "Sample space, mutually exclusive events, independence definition P(A cap B) = P(A)P(B)"),
                VideoTimestamp(420, "07:00", "Conditional Probability & Total Probability", "Theorem of total probability P(A) = sum P(A|Bi)P(Bi)"),
                VideoTimestamp(1080, "18:00", "Bayes' Theorem In-Depth", "Posterior probability calculation P(Bk|A) = P(A|Bk)P(Bk) / sum P(A|Bi)P(Bi)"),
                VideoTimestamp(1860, "31:00", "Discrete vs Continuous Distributions", "Binomial, Poisson (lambda = np, variance = lambda), Exponential and Normal"),
                VideoTimestamp(2700, "45:00", "Solved GATE Numerical Problems", "GATE 2023 NAT problem on Poisson defect rates and probability bounds")
            )
        ),
        Lesson(
            id="l3",
            subjectId="s2",
            topicId="t_graph",
            topicName="Graph Algorithms",
            title="Graph Algorithms: Dijkstra & Floyd-Warshall",
            description="Time complexity analysis, single-source vs all-pairs shortest paths and priority queue implementation.",
            videoUrl="https://storage.googleapis.com/exoplayer-test-media-0/BigBuckBunny_320x180.mp4",
            durationString="55 mins",
            educatorName="Prof. Arvind Rao (IIT Bombay)",
            isCompleted=true,
            pdfAttachmentTitle="Graph Shortest Paths & MST Cheatsheet.pdf",
            pdfAssetFileName="mock_pdf_path.pdf",
            timestamps=listOf(
                VideoTimestamp(0, "00:00", "Single-Source Shortest Paths Overview", "Problem formulation on weighted directed graphs with nonnegative edge weights"),
                VideoTimestamp(270, "04:30", "Relaxation Principle & Greedy Choice", "Relax(u,v,w): if d[v] > d[u] + w(u,v), update d[v] = d[u] + w(u,v)"),
                VideoTimestamp(860, "14:20", "Min-Heap / Priority Queue Implementation", "Complexity analysis: O((V + E) log V) with binary heap vs O(V^2) with array"),
                VideoTimestamp(1695, "28:15", "Why Dijkstra Fails with Negative Edges", "Counter-example showing greedy greed choice permanence failure with negative edge weights"),
                VideoTimestamp(2460, "41:00", "All-Pairs Shortest Path: Floyd-Warshall", "Dynamic programming formulation: D_k[i,j] = min(D_{k-1}[i,j], D_{k-1}[i,k] + D_{k-1}[k,j]) with O(V^3) time")
            )
        ),
        Lesson(
            id="l4",
            subjectId="s2",
            topicId="t_dp",
            topicName="Dynamic Programming",
            title="Dynamic Programming: Matrix Chain Multiplication",
            description="Optimal substructure, memoization tables, parenthesization and recurrence formulation.",
            videoUrl="https://storage.googleapis.com/exoplayer-test-media-0/BigBuckBunny_320x180.mp4",
            durationString="62 mins",
            educatorName="Prof. Arvind Rao (IIT Bombay)",
            isCompleted=false,
            pdfAttachmentTitle="Dynamic Programming Classic Patterns.pdf",
            pdfAssetFileName="mock_pdf_path2.pdf",
            timestamps=listOf(
                VideoTimestamp(0, "00:00", "Problem Statement & Matrix Multiplication Basics", "Scalar multiplication count for (p x q) and (q x r) is p * q * r"),
                VideoTimestamp(600, "10:00", "Recurrence Relation Formulation", "m[i,j] = min_{i<=k<j} {m[i,k] + m[k+1,j] + p_{i-1} * p_k * p_j}"),
                VideoTimestamp(1500, "25:00", "Bottom-Up Table Filling Strategy", "Filling table along diagonals of length l = 2 to n in O(n^3) time"),
                VideoTimestamp(2400, "40:00", "Optimal Parenthesization Extraction", "Using s[i,j] split table to reconstruct the optimal matrix parenthesis"),
                VideoTimestamp(3120, "52:00", "GATE PYQ Drill: 4-Matrix Chain", "Solving GATE 2021 NAT question with dimensions [10x20, 20x30, 30x40, 40x30]")
            )
        ),
        Lesson(
            id="l5",
            subjectId="s3",
            topicId="t_deadlock",
            topicName="Process Management & Deadlocks",
            title="Deadlock Prevention, Avoidance & Banker's Algorithm",
            description="Resource allocation graphs, Coffman conditions, safe states and safety check algorithm.",
            videoUrl="https://storage.googleapis.com/exoplayer-test-media-0/BigBuckBunny_320x180.mp4",
            durationString="45 mins",
            educatorName="Dr. Meenakshi Sundaram",
            isCompleted=false,
            pdfAttachmentTitle="Operating Systems Deadlocks & Memory Notes.pdf",
            pdfAssetFileName="mock_pdf_path.pdf",
            timestamps=listOf(
                VideoTimestamp(0, "00:00", "Four Coffman Necessary Conditions", "Mutual exclusion, Hold and wait, No preemption, Circular wait"),
                VideoTimestamp(360, "06:00", "Deadlock Prevention vs Avoidance", "Preventing at least one condition vs maintaining system in safe state"),
                VideoTimestamp(900, "15:00", "Banker's Algorithm: Need Matrix Calculation", "Need[i,j] = Max[i,j] - Allocation[i,j] and Work vector updates"),
                VideoTimestamp(1560, "26:00", "Safety Algorithm Trace & Safe Sequence", "Finding sequence <P1, P3, P4, P0, P2> guaranteeing no deadlock"),
                VideoTimestamp(2220, "37:00", "Resource-Request Algorithm & GATE Traps", "Safe state vs deadlock-free distinction in GATE examinations")
            )
        ),
        Lesson(
            id="l6",
            subjectId="s4",
            topicId="t_norm",
            topicName="Database Normalization",
            title="Lossless Join Decomposition & Dependency Preservation",
            description="Testing for BCNF and 3NF minimal covers, functional dependency closures and attribute sets.",
            videoUrl="https://storage.googleapis.com/exoplayer-test-media-0/BigBuckBunny_320x180.mp4",
            durationString="50 mins",
            educatorName="Dr. Meenakshi Sundaram",
            isCompleted=false,
            pdfAttachmentTitle="DBMS Normalization 1NF to BCNF Formulae.pdf",
            pdfAssetFileName="mock_pdf_path2.pdf",
            timestamps=listOf(
                VideoTimestamp(0, "00:00", "Functional Dependencies & Closure X+", "Definition of FD X -> Y and Armstrong's axioms (reflexivity, augmentation, transitivity)"),
                VideoTimestamp(480, "08:00", "Candidate Keys Determination Algorithm", "Finding all candidate keys using essential attribute analysis"),
                VideoTimestamp(1140, "19:00", "Lossless Join Property Condition", "R1 cap R2 -> R1 or R1 cap R2 -> R2 check"),
                VideoTimestamp(1740, "29:00", "Dependency Preservation Test", "Projecting FDs onto sub-relations and checking (F1 cup F2)+ = F+"),
                VideoTimestamp(2400, "40:00", "3NF Synthesis vs BCNF Decomposition", "Why 3NF always guarantees lossless + dependency preservation, while BCNF may lose FDs")
            )
        ),
        Lesson(
            id="l7",
            subjectId="s5",
            topicId="t_toc",
            topicName="Decidability & Turing Machines",
            title="Decidability and Halting Problem",
            description="Reduction techniques, Turing recognizability, Rice's theorem and diagonal proof.",
            videoUrl="https://storage.googleapis.com/exoplayer-test-media-0/BigBuckBunny_320x180.mp4",
            durationString="40 mins",
            educatorName="Prof. Arvind Rao (IIT Bombay)",
            isCompleted=false,
            pdfAttachmentTitle="TOC Closure & Decidability Summary Table.pdf",
            pdfAssetFileName="mock_pdf_path.pdf",
            timestamps=listOf(
                VideoTimestamp(0, "00:00", "Chomsky Hierarchy Review", "Regular, CFL, CSL, Recursive (Decidable), Recursively Enumerable"),
                VideoTimestamp(300, "05:00", "Turing Machine Formal Model", "7-tuple definition, transition function, accept and reject states"),
                VideoTimestamp(780, "13:00", "The Halting Problem (HALT_TM)", "Formal proof by contradiction using Turing's diagonal argument"),
                VideoTimestamp(1440, "24:00", "Rice's Theorem Statement & Applications", "Any non-trivial semantic property of RE languages is undecidable"),
                VideoTimestamp(1980, "33:00", "Post Correspondence Problem (PCP) & Reductions", "Reduction from HALT to PCP and CFL ambiguity")
            )
        ),
        Lesson(
            id="l8",
            subjectId="s6",
            topicId="t_cn",
            topicName="IP Addressing & Routing",
            title="Subnetting, CIDR & Variable Length Subnet Masks",
            description="IPv4 packet fragmentation, header calculation, prefix matching and longest prefix match.",
            videoUrl="https://storage.googleapis.com/exoplayer-test-media-0/BigBuckBunny_320x180.mp4",
            durationString="58 mins",
            educatorName="Prof. Arvind Rao (IIT Bombay)",
            isCompleted=false,
            pdfAttachmentTitle="Computer Networks Protocols & Subnetting Guide.pdf",
            pdfAssetFileName="mock_pdf_path2.pdf",
            timestamps=listOf(
                VideoTimestamp(0, "00:00", "IPv4 Addressing & Classful Shortcomings", "Class A, B, C ranges and address exhaustion motivation for CIDR"),
                VideoTimestamp(480, "08:00", "CIDR Notation & Subnet Masking", "Slash notation /24, /27, network ID and broadcast address calculation"),
                VideoTimestamp(1200, "20:00", "Variable Length Subnet Masking (VLSM)", "Dividing block 192.168.1.0/24 into subnets of 60, 30, 14, and 2 hosts"),
                VideoTimestamp(2100, "35:00", "Longest Prefix Match in Routing Tables", "Routing table lookup algorithm with binary trie"),
                VideoTimestamp(2880, "48:00", "IPv4 Header Fragmentation Calculation", "MTU, Identification, MF flag, DF flag and fragment offset = bytes / 8")
            )
        )
    )

    private val defaultNotes = listOf(
        Note(id="n1", subjectId="s1", title="Engineering Mathematics Formula Handbook", description="Calculus, Linear Algebra, Vector Calculus quick sheets", type=NoteType.PDF, fileUrlOrContent="mock_pdf_path.pdf", isDownloaded=true),
        Note(id="n2", subjectId="s2", title="Master Algorithm Recurrences & Master Theorem", description="Quick reference tables for time complexity bounds", type=NoteType.PDF, fileUrlOrContent="mock_pdf_path2.pdf", isDownloaded=true),
        Note(id="n3", subjectId="s3", title="OS Synchronization & Classical Problems Notes", description="Dining philosophers, Producer-Consumer semaphore code", type=NoteType.TEXT, fileUrlOrContent="Semaphore and Mutex implementations in C", isDownloaded=false),
        Note(id="n4", subjectId="s4", title="GATE DBMS SQL Queries & Normal Forms Summary", description="Comprehensive 1NF to BCNF rules cheat sheet", type=NoteType.PDF, fileUrlOrContent="mock_pdf_path.pdf", isDownloaded=true),
        Note(id="n5", subjectId="s5", title="Closure Properties of Formal Languages Table", description="Union, intersection, complementation closure summary", type=NoteType.PDF, fileUrlOrContent="mock_pdf_path2.pdf", isDownloaded=false)
    )

    private val mutableLessons = java.util.Collections.synchronizedList(defaultLessons.toMutableList())
    private val mutableNotes = java.util.Collections.synchronizedList(defaultNotes.toMutableList())

    private val defaultInstitutions = listOf(
        Institution("i1", "IGATE Central Campus", null, "Official GATE CS/IT and Mechanical Preparation Center"),
        Institution("i2", "IIT Madras Research Park Hub", null, "Joint Faculty Mentorship and Advanced Mock Series"),
        Institution("i3", "IISc Bangalore Prep Wing", null, "Special Focus on Data Science & AI Curriculum")
    )

    private val defaultTests = listOf(
        GateTest("t1", "All India Open GATE Mock 1", "Comprehensive Syllabus", GateBranch.CS, 60, 50, 25, true, "Full Mock"),
        GateTest("t2", "Algorithms & Data Structures Speed Test", "Data Structures & Algorithms", GateBranch.CS, 30, 25, 10, false, "Subject Test"),
        GateTest("t3", "Engineering Mathematics & Aptitude Drill", "Engineering Mathematics", GateBranch.CS, 35, 30, 12, false, "Subject Test"),
        GateTest("t4", "Operating Systems Core Concepts & PYQs", "Operating Systems", GateBranch.CS, 40, 30, 15, false, "Topic Test"),
        GateTest("t5", "GATE 2024 Official Paper Simulation", "All Subjects", GateBranch.CS, 180, 100, 65, true, "PYQ 2024")
    )

    private val defaultDoubts = listOf(
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

    private val defaultBatches = listOf(
        Batch("b1", "GATE 2027 CS - Alpha Star Batch", GateBranch.CS, "Prof. Arvind Rao & Team", 420, "Mon-Fri 07:00 AM - 09:30 AM", 68),
        Batch("b2", "Weekend Comprehensive Batch - CS/IT", GateBranch.CS, "Dr. Meenakshi Sundaram", 280, "Sat-Sun 09:00 AM - 02:00 PM", 45),
        Batch("b3", "GATE 2026 Rank Booster Test Series Batch", GateBranch.CS, "Prof. K. Sen", 650, "Daily 06:00 PM - 08:00 PM", 82)
    )

    private val defaultQuestions = listOf(
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

    override val currentUserProfile: Flow<UserProfile> = callbackFlow {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userFallback = defaultProfile.copy(
                id = currentUser.uid,
                email = currentUser.email ?: currentUser.phoneNumber ?: defaultProfile.email,
                name = currentUser.displayName ?: currentUser.email?.substringBefore('@') ?: defaultProfile.name
            )
            trySend(userFallback)

            val listener = db.collection("users").document(currentUser.uid)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        Log.w("FirestoreRepo", "Error fetching user profile (using cached/fallback): ${e.message}")
                        trySend(userFallback)
                        return@addSnapshotListener
                    }
                    if (snapshot != null && snapshot.exists()) {
                        val profile = runCatching { snapshot.toObject(UserProfile::class.java) }.getOrNull()
                        trySend(profile ?: userFallback)
                    } else {
                        trySend(userFallback)
                        try {
                            db.collection("users").document(currentUser.uid).set(userFallback)
                        } catch (ex: Exception) {
                            Log.w("FirestoreRepo", "Could not create initial profile doc: ${ex.message}")
                        }
                    }
                }
            awaitClose { listener.remove() }
        } else {
            trySend(defaultProfile)
            awaitClose { }
        }
    }

    override suspend fun updateUserProfile(profile: UserProfile) {
        val user = auth.currentUser ?: return
        val profileToSave = profile.copy(id = user.uid, email = user.email ?: profile.email)
        try {
            db.collection("users").document(user.uid).set(profileToSave, com.google.firebase.firestore.SetOptions.merge()).await()
        } catch (e: Exception) {
            Log.w("FirestoreRepo", "updateUserProfile failed: ${e.message}")
        }
    }

    override suspend fun switchRole(role: UserRole) {
        val user = auth.currentUser ?: return
        try {
            db.collection("users").document(user.uid).set(
                mapOf("role" to role.name),
                com.google.firebase.firestore.SetOptions.merge()
            ).await()
        } catch (e: Exception) {
            Log.w("FirestoreRepo", "switchRole failed: ${e.message}")
        }
    }

    override fun getAllSubjects(): Flow<List<Subject>> = callbackFlow {
        trySend(defaultSubjects)
        val listener = db.collection("subjects").addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w("FirestoreRepo", "getAllSubjects listener error: ${e.message}")
                trySend(defaultSubjects)
                return@addSnapshotListener
            }
            val list = snapshot?.documents?.mapNotNull { runCatching { it.toObject(Subject::class.java) }.getOrNull() } ?: emptyList()
            trySend(if (list.isNotEmpty()) list else defaultSubjects)
        }
        awaitClose { listener.remove() }
    }

    override fun getSubjectsForBranch(branch: GateBranch): Flow<List<Subject>> = callbackFlow {
        val fallback = defaultSubjects.filter { it.branch == branch }
        trySend(fallback)
        val listener = db.collection("subjects").whereEqualTo("branch", branch.name).addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w("FirestoreRepo", "getSubjectsForBranch listener error: ${e.message}")
                trySend(fallback)
                return@addSnapshotListener
            }
            val list = snapshot?.documents?.mapNotNull { runCatching { it.toObject(Subject::class.java) }.getOrNull() } ?: emptyList()
            trySend(if (list.isNotEmpty()) list else fallback)
        }
        awaitClose { listener.remove() }
    }

    override fun getLessonsForSubject(subjectId: String): Flow<List<Lesson>> = callbackFlow {
        val fallback = mutableLessons.filter { it.subjectId == subjectId }
        trySend(fallback)
        val listener = db.collection("lessons").whereEqualTo("subjectId", subjectId).addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w("FirestoreRepo", "getLessonsForSubject listener error: ${e.message}")
                trySend(fallback)
                return@addSnapshotListener
            }
            val list = snapshot?.documents?.mapNotNull { runCatching { it.toObject(Lesson::class.java) }.getOrNull() } ?: emptyList()
            trySend(if (list.isNotEmpty()) list else fallback)
        }
        awaitClose { listener.remove() }
    }

    override fun getRecentLessons(): Flow<List<Lesson>> = callbackFlow {
        trySend(mutableLessons.toList())
        val listener = db.collection("lessons").addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w("FirestoreRepo", "getRecentLessons listener error: ${e.message}")
                trySend(mutableLessons.toList())
                return@addSnapshotListener
            }
            val list = snapshot?.documents?.mapNotNull { runCatching { it.toObject(Lesson::class.java) }.getOrNull() } ?: emptyList()
            trySend(if (list.isNotEmpty()) list else mutableLessons.toList())
        }
        awaitClose { listener.remove() }
    }

    override fun getNotesForSubject(subjectId: String): Flow<List<Note>> = callbackFlow {
        val fallback = mutableNotes.filter { it.subjectId == subjectId }
        trySend(fallback)
        val listener = db.collection("notes").whereEqualTo("subjectId", subjectId).addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w("FirestoreRepo", "getNotesForSubject listener error: ${e.message}")
                trySend(fallback)
                return@addSnapshotListener
            }
            val list = snapshot?.documents?.mapNotNull { runCatching { it.toObject(Note::class.java) }.getOrNull() } ?: emptyList()
            trySend(if (list.isNotEmpty()) list else fallback)
        }
        awaitClose { listener.remove() }
    }

    override fun getPinnedOrDownloadedNotes(): Flow<List<Note>> = callbackFlow {
        val fallback = defaultNotes.filter { it.isDownloaded }
        trySend(fallback)
        val listener = db.collection("notes").whereEqualTo("isDownloaded", true).addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w("FirestoreRepo", "getPinnedOrDownloadedNotes listener error: ${e.message}")
                trySend(fallback)
                return@addSnapshotListener
            }
            val list = snapshot?.documents?.mapNotNull { runCatching { it.toObject(Note::class.java) }.getOrNull() } ?: emptyList()
            trySend(if (list.isNotEmpty()) list else fallback)
        }
        awaitClose { listener.remove() }
    }

    override fun getAllInstitutions(): Flow<List<Institution>> = callbackFlow {
        trySend(defaultInstitutions)
        val listener = db.collection("institutions").addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w("FirestoreRepo", "getAllInstitutions listener error: ${e.message}")
                trySend(defaultInstitutions)
                return@addSnapshotListener
            }
            val list = snapshot?.documents?.mapNotNull { runCatching { it.toObject(Institution::class.java) }.getOrNull() } ?: emptyList()
            trySend(if (list.isNotEmpty()) list else defaultInstitutions)
        }
        awaitClose { listener.remove() }
    }

    override suspend fun insertLesson(lesson: Lesson) {
        mutableLessons.add(0, lesson)
        try {
            db.collection("lessons").document(lesson.id).set(lesson).await()
        } catch (e: Exception) {
            Log.w("FirestoreRepo", "insertLesson failed: ${e.message}")
        }
    }

    override suspend fun insertNote(note: Note) {
        mutableNotes.add(0, note)
        try {
            db.collection("notes").document(note.id).set(note).await()
        } catch (e: Exception) {
            Log.w("FirestoreRepo", "insertNote failed: ${e.message}")
        }
    }

    override suspend fun syncWithCloud() {
        // Direct reactive connection maintained
    }

    override fun getAvailableTests(branch: GateBranch): Flow<List<GateTest>> = callbackFlow {
        val fallback = defaultTests.filter { it.branch == branch || it.branch == GateBranch.CS }
        trySend(fallback)
        val listener = db.collection("tests").whereEqualTo("branch", branch.name).addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w("FirestoreRepo", "getAvailableTests listener error: ${e.message}")
                trySend(fallback)
                return@addSnapshotListener
            }
            val list = snapshot?.documents?.mapNotNull { runCatching { it.toObject(GateTest::class.java) }.getOrNull() } ?: emptyList()
            trySend(if (list.isNotEmpty()) list else fallback)
        }
        awaitClose { listener.remove() }
    }

    override fun getQuestionsForTest(testId: String): Flow<List<GateQuestion>> = callbackFlow {
        val fallback = defaultQuestions.filter { it.testId == testId }.ifEmpty { defaultQuestions }
        trySend(fallback)
        val listener = db.collection("questions").whereEqualTo("testId", testId).addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w("FirestoreRepo", "getQuestionsForTest listener error: ${e.message}")
                trySend(fallback)
                return@addSnapshotListener
            }
            val list = snapshot?.documents?.mapNotNull { runCatching { it.toObject(GateQuestion::class.java) }.getOrNull() } ?: emptyList()
            trySend(if (list.isNotEmpty()) list else fallback)
        }
        awaitClose { listener.remove() }
    }

    override suspend fun submitTestAttempt(result: TestAttemptResult) {
        try {
            db.collection("test_results").add(result).await()
        } catch (e: Exception) {
            Log.w("FirestoreRepo", "submitTestAttempt failed: ${e.message}")
        }
    }

    override fun getRecentTestResults(): Flow<List<TestAttemptResult>> = callbackFlow {
        val user = auth.currentUser
        if (user != null) {
            val listener = db.collection("test_results").whereEqualTo("userId", user.uid).addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w("FirestoreRepo", "getRecentTestResults error: ${e.message}")
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { runCatching { it.toObject(TestAttemptResult::class.java) }.getOrNull() } ?: emptyList()
                trySend(list)
            }
            awaitClose { listener.remove() }
        } else {
            trySend(emptyList())
            awaitClose { }
        }
    }

    override fun getDoubts(): Flow<List<Doubt>> = callbackFlow {
        trySend(defaultDoubts)
        val listener = db.collection("doubts").addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w("FirestoreRepo", "getDoubts listener error: ${e.message}")
                trySend(defaultDoubts)
                return@addSnapshotListener
            }
            val list = snapshot?.documents?.mapNotNull { runCatching { it.toObject(Doubt::class.java) }.getOrNull() } ?: emptyList()
            trySend(if (list.isNotEmpty()) list else defaultDoubts)
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
        try {
            db.collection("doubts").document(newDoubt.id).set(newDoubt).await()
        } catch (e: Exception) {
            Log.w("FirestoreRepo", "postDoubt failed: ${e.message}")
        }
    }

    override suspend fun answerDoubt(doubtId: String, answer: String, teacherName: String) {
        try {
            db.collection("doubts").document(doubtId).update(
                "answerText", answer,
                "answeredByTeacher", teacherName,
                "isResolved", true
            ).await()
        } catch (e: Exception) {
            Log.w("FirestoreRepo", "answerDoubt failed: ${e.message}")
        }
    }

    override fun getBatches(): Flow<List<Batch>> = callbackFlow {
        trySend(defaultBatches)
        val listener = db.collection("batches").addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w("FirestoreRepo", "getBatches listener error: ${e.message}")
                trySend(defaultBatches)
                return@addSnapshotListener
            }
            val list = snapshot?.documents?.mapNotNull { runCatching { it.toObject(Batch::class.java) }.getOrNull() } ?: emptyList()
            trySend(if (list.isNotEmpty()) list else defaultBatches)
        }
        awaitClose { listener.remove() }
    }

    override suspend fun createBatch(batch: Batch) {
        try {
            db.collection("batches").document(batch.id).set(batch).await()
        } catch (e: Exception) {
            Log.w("FirestoreRepo", "createBatch failed: ${e.message}")
        }
    }

    override suspend fun seedMockDataIfEmpty() {
        try {
            val subjectsCount = try {
                db.collection("subjects").get().await().size()
            } catch (e: Exception) {
                Log.w("FirestoreRepo", "Check subjects failed: ${e.message}")
                -1
            }
            if (subjectsCount == 0) {
                for (sub in defaultSubjects) db.collection("subjects").document(sub.id).set(sub)
                for (les in defaultLessons) db.collection("lessons").document(les.id).set(les)
                for (note in defaultNotes) db.collection("notes").document(note.id).set(note)
                for (inst in defaultInstitutions) db.collection("institutions").document(inst.id).set(inst)
                for (test in defaultTests) db.collection("tests").document(test.id).set(test)
                for (doubt in defaultDoubts) db.collection("doubts").document(doubt.id).set(doubt)
                for (batch in defaultBatches) db.collection("batches").document(batch.id).set(batch)
                for (q in defaultQuestions) db.collection("questions").document(q.id).set(q)
            }
        } catch (e: Exception) {
            Log.w("FirestoreRepo", "Mock data seeding skipped: ${e.message}")
        }
    }
}
