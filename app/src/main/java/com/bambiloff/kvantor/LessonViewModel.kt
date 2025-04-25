package com.bambiloff.kvantor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class LessonViewModel : ViewModel() {

    private val db   = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _modules = MutableStateFlow<List<Module>>(emptyList())
    val modules: StateFlow<List<Module>> = _modules

    private val _currentModuleIndex = MutableStateFlow(0)
    val currentModuleIndex: StateFlow<Int> = _currentModuleIndex

    private val _currentPageIndex = MutableStateFlow(0)
    val currentPageIndex: StateFlow<Int> = _currentPageIndex

    val currentModule: StateFlow<Module?> =
        combine(_modules, _currentModuleIndex) { list, idx -> list.getOrNull(idx) }
            .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    /** Завантажує всі модулі і відразу намагається відновити прогрес */
    fun loadModules() {
        viewModelScope.launch {
            try {
                val snapshot = db.collection("modules").get().await()
                val loaded = snapshot.documents
                    .mapNotNull { it.toObject(ModuleDto::class.java)?.toModule() }
                    .sortedBy { it.id }
                _modules.value = loaded
                loadProgress()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /** Зберігає поточний стан (moduleIndex + pageIndex) в Firestore */
    private fun saveProgress() {
        val user = auth.currentUser ?: return
        val data = mapOf(
            "moduleIndex" to _currentModuleIndex.value,
            "pageIndex"   to _currentPageIndex.value
        )
        db.collection("users")
            .document(user.uid)
            .update("progress", data)
            .addOnSuccessListener { println("✅ Прогрес збережено: $data") }
            .addOnFailureListener { e -> println("❌ Save error: ${e.message}") }
    }

    /** Відновлює стан із Firestore і кладе в stateFlow, із clamp індексів */
    private fun loadProgress() {
        val user = auth.currentUser ?: return
        viewModelScope.launch {
            try {
                val doc = db.collection("users").document(user.uid).get().await()
                @Suppress("UNCHECKED_CAST")
                val prog = doc.get("progress") as? Map<String, Any>
                val mIdx = (prog?.get("moduleIndex") as? Long)?.toInt() ?: 0
                val pIdx = (prog?.get("pageIndex")   as? Long)?.toInt() ?: 0

                // Clamp модуля
                val maxModule = _modules.value.lastIndex.coerceAtLeast(0)
                _currentModuleIndex.value = mIdx.coerceIn(0, maxModule)

                // Clamp сторінки
                val pages   = _modules.value.getOrNull(_currentModuleIndex.value)?.pages?.size ?: 1
                val maxPage = (pages - 1).coerceAtLeast(0)
                _currentPageIndex.value = pIdx.coerceIn(0, maxPage)

                println("📥 Прогрес відновлено: module=$mIdx, page=$pIdx")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /** Позначає модуль як завершений у списку користувача */
    private fun markModuleCompleted(moduleId: String) {
        val user = auth.currentUser ?: return
        val ref  = db.collection("users").document(user.uid)
        db.runTransaction { tx ->
            val done = tx.get(ref).get("completedModules") as? List<String> ?: emptyList()
            if (moduleId !in done) tx.update(ref, "completedModules", done + moduleId)
        }.addOnSuccessListener {
            println("✅ Module completed: $moduleId")
        }
    }

    /**
     * Навігація «Далі»:
     * – ++pageIndex, якщо в модулі ще є сторінки
     * – інакше — markModuleCompleted + ++moduleIndex + pageIndex = 0
     * Після кожного кроку зберігає прогрес.
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
