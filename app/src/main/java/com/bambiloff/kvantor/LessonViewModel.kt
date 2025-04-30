package com.bambiloff.kvantor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class LessonViewModel(
    private val courseType: String = "python"   // "python" або "javascript"
) : ViewModel() {

    /* ---------------- Firebase ---------------- */
    private val db   = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    /* ---------------- State ---------------- */
    private val _modules = MutableStateFlow<List<Module>>(emptyList())
    val modules: StateFlow<List<Module>> = _modules

    private val _currentModuleIndex = MutableStateFlow(0)
    val currentModuleIndex: StateFlow<Int> = _currentModuleIndex

    private val _currentPageIndex = MutableStateFlow(0)
    val currentPageIndex: StateFlow<Int> = _currentPageIndex

    val currentModule =
        combine(_modules, _currentModuleIndex) { list, idx -> list.getOrNull(idx) }
            .stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.Eagerly, null)

    /* ------------------------------------------------------------------------ */
    /** Завантажує модулі і відновлює прогрес */
    fun loadModules() {
        viewModelScope.launch {
            val collection = if (courseType == "javascript") "modules_js" else "modules"
            try {
                val snapshot = db.collection(collection).get().await()
                val loaded = snapshot.documents
                    .mapNotNull { it.toObject(ModuleDto::class.java)?.toModule() }
                    .sortedBy { it.id }
                _modules.value = loaded
                restoreProgress()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /* ------------------------------------------------------------------------ */
    /** Зберігає позицію користувача */
    private fun saveProgress() {
        auth.currentUser?.uid?.let { uid ->
            val progressData = mapOf(
                "moduleIndex" to _currentModuleIndex.value,
                "pageIndex"   to _currentPageIndex.value
            )
            val data = mapOf("progress" to mapOf(courseType to progressData))
            db.collection("users")
                .document(uid)
                .set(data, SetOptions.merge())
                .addOnFailureListener { e -> println("❌ Save error: ${e.message}") }
        }
    }

    /* ------------------------------------------------------------------------ */
    /** Відновлює позицію користувача */
    private fun restoreProgress() {
        viewModelScope.launch {
            auth.currentUser?.uid?.let { uid ->
                try {
                    val doc = db.collection("users").document(uid).get().await()
                    @Suppress("UNCHECKED_CAST")
                    val root = doc.get("progress") as? Map<String, Map<String, Long>>
                    val course = root?.get(courseType)
                    val mIdx = (course?.get("moduleIndex") ?: 0L).toInt()
                    val pIdx = (course?.get("pageIndex")   ?: 0L).toInt()
                    val maxModule = _modules.value.lastIndex.coerceAtLeast(0)
                    _currentModuleIndex.value = mIdx.coerceIn(0, maxModule)
                    val pageCount = _modules.value
                        .getOrNull(_currentModuleIndex.value)
                        ?.pages
                        ?.size ?: 1
                    _currentPageIndex.value = pIdx.coerceIn(0, pageCount - 1)
                    println("📥 Restored ($courseType): module=$mIdx page=$pIdx")
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    /* ------------------------------------------------------------------------ */
    /** Позначає модуль завершеним */
    private fun markModuleCompleted(moduleId: String) {
        auth.currentUser?.uid?.let { uid ->
            val ref = db.collection("users").document(uid)
            db.runTransaction { tx ->
                val done = tx.get(ref).get("completedModules") as? List<String> ?: emptyList()
                if (moduleId !in done) {
                    tx.update(ref, "completedModules", done + moduleId)
                }
            }.addOnSuccessListener {
                println("✅ Module completed: $moduleId ($courseType)")
            }
        }
    }

    /* ------------------------------------------------------------------------ */
    /**
     * Натиск “Далі”:
     * – переходить на наступну сторінку, або
     * – завершує модуль, або
     * – при завершенні останнього модуля — розблоковує ачівку курсу.
     */
    fun next() {
        viewModelScope.launch {
            val mod = currentModule.value ?: return@launch
            val lastModuleIndex = _modules.value.lastIndex

            if (_currentPageIndex.value < mod.pages.lastIndex) {
                // рухаємося по сторінках у межах модуля
                _currentPageIndex.value += 1
            } else {
                // модуль завершено
                markModuleCompleted(mod.id)

                if (_currentModuleIndex.value < lastModuleIndex) {
                    // переходимо до наступного модуля
                    _currentModuleIndex.value += 1
                    _currentPageIndex.value = 0
                } else {
                    // курс завершено — розблоковуємо відповідну ачівку
                    auth.currentUser?.uid?.let { uid ->
                        when (courseType) {
                            "python"     -> AchievementManager.unlockAchievement(uid, "PY_MASTER")
                            "javascript" -> AchievementManager.unlockAchievement(uid, "JS_SAMURAI")
                        }
                        println("🏆 Course achievement unlocked for $courseType")
                    }
                }
            }

            // зберігаємо прогрес (індекси)
            saveProgress()
        }
    }
}
