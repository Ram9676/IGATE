package com.example.igate.presentation.announcements

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
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
import com.example.igate.IgateApplication
import com.example.igate.data.analytics.AnalyticsTracker
import com.example.igate.domain.model.UserRole
import com.example.igate.theme.BrandBlue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch

data class FirestoreAnnouncement(
    val id: String = "",
    val title: String = "",
    val message: String = "",
    val timestamp: String = "",
    val type: String = "GENERAL",
    val postedBy: String = "IGATE Admin",
    val createdAt: Long = 0L
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnnouncementsScreen(onBackClick: () -> Unit) {
    val db = remember { FirebaseFirestore.getInstance() }
    val announcements = remember {
        callbackFlow {
            val listener = db.collection("announcements")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        trySend(emptyList())
                        return@addSnapshotListener
                    }
                    val list = snapshot?.documents?.mapNotNull { doc ->
                        runCatching { doc.toObject(FirestoreAnnouncement::class.java)?.copy(id = doc.id) }.getOrNull()
                    } ?: emptyList()
                    trySend(list)
                }
            awaitClose { listener.remove() }
        }
    }
    val announcementList by announcements.collectAsState(initial = emptyList())

    val context = LocalContext.current
    val repository = (context.applicationContext as IgateApplication).container.igateRepository
    val userProfile by repository.currentUserProfile.collectAsState(
        initial = com.example.igate.domain.model.UserProfile()
    )
    val isAdminOrTeacher = userProfile.role == UserRole.ADMIN || userProfile.role == UserRole.TEACHER

    var showPostDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Seed demo announcements if empty
    LaunchedEffect(announcementList) {
        if (announcementList.isEmpty()) {
            val demoAnnouncements = listOf(
                FirestoreAnnouncement(
                    title = "Urgent: Math Class Rescheduled",
                    message = "Today's Advanced Calculus class has been moved to 4:00 PM due to instructor unavailability.",
                    timestamp = "2 hours ago",
                    type = "WARNING",
                    postedBy = "IGATE Admin",
                    createdAt = System.currentTimeMillis() - 7200000
                ),
                FirestoreAnnouncement(
                    title = "New Mock Exam Available",
                    message = "The GATE CS Full Mock Test 2 is now live. Complete it by Sunday midnight to receive your global ranking.",
                    timestamp = "5 hours ago",
                    type = "EVENT",
                    postedBy = "Prof. Arvind Rao",
                    createdAt = System.currentTimeMillis() - 18000000
                ),
                FirestoreAnnouncement(
                    title = "Welcome to IGATE Platform",
                    message = "Congratulations on joining India's premier GATE preparation platform. Explore syllabus, mock tests, and doubt solving features.",
                    timestamp = "1 day ago",
                    type = "INFO",
                    postedBy = "IGATE Admin",
                    createdAt = System.currentTimeMillis() - 86400000
                )
            )
            for (a in demoAnnouncements) {
                try { db.collection("announcements").add(a) } catch (_: Exception) {}
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Announcements", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        floatingActionButton = {
            if (isAdminOrTeacher) {
                FloatingActionButton(
                    onClick = { showPostDialog = true },
                    containerColor = BrandBlue
                ) {
                    Icon(Icons.Filled.Add, "Post Announcement", tint = Color.White)
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        if (announcementList.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("No announcements yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(announcementList, key = { it.id }) { announcement ->
                    AnnouncementCard(announcement)
                }
            }
        }
    }

    if (showPostDialog) {
        PostAnnouncementDialog(
            onDismiss = { showPostDialog = false },
            onPost = { title, message, type ->
                scope.launch {
                    val announcement = FirestoreAnnouncement(
                        title = title,
                        message = message,
                        timestamp = "Just now",
                        type = type,
                        postedBy = userProfile.name,
                        createdAt = System.currentTimeMillis()
                    )
                    try {
                        db.collection("announcements").add(announcement)
                        AnalyticsTracker.logAnnouncementPosted(title)
                    } catch (_: Exception) {}
                }
                showPostDialog = false
            }
        )
    }
}

@Composable
private fun AnnouncementCard(announcement: FirestoreAnnouncement) {
    val icon: ImageVector
    val iconTint: Color
    when (announcement.type) {
        "WARNING" -> { icon = Icons.Filled.Warning; iconTint = MaterialTheme.colorScheme.error }
        "EVENT" -> { icon = Icons.Filled.Event; iconTint = BrandBlue }
        "INFO" -> { icon = Icons.Filled.Info; iconTint = MaterialTheme.colorScheme.tertiary }
        else -> { icon = Icons.Filled.Campaign; iconTint = MaterialTheme.colorScheme.onSurfaceVariant }
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 1.dp
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier.size(42.dp).clip(CircleShape)
                    .background(iconTint.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = iconTint, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    announcement.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    announcement.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        announcement.postedBy,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = BrandBlue
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        announcement.timestamp,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
private fun PostAnnouncementDialog(
    onDismiss: () -> Unit,
    onPost: (String, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("GENERAL") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Post Announcement", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("Message") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    minLines = 3
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("GENERAL", "INFO", "WARNING", "EVENT").forEach { t ->
                        FilterChip(
                            selected = type == t,
                            onClick = { type = t },
                            label = { Text(t, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (title.isNotBlank() && message.isNotBlank()) onPost(title, message, type) },
                colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
            ) { Text("Post") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
