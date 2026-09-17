package com.example.igate.presentation.main

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Filled.Home)
    object Lessons : Screen("lessons", "Learn", Icons.AutoMirrored.Filled.MenuBook)
    object Library : Screen("library", "Library", Icons.AutoMirrored.Filled.LibraryBooks)
    object Quiz : Screen("quiz", "Tests", Icons.Filled.Timer)
    object Doubts : Screen("doubts", "Doubts", Icons.Filled.Forum)
    object Profile : Screen("profile", "Profile", Icons.Filled.Person)
    
    // Educator / PW Drona
    object Teacher : Screen("teacher", "Educator", Icons.Filled.CastForEducation)

    // Institute Admin
    object Admin : Screen("admin", "Admin", Icons.Filled.AdminPanelSettings)

    object Institutions : Screen("institutions", "Institutions", Icons.Filled.Business)

    object PdfViewer : Screen("pdf_viewer/{assetName}/{title}", "PDF Viewer", Icons.AutoMirrored.Filled.LibraryBooks) {
        fun createRoute(assetName: String, title: String): String {
            val encAsset = java.net.URLEncoder.encode(assetName, java.nio.charset.StandardCharsets.UTF_8.toString())
            val encTitle = java.net.URLEncoder.encode(title, java.nio.charset.StandardCharsets.UTF_8.toString())
            return "pdf_viewer/$encAsset/$encTitle"
        }
    }

    object LessonDetail : Screen("lesson_detail/{lessonId}/{lessonTitle}", "Lesson Detail", Icons.Filled.PlayLesson) {
        fun createRoute(lessonId: String, lessonTitle: String): String {
            val encId = java.net.URLEncoder.encode(lessonId, java.nio.charset.StandardCharsets.UTF_8.toString())
            val encTitle = java.net.URLEncoder.encode(lessonTitle, java.nio.charset.StandardCharsets.UTF_8.toString())
            return "lesson_detail/$encId/$encTitle"
        }
    }

    object Announcements : Screen("announcements", "Announcements", Icons.Filled.Notifications)
    object Chat : Screen("chat_list", "Chat", Icons.Filled.Forum)
    object ChatRoom : Screen("chat_room/{roomId}/{roomName}", "Chat Room", Icons.Filled.Forum) {
        fun createRoute(roomId: String, roomName: String): String {
            val encRoomId = java.net.URLEncoder.encode(roomId, java.nio.charset.StandardCharsets.UTF_8.toString())
            val encRoomName = java.net.URLEncoder.encode(roomName, java.nio.charset.StandardCharsets.UTF_8.toString())
            return "chat_room/$encRoomId/$encRoomName"
        }
    }
    object Analytics : Screen("analytics", "Analytics", Icons.Filled.Insights)

    // New screens
    object PYQ : Screen("pyq", "PYQ", Icons.Filled.History)
    object LiveClass : Screen("live_class", "Live Classes", Icons.Filled.LiveTv)
    object TeacherStudents : Screen("teacher_students", "Students", Icons.Filled.Group)
    object TeacherTestCreator : Screen("teacher_test_creator", "Create Test", Icons.Filled.EditNote)
    object AdminAnalytics : Screen("admin_analytics", "Platform Analytics", Icons.Filled.Insights)
}

val StudentBottomNavItems = listOf(
    Screen.Home,
    Screen.Lessons,
    Screen.Quiz,
    Screen.Doubts,
    Screen.Profile
)

val TeacherBottomNavItems = listOf(
    Screen.Teacher,
    Screen.Lessons,
    Screen.Doubts,
    Screen.Profile
)

val AdminBottomNavItems = listOf(
    Screen.Admin,
    Screen.Lessons,
    Screen.Institutions,
    Screen.Profile
)
