package com.bambiloff.kvantor

import android.annotation.SuppressLint
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

object GameManager {

    @SuppressLint("StaticFieldLeak")
    private val db = FirebaseFirestore.getInstance()

    private fun ref(uid: String) =
        db.collection("users").document(uid)

    /* ------------ універсальна покупка ------------ */
    suspend fun buy(
        uid: String,
        price: Int,
        onSuccess: suspend () -> Unit
    ): Boolean = withContext(Dispatchers.IO) {
        val ref = db.collection("users").document(uid)
        db.runTransaction { tx ->
            val coins = (tx.get(ref).getLong("coins") ?: 0).toInt()
            if (coins >= price) {
                tx.update(ref, "coins", coins - price)
                true
            } else false
        }.await().also { ok -> if (ok) onSuccess() }
    }

    /* ------------ додати життя / підказки ------------ */
    suspend fun addLives(uid: String, delta: Int) = withContext(Dispatchers.IO) {
        val ref = db.collection("users").document(uid)
        db.runTransaction { tx ->
            val cur = (tx.get(ref).getLong("lives") ?: 0).toInt()
            tx.update(ref, "lives", cur + delta)
        }.await()
    }

    suspend fun addHints(uid: String, delta: Int) = withContext(Dispatchers.IO) {
        val ref = db.collection("users").document(uid)
        db.runTransaction { tx ->
            val cur = (tx.get(ref).getLong("hints") ?: 0).toInt()
            tx.update(ref, "hints", cur + delta)
        }.await()
    }

    /* ---------- lives ---------- */

    /** −1 life. Повертає false, якщо життя вже =0 */
    suspend fun spendLife(uid: String): Boolean = withContext(Dispatchers.IO) {
        db.runTransaction { tx ->
            val doc   = tx.get(ref(uid))
            val lives = (doc.getLong("lives") ?: 0).toInt()
            if (lives > 0) {
                tx.update(
                    ref(uid),
                    mapOf(
                        "lives"      to lives - 1,
                        "lastLifeTS" to FieldValue.serverTimestamp()
                    )
                )
                true
            } else false
        }.await()
    }

    /** Повертає життя, якщо минуло ≥10 хв і їх <10 */
    suspend fun maybeRestoreLife(uid: String, maxLives: Int = 10) =
        withContext(Dispatchers.IO) {
            db.runTransaction { tx ->
                val doc   = tx.get(ref(uid))
                val lives = (doc.getLong("lives") ?: maxLives.toLong()).toInt()
                if (lives >= maxLives) return@runTransaction     // уже максимум

                val last  = doc.getTimestamp("lastLifeTS") ?: return@runTransaction
                val diffM = (Timestamp.now().seconds - last.seconds) / 60
                if (diffM >= 2) {
                    tx.update(
                        ref(uid),
                        mapOf(
                            "lives"      to lives + 1,
                            "lastLifeTS" to FieldValue.serverTimestamp()
                        )
                    )
                }
            }.await()
        }

    /* ---------- hints ---------- */

    /** −1 hint. Повертає false, якщо підказки =0 */
    suspend fun spendHint(uid: String): Boolean =
        withContext(Dispatchers.IO) {
            db.runTransaction { tx ->
                val doc   = tx.get(ref(uid))
                val hints = (doc.getLong("hints") ?: 0).toInt()
                if (hints > 0) {
                    tx.update(ref(uid), "hints", hints - 1)
                    true
                } else false
            }.await()
        }

    /* ---------- coins ---------- */

    suspend fun addCoins(uid: String, amount: Int) =
        ref(uid).update("coins", FieldValue.increment(amount.toLong())).await()
}