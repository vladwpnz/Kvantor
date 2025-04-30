package com.bambiloff.kvantor

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

object UserBootstrapper {
    private val db = FirebaseFirestore.getInstance()

    /** Викликати одразу після успішного збереження профілю користувача */
    suspend fun createUserSkeleton(
        uid: String,
        nickname: String,
        avatarName: String
    ) = withContext(Dispatchers.IO) {
        val userRef = db.collection("users").document(uid)
        val achRef  = userRef.collection("achievements")
        val now     = FieldValue.serverTimestamp()

        // Батч-запис профілю + трьох ачівок
        val batch = db.batch()
        batch.set(
            userRef,
            mapOf(
                "nickname"  to nickname,
                "avatarName" to avatarName,
                "createdAt" to now
            ),
            SetOptions.merge()
        )
        batch.set(achRef.document("WELCOME"),   mapOf("unlocked" to true,  "unlockedAt" to now))
        batch.set(achRef.document("PY_MASTER"), mapOf("unlocked" to false))
        batch.set(achRef.document("JS_SAMURAI"),mapOf("unlocked" to false))
        batch.commit().await()
    }
}
