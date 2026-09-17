package com.example.igate.presentation.main

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.igate.IgateApplication
import com.example.igate.domain.model.UserRole
import com.example.igate.presentation.admin.AdminAnalyticsScreen
import com.example.igate.presentation.admin.AdminScreen
import com.example.igate.presentation.analytics.AnalyticsScreen
import com.example.igate.presentation.announcements.AnnouncementsScreen
import com.example.igate.presentation.chat.ChatScreen
import com.example.igate.presentation.chat.ChatListScreen
import com.example.igate.presentation.doubts.DoubtsScreen
import com.example.igate.presentation.home.HomeScreen
import com.example.igate.presentation.institutions.InstitutionsScreen
import com.example.igate.presentation.lessons.LessonDetailScreen
import com.example.igate.presentation.lessons.LessonsScreen
import com.example.igate.presentation.library.LibraryScreen
import com.example.igate.presentation.library.PdfViewerScreen
import com.example.igate.presentation.live.LiveClassScreen
import com.example.igate.presentation.profile.ProfileScreen
import com.example.igate.presentation.pyq.PYQScreen
import com.example.igate.presentation.quiz.QuizScreen
import com.example.igate.presentation.teacher.TeacherDashboardScreen
import com.example.igate.presentation.teacher.TeacherStudentsScreen
import com.example.igate.presentation.teacher.TeacherTestCreatorScreen
import com.example.igate.theme.BrandBlue
import com.example.igate.presentation.common.ios.iosGlassmorphism

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val repository = (LocalContext.current.applicationContext as IgateApplication).container.igateRepository
    val userProfile by repository.currentUserProfile.collectAsState(initial = com.example.igate.domain.model.UserProfile())

    val navItems = when (userProfile.role) {
        UserRole.STUDENT -> StudentBottomNavItems
        UserRole.TEACHER -> TeacherBottomNavItems
        UserRole.ADMIN -> AdminBottomNavItems
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    LaunchedEffect(userProfile.role) {
        val targetRoute = when (userProfile.role) {
            UserRole.STUDENT -> Screen.Home.route
            UserRole.TEACHER -> Screen.Teacher.route
            UserRole.ADMIN -> Screen.Admin.route
        }
        if (currentRoute != targetRoute && currentRoute != null) {
            navController.navigate(targetRoute) {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    // Hide bottom bar on full screen mock exam / pdf viewer
    val shouldShowBottomBar = currentRoute !in listOf(
        Screen.Quiz.route,
        Screen.PdfViewer.route,
        Screen.LessonDetail.route
    )

    Scaffold(
        bottomBar = {
            if (shouldShowBottomBar) {
                AppBottomBar(
                    navController = navController,
                    items = navItems,
                    currentRoute = currentRoute
                )
            }
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                modifier = Modifier.fillMaxSize()
            ) {
            // Student Home
            composable(Screen.Home.route) {
                HomeScreen(
                    onLessonClick = { lessonId, title ->
                        navController.navigate(Screen.LessonDetail.createRoute(lessonId, title))
                    },
                    onSubjectClick = { _, _ ->
                        navController.navigate(Screen.Lessons.route)
                    },
                    onQuizClick = { _ ->
                        navController.navigate(Screen.Quiz.route)
                    },
                    onAnnouncementsClick = {
                        navController.navigate(Screen.Announcements.route)
                    },
                    onDoubtsClick = {
                        navController.navigate(Screen.Doubts.route)
                    },
                    onLiveClassClick = {
                        navController.navigate(Screen.LiveClass.route)
                    },
                    onPYQClick = {
                        navController.navigate(Screen.PYQ.route)
                    }
                )
            }

            // Lessons / Subjects
            composable(Screen.Lessons.route) {
                LessonsScreen(
                    onLessonClick = { lessonId, title ->
                        navController.navigate(Screen.LessonDetail.createRoute(lessonId, title))
                    },
                    onPdfClick = { assetName, title ->
                        navController.navigate(Screen.PdfViewer.createRoute(assetName, title))
                    }
                )
            }

            // Library
            composable(Screen.Library.route) {
                LibraryScreen(
                    onPdfClick = { assetName, title ->
                        navController.navigate(Screen.PdfViewer.createRoute(assetName, title))
                    }
                )
            }

            // Authentic GATE Test Series & Practice
            composable(Screen.Quiz.route) {
                QuizScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }

            // Doubts Forum
            composable(Screen.Doubts.route) {
                DoubtsScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }

            // Teacher Dashboard (PW Drona / Educator)
            composable(Screen.Teacher.route) {
                TeacherDashboardScreen(
                    onBackClick = {
                        if (!navController.popBackStack()) {
                            navController.navigate(Screen.Profile.route)
                        }
                    },
                    onStudentsClick = { navController.navigate(Screen.TeacherStudents.route) },
                    onCreateTestClick = { navController.navigate(Screen.TeacherTestCreator.route) }
                )
            }

            // Admin Portal (Teachmint / Classplus)
            composable(Screen.Admin.route) {
                AdminScreen(
                    onBackClick = {
                        if (!navController.popBackStack()) {
                            navController.navigate(Screen.Profile.route)
                        }
                    },
                    onAnalyticsClick = { navController.navigate(Screen.AdminAnalytics.route) }
                )
            }

            // Institutions
            composable(Screen.Institutions.route) {
                InstitutionsScreen()
            }

            // Academic Profile & Role Switcher
            composable(Screen.Profile.route) {
                ProfileScreen(
                    onAdminClick = { navController.navigate(Screen.Admin.route) },
                    onTeacherClick = { navController.navigate(Screen.Teacher.route) },
                    onAnalyticsClick = { navController.navigate(Screen.Analytics.route) },
                    onDoubtsClick = { navController.navigate(Screen.Doubts.route) },
                    onLogoutClick = { /* handeled by MainActivity state listener */ }
                )
            }

            // Analytics Dashboard
            composable(Screen.Analytics.route) {
                AnalyticsScreen(onBackClick = { navController.popBackStack() })
            }

            // Lesson Detail Video Player
            composable(Screen.LessonDetail.route) { backStackEntry ->
                val rawId = backStackEntry.arguments?.getString("lessonId") ?: ""
                val rawTitle = backStackEntry.arguments?.getString("lessonTitle") ?: "Lesson"
                val lessonId = try { java.net.URLDecoder.decode(rawId, "UTF-8") } catch (e: Exception) { rawId }
                val lessonTitle = try { java.net.URLDecoder.decode(rawTitle, "UTF-8") } catch (e: Exception) { rawTitle }
                LessonDetailScreen(
                    lessonId = lessonId,
                    lessonTitle = lessonTitle,
                    onBackClick = { navController.popBackStack() },
                    onPdfClick = { assetName, title ->
                        navController.navigate(Screen.PdfViewer.createRoute(assetName, title))
                    }
                )
            }

            // PDF Viewer
            composable(Screen.PdfViewer.route) { backStackEntry ->
                val rawAsset = backStackEntry.arguments?.getString("assetName") ?: ""
                val rawTitle = backStackEntry.arguments?.getString("title") ?: "Document"
                val assetName = try { java.net.URLDecoder.decode(rawAsset, "UTF-8") } catch (e: Exception) { rawAsset }
                val title = try { java.net.URLDecoder.decode(rawTitle, "UTF-8") } catch (e: Exception) { rawTitle }
                PdfViewerScreen(
                    assetFileName = assetName,
                    title = title,
                    onBackClick = { navController.popBackStack() }
                )
            }

            // Announcements
            composable(Screen.Announcements.route) {
                AnnouncementsScreen(onBackClick = { navController.popBackStack() })
            }

            // Chat List
            composable(Screen.Chat.route) {
                ChatListScreen(
                    onBackClick = { navController.popBackStack() },
                    onRoomClick = { roomId ->
                        navController.navigate(Screen.ChatRoom.createRoute(roomId, "Chat"))
                    }
                )
            }

            // Chat Room
            composable(Screen.ChatRoom.route) { backStackEntry ->
                val rawId = backStackEntry.arguments?.getString("roomId") ?: ""
                val rawName = backStackEntry.arguments?.getString("roomName") ?: "Chat"
                val roomId = try { java.net.URLDecoder.decode(rawId, "UTF-8") } catch (e: Exception) { rawId }
                val roomName = try { java.net.URLDecoder.decode(rawName, "UTF-8") } catch (e: Exception) { rawName }
                ChatScreen(
                    roomId = roomId,
                    roomName = roomName,
                    onBackClick = { navController.popBackStack() }
                )
            }

            // PYQ Browser
            composable(Screen.PYQ.route) {
                PYQScreen(onBackClick = { navController.popBackStack() })
            }

            // Live Classes
            composable(Screen.LiveClass.route) {
                LiveClassScreen(onBackClick = { navController.popBackStack() })
            }

            // Teacher: Student Roster
            composable(Screen.TeacherStudents.route) {
                TeacherStudentsScreen(onBackClick = { navController.popBackStack() })
            }

            // Teacher: Test Creator
            composable(Screen.TeacherTestCreator.route) {
                TeacherTestCreatorScreen(
                    onBackClick = { navController.popBackStack() },
                    onPublish = { navController.popBackStack() }
                )
            }

            // Admin: Platform Analytics
            composable(Screen.AdminAnalytics.route) {
                AdminAnalyticsScreen(onBackClick = { navController.popBackStack() })
            }
        }
    }
}
}

// Crisp, solid, high-contrast Material 3 Docked Bottom Bar
@Composable
fun AppBottomBar(
    navController: NavHostController,
    items: List<Screen>,
    currentRoute: String?
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp,
        shadowElevation = 8.dp,
        border = androidx.compose.foundation.BorderStroke(
            0.5.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    ) {
        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp,
            windowInsets = NavigationBarDefaults.windowInsets,
            modifier = Modifier.fillMaxWidth()
        ) {
            items.forEach { screen ->
                val isSelected = currentRoute == screen.route
                NavigationBarItem(
                    icon = {
                        Icon(
                            imageVector = screen.icon,
                            contentDescription = screen.title,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = {
                        Text(
                            text = screen.title,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    selected = isSelected,
                    onClick = {
                        if (currentRoute != screen.route) {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }
    }
}
