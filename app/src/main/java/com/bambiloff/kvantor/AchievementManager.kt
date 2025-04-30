package com.bambiloff.kvantor

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

object AchievementManager {

    /** Викликаємо, коли треба відмітити ачівку як unlocked=true */
    suspend fun unlockAchievement(uid: String, achId: String) {
        // Отримуємо інстанс Firestore локально, а не в статичному полі
        val db = FirebaseFirestore.getInstance()

        val achRef = db.collection("users")
            .document(uid)
            .collection("achievements")
            .document(achId)

        db.runTransaction { tx ->
            val snap = tx.get(achRef)
            if (!snap.exists() || snap.getBoolean("unlocked") == false) {
                tx.set(
                    achRef,
                    mapOf(
                        "unlocked" to true,
                        "unlockedAt" to FieldValue.serverTimestamp()
                    ),
                    SetOptions.merge()
                )
            }
        }.await()
    }
}
