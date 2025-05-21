// File: app/src/main/java/com/bambiloff/kvantor/CourseCompletionChecker.kt
package com.bambiloff.kvantor

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object CourseCompletionChecker {

    /**
     * Перевіряє завершення будь-якого курсу (python/js) за назвою колекції,
     * порівнює з тим, скільки модулів вже є в списку completedModules у документі користувача.
     */
    suspend fun checkCourseCompleted(uid: String, courseType: String) {
        val db = FirebaseFirestore.getInstance()

        // обираємо вашу колекцію з модулями
        val modulesCol = if (courseType == "javascript") "modules_js" else "modules"

        // загальна кількість модулів у курсі
        val totalModules = db.collection(modulesCol)
            .get().await().size()

        // читаємо поле completedModules із документа users/{uid}
        val userDoc = db.collection("users").document(uid).get().await()
        val doneAny = userDoc.get("completedModules")
        val doneList = (doneAny as? List<*>)?.filterIsInstance<String>() ?: emptyList()

        // якщо всі модулі позначені — розблокуємо ачивку
        if (totalModules > 0 && doneList.size == totalModules) {
            val achId = if (courseType == "python") "PY_MASTER" else "JS_SAMURAI"
            AchievementManager.unlockAchievement(uid, achId)
            println("🏆 [$courseType] achievement unlocked via CourseCompletionChecker")
        }
    }
}
