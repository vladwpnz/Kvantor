package com.bambiloff.kvantor

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

object UserBootstrapper {

    /* ---------- дефолтні значення ---------- */
    private const val START_LIVES  = 5
    private const val START_HINTS  = 3
    private const val START_COINS  = 0

    /** Швидке посилання на Firestore */
    private val db: FirebaseFirestore
        get() = FirebaseFirestore.getInstance()

    /**
     * Викликати одразу після **реєстрації** (користувач іще не має документа).
     */
    suspend fun createUserSkeleton(
        uid: String,
        nickname: String,
        avatarName: String
    ): Unit = withContext(Dispatchers.IO) {

        val userRef = db.collection("users").document(uid)
        val achRef  = userRef.collection("achievements")
        val now     = FieldValue.serverTimestamp()

        db.runBatch { b ->
            /* --- профіль --- */
            b.set(
                userRef,
                mapOf(
                    "nickname"    to nickname,
                    "avatarName"  to avatarName,
                    "createdAt"   to now,
                    "lives"       to START_LIVES,
                    "hints"       to START_HINTS,
                    "coins"       to START_COINS,
                    "lastLifeTS"  to null          // поки не витрачене життя
                ),
                SetOptions.merge()
            )

            /* --- базові ачівки --- */
            b.set(achRef.document("WELCOME"),           mapOf("unlocked" to true,  "unlockedAt" to now))
            b.set(achRef.document("FIRST_STEPS_IN_PY"), mapOf("unlocked" to false))
            b.set(achRef.document("FIRST_STEPS_IN_JS"), mapOf("unlocked" to false))
        }.await()
    }

    /**
     * Викликати **після логіну** для існуючих акаунтів, щоб додати відсутні поля.
     * Не перезаписує значення, якщо вони вже є.
     */
    suspend fun ensureStats(uid: String): Unit = withContext(Dispatchers.IO) {
        val ref = db.collection("users").document(uid)

        db.runTransaction { tx ->
            val snap = tx.get(ref)

            if (!snap.contains("lives"))
                tx.update(ref, "lives", START_LIVES)

            if (!snap.contains("hints"))
                tx.update(ref, "hints", START_HINTS)

            if (!snap.contains("coins"))
                tx.update(ref, "coins", START_COINS)

            if (!snap.contains("lastLifeTS"))
                tx.update(ref, "lastLifeTS", null)
        }.await()
    }
}
