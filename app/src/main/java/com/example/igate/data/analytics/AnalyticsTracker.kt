package com.example.igate.data.analytics

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

object AnalyticsTracker {

    private var analytics: FirebaseAnalytics? = null

    fun init(context: Context) {
        analytics = FirebaseAnalytics.getInstance(context)
    }

    // ── User Properties ──

    fun setUserRole(role: String) {
        analytics?.setUserProperty("user_role", role)
    }

    fun setUserBranch(branch: String) {
        analytics?.setUserProperty("gate_branch", branch)
    }

    fun setTargetYear(year: String) {
        analytics?.setUserProperty("target_year", year)
    }

    // ── Screen Views ──

    fun logScreenView(screenName: String, screenClass: String = "") {
        analytics?.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, Bundle().apply {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            putString(FirebaseAnalytics.Param.SCREEN_CLASS, screenClass)
        })
    }

    // ── Lesson Events ──

    fun logLessonViewed(lessonId: String, subjectId: String, lessonTitle: String) {
        analytics?.logEvent("lesson_viewed", Bundle().apply {
            putString("lesson_id", lessonId)
            putString("subject_id", subjectId)
            putString("lesson_title", lessonTitle)
        })
    }

    // ── Test Events ──

    fun logTestStarted(testId: String, branch: String) {
        analytics?.logEvent("test_started", Bundle().apply {
            putString("test_id", testId)
            putString("branch", branch)
        })
    }

    fun logTestCompleted(testId: String, score: Double, accuracy: Double, percentile: Double) {
        analytics?.logEvent("test_completed", Bundle().apply {
            putString("test_id", testId)
            putDouble("score", score)
            putDouble("accuracy", accuracy)
            putDouble("percentile", percentile)
        })
    }

    // ── Doubt Events ──

    fun logDoubtPosted(subjectName: String) {
        analytics?.logEvent("doubt_posted", Bundle().apply {
            putString("subject_name", subjectName)
        })
    }

    fun logDoubtAnswered(subjectName: String) {
        analytics?.logEvent("doubt_answered", Bundle().apply {
            putString("subject_name", subjectName)
        })
    }

    // ── Chat Events ──

    fun logChatMessageSent(roomId: String, senderRole: String) {
        analytics?.logEvent("chat_message_sent", Bundle().apply {
            putString("room_id", roomId)
            putString("sender_role", senderRole)
        })
    }

    // ── Auth Events ──

    fun logLoginMethod(method: String) {
        analytics?.logEvent(FirebaseAnalytics.Event.LOGIN, Bundle().apply {
            putString(FirebaseAnalytics.Param.METHOD, method)
        })
    }

    fun logSignUp(method: String) {
        analytics?.logEvent(FirebaseAnalytics.Event.SIGN_UP, Bundle().apply {
            putString(FirebaseAnalytics.Param.METHOD, method)
        })
    }

    // ── Announcement Events ──

    fun logAnnouncementViewed(announcementId: String) {
        analytics?.logEvent("announcement_viewed", Bundle().apply {
            putString("announcement_id", announcementId)
        })
    }

    fun logAnnouncementPosted(title: String) {
        analytics?.logEvent("announcement_posted", Bundle().apply {
            putString("title", title)
        })
    }
}
