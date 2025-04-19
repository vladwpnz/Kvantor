package com.bambiloff.kvantor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Тримає увесь стан уроків:
 *   – список модулів з Firestore
 *   – поточний модуль / сторінку
 *   – навігацію «Далі»
 */
class LessonViewModel : ViewModel() {

    /* ---------- Firebase ---------- */
    private val db = FirebaseFirestore.getInstance()

    /* ---------- STATE ---------- */

    /** Усі модулі, що прийшли з БД */
    private val _modules = MutableStateFlow<List<Module>>(emptyList())
    val modules: StateFlow<List<Module>> = _modules

    /** Індекс активного модуля */
    private val _currentModuleIndex = MutableStateFlow(0)
    val currentModuleIndex: StateFlow<Int> = _currentModuleIndex

    /** Індекс сторінки в середині активного модуля */
    private val _currentPageIndex = MutableStateFlow(0)
    val currentPageIndex: StateFlow<Int> = _currentPageIndex

    /** Корисний «курсор» на активний модуль */
    val currentModule: StateFlow<Module?> =
        combine(_modules, _currentModuleIndex) { list, idx ->
            list.getOrNull(idx)
        }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    /* ---------- Завантаження модулів ---------- */

    /** Один раз при старті витягуємо модулі з Firestore */
    fun loadModules() = viewModelScope.launch {
        try {
            val snapshot = db.collection("modules").get().await()
            val loaded   = snapshot.documents
                .mapNotNull { it.toObject(ModuleDto::class.java)?.toModule() }
                .sortedBy   { it.id }

            _modules.value = loaded
            println("Завантажені модулі: ${loaded.map { it.id }}")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /* ---------- Фіксуємо, що модуль пройдено ---------- */

    private fun markModuleCompleted(moduleId: String) {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val userDoc = db.collection("users").document(user.uid)

        db.runTransaction { txn ->
            val done = txn.get(userDoc).get("completedModules") as? List<String> ?: emptyList()
            if (moduleId !in done) txn.update(userDoc, "completedModules", done + moduleId)
        }.addOnSuccessListener {
            println("✅ Модуль '$moduleId' збережено.")
        }
    }

    /* ---------- НАВІГАЦІЯ «Далі» ---------- */

    /**
     * Якщо всередині модуля є ще сторінки — йдемо до них.
     * Інакше — переходимо на наступний модуль і починаємо його з 0‑ї сторінки.
     */
    fun next() {
        val module = currentModule.value ?: return

        if (_currentPageIndex.value < module.pages.lastIndex) {
            // є ще сторінки
            _currentPageIndex.value += 1
        } else {
            // модуль закінчився
            markModuleCompleted(module.id)

            if (_currentModuleIndex.value < _modules.value.lastIndex) {
                _currentModuleIndex.value += 1   // наступний модуль
                _currentPageIndex.value  = 0     // з початку
            }
        }
    }
}
