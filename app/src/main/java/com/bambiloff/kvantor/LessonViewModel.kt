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

/**
 * ViewModel, що працює як для Python-курсу, так і для JavaScript-курсу.
 * Назва курсу передається в конструкторі (`courseType`).
 *
 *  – "python"      → читає колекцію "modules"
 *  – "javascript"  → читає колекцію "modules_js"
 *
 * Прогрес зберігається у полі `progress.{courseType}` у документі користувача.
 */
class LessonViewModel(
    private val courseType: String = "python"   // за замовчуванням – Python
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
    /** Завантажує модулі для поточного курсу і одразу відновлює прогрес. */
    fun loadModules() {
        viewModelScope.launch {
            val collection = when (courseType) {
                "javascript" -> "modules_js"
                else          -> "modules"           // python
            }

            try {
                val snapshot = db.collection(collection).get().await()
                val loaded   = snapshot.documents
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
    /** Зберігає позицію користувача у Firestore в progress.{courseType}. */
    private fun saveProgress() {
        val user = auth.currentUser ?: return

        val progressData = mapOf(
            "moduleIndex" to _currentModuleIndex.value,
            "pageIndex"   to _currentPageIndex.value
        )

        val data = mapOf(
            "progress" to mapOf(courseType to progressData)
        )

        db.collection("users")
            .document(user.uid)
            .set(data, SetOptions.merge())
            .addOnFailureListener { e -> println("❌ Save error: ${e.message}") }
    }

    /* ------------------------------------------------------------------------ */
    /** Відновлює позицію користувача з progress.{courseType}. */
    private fun restoreProgress() {
        val user = auth.currentUser ?: return
        viewModelScope.launch {
            try {
                val doc = db.collection("users").document(user.uid).get().await()

                @Suppress("UNCHECKED_CAST")
                val progressRoot = doc.get("progress") as? Map<String, Map<String, Long>>
                val thisCourse   = progressRoot?.get(courseType)

                val mIdx = (thisCourse?.get("moduleIndex") ?: 0L).toInt()
                val pIdx = (thisCourse?.get("pageIndex")   ?: 0L).toInt()

                // clamp module index
                val maxModule = _modules.value.lastIndex.coerceAtLeast(0)
                _currentModuleIndex.value = mIdx.coerceIn(0, maxModule)

                // clamp page index
                val pageCount = _modules.value.getOrNull(_currentModuleIndex.value)?.pages?.size ?: 1
                val maxPage   = (pageCount - 1).coerceAtLeast(0)
                _currentPageIndex.value = pIdx.coerceIn(0, maxPage)

                println("📥 Progress restored ($courseType): module=$mIdx page=$pIdx")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /* ------------------------------------------------------------------------ */
    /** Позначає модуль завершеним і оновлює поле completedModules. */

    @Suppress("UNCHECKED_CAST")
    private fun markModuleCompleted(moduleId: String) {
        val user = auth.currentUser ?: return
        val ref  = db.collection("users").document(user.uid)

        db.runTransaction { tx ->
            val done = tx.get(ref).get("completedModules") as? List<String> ?: emptyList()
            if (moduleId !in done) tx.update(ref, "completedModules", done + moduleId)
        }.addOnSuccessListener {
            println("✅ Module completed: $moduleId ($courseType)")
        }
    }

    /* ------------------------------------------------------------------------ */
    /**
     * Кнопка «Далі»:
     *  – переходить на наступну сторінку, якщо ще є;
     *  – інакше позначає модуль завершеним і переходить до наступного модуля.
     *  Після кожного кроку прогрес зберігається.
     */
    fun next() {
        val mod = currentModule.value ?: return

        if (_currentPageIndex.value < mod.pages.lastIndex) {
            _currentPageIndex.value += 1
        } else {
            markModuleCompleted(mod.id)
            if (_currentModuleIndex.value < _modules.value.lastIndex) {
                _currentModuleIndex.value += 1
                _currentPageIndex.value = 0
            }
        }
        saveProgress()
    }
}