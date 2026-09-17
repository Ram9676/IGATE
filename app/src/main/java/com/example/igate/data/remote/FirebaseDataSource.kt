package com.example.igate.data.remote

import com.example.igate.domain.model.Lesson
import com.example.igate.domain.model.Note
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import android.util.Log

class FirebaseDataSource {
    private val db = FirebaseFirestore.getInstance()

    suspend fun uploadLesson(lesson: Lesson): Boolean {
        return try {
            db.collection("lessons").document(lesson.id).set(lesson).await()
            Log.d("FirebaseDataSource", "Lesson uploaded successfully: ${lesson.title}")
            true
        } catch (e: Exception) {
            Log.e("FirebaseDataSource", "Error uploading lesson", e)
            false
        }
    }

    suspend fun uploadNote(note: Note): Boolean {
        return try {
            db.collection("notes").document(note.id).set(note).await()
            Log.d("FirebaseDataSource", "Note uploaded successfully: ${note.title}")
            true
        } catch (e: Exception) {
            Log.e("FirebaseDataSource", "Error uploading note", e)
            false
        }
    }

    suspend fun getAllLessons(): List<Lesson> {
        return try {
            val snapshot = db.collection("lessons").get().await()
            snapshot.toObjects(Lesson::class.java)
        } catch (e: Exception) {
            Log.e("FirebaseDataSource", "Error fetching lessons", e)
            emptyList()
        }
    }

    suspend fun getAllNotes(): List<Note> {
        return try {
            val snapshot = db.collection("notes").get().await()
            snapshot.toObjects(Note::class.java)
        } catch (e: Exception) {
            Log.e("FirebaseDataSource", "Error fetching notes", e)
            emptyList()
        }
    }
}
