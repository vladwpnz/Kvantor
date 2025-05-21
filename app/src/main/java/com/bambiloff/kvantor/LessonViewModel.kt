// File: app/src/main/java/com/bambiloff/kvantor/LessonViewModel.kt
package com.bambiloff.kvantor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class LessonViewModel(
    private val courseType: String = "python"   // "python" або "javascript"
) : ViewModel() {

    /* ---------------- Firebase ---------------- */
    private val db   = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    /* ---------------- Data ---------------- */
    private val _modules            = MutableStateFlow<List<Module>>(emptyList())
    val           modules: StateFlow<List<Module>> = _modules

    private val _currentModuleIndex = MutableStateFlow(0)
    val           currentModuleIndex: StateFlow<Int> = _currentModuleIndex

    private val _currentPageIndex   = MutableStateFlow(0)
    val           currentPageIndex:  StateFlow<Int> = _currentPageIndex

    /* ----------  Gamification  ---------- */
    private val _lives     = MutableStateFlow(0)
    val           lives:    StateFlow<Int> = _lives

    private val _hints     = MutableStateFlow(0)
    val           hints:    StateFlow<Int> = _hints

    private val _coins     = MutableStateFlow(0)
    val           coins:    StateFlow<Int> = _coins

    private val _showHint  = MutableStateFlow<String?>(null)
    val           showHint: StateFlow<String?> = _showHint

    /* ----------  last life timestamp & таймер ---------- */
    private val _lastLifeTS     = MutableStateFlow<Timestamp?>(null)
    private val _timeToNextLife = MutableStateFlow(0L)      // сек
    val           timeToNextLife: StateFlow<Long> = _timeToNextLife

    /* (можна реагувати у UI) */
    sealed interface UiEvent {
        object NoLives  : UiEvent
        object NoHints  : UiEvent
        object NoCoins  : UiEvent
    }
    private val _events = MutableSharedFlow<UiEvent>()
    val           events = _events.asSharedFlow()

    /* -------- константи -------- */
    private companion object {
        const val LIFE_COST        = 30
        const val HINT_COST        = 20
        const val MAX_LIVES        = 10
        const val RESTORE_INTERVAL = 2 * 60   // 10 хвилин = 600 с
    }

    /* ---------------- Current module helper ---------------- */
    val currentModule = combine(_modules, _currentModuleIndex) { list, idx ->
        list.getOrNull(idx)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    /* -------------------------------------------------------------------- */
    init {
        auth.currentUser?.uid?.let { uid ->
            /* ---- 1. live listener на документ користувача ---- */
            db.collection("users").document(uid)
                .addSnapshotListener { snap, _ ->
                    snap ?: return@addSnapshotListener
                    _lives.value      = (snap.getLong("lives") ?: 0).toInt()
                    _hints.value      = (snap.getLong("hints") ?: 0).toInt()
                    _coins.value      = (snap.getLong("coins") ?: 0).toInt()
                    _lastLifeTS.value = snap.getTimestamp("lastLifeTS")
                }

            /* ---- 2. одноразова ініціалізація ---- */
            viewModelScope.launch {
                UserBootstrapper.ensureStats(uid)
                GameManager.maybeRestoreLife(uid)
            }

            /* ---- 3. тікер 1 сек: рахуємо до наступного ❤️ ---- */
            viewModelScope.launch {
                while (true) {
                    delay(1_000)

                    val livesNow = _lives.value
                    if (livesNow >= MAX_LIVES) {
                        _timeToNextLife.value = 0
                        continue
                    }

                    val last = _lastLifeTS.value
                    if (last == null) { _timeToNextLife.value = 0; continue }

                    val passed = (Timestamp.now().seconds - last.seconds).toInt()
                    val left   = (RESTORE_INTERVAL - passed).coerceAtLeast(0)
                    _timeToNextLife.value = left.toLong()

                    if (left == 0) {
                        GameManager.maybeRestoreLife(uid, MAX_LIVES)
                    }
                }
            }
        }
    }

    /* ---------------- Modules loading ---------------- */
    fun loadModules() {
        viewModelScope.launch {
            val collection = if (courseType == "javascript") "modules_js" else "modules"
            try {
                val snapshot = db.collection(collection).get().await()
                _modules.value = snapshot.documents
                    .mapNotNull { it.toObject(ModuleDto::class.java)?.toModule() }
                    .sortedBy { it.id }

                restoreProgress()          // відновлюємо позицію
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /* ---------------- Progress save / restore ---------------- */
    fun saveProgress() {
        auth.currentUser?.uid?.let { uid ->
            val progress = mapOf(
                "moduleIndex" to _currentModuleIndex.value,
                "pageIndex"   to _currentPageIndex.value
            )
            db.collection("users").document(uid)
                .set(
                    mapOf("progress" to mapOf(courseType to progress)),
                    SetOptions.merge()
                )
        }
    }

    private fun restoreProgress() {
        viewModelScope.launch {
            auth.currentUser?.uid?.let { uid ->
                try {
                    val doc  = db.collection("users").document(uid).get().await()
                    @Suppress("UNCHECKED_CAST")
                    val root = doc.get("progress") as? Map<String, Map<String, Long>>
                    val cur  = root?.get(courseType)

                    val mIdx = (cur?.get("moduleIndex") ?: 0L).toInt()
                    val pIdx = (cur?.get("pageIndex")   ?: 0L).toInt()

                    _currentModuleIndex.value =
                        mIdx.coerceIn(0, _modules.value.lastIndex.coerceAtLeast(0))

                    val pagesInModule = _modules.value
                        .getOrNull(_currentModuleIndex.value)
                        ?.pages?.lastIndex ?: 0

                    _currentPageIndex.value =
                        pIdx.coerceIn(0, pagesInModule)

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    /* ---------------- Gamification helpers ---------------- */

    fun checkAnswer(page: Page.Test, userAnswerIndex: Int) = viewModelScope.launch {
        val uid = auth.currentUser?.uid ?: return@launch
        if (userAnswerIndex == page.correctAnswerIndex) {
            GameManager.addCoins(uid, 10)
        } else {
            val ok = GameManager.spendLife(uid)
            if (!ok) _events.emit(UiEvent.NoLives)
        }
    }

    fun requestHint(page: Page.Test) = viewModelScope.launch {
        val uid = auth.currentUser?.uid ?: return@launch
        val ok  = GameManager.spendHint(uid)
        if (ok) _showHint.value = page.hint else _events.emit(UiEvent.NoHints)
    }

    fun clearHint() { _showHint.value = null }

    fun buyLife() = viewModelScope.launch {
        val uid = auth.currentUser?.uid ?: return@launch
        val ok  = GameManager.buy(uid, LIFE_COST) { GameManager.addLives(uid, 1) }
        if (!ok) _events.emit(UiEvent.NoCoins)
    }

    fun buyHint() = viewModelScope.launch {
        val uid = auth.currentUser?.uid ?: return@launch
        val ok  = GameManager.buy(uid, HINT_COST) { GameManager.addHints(uid, 1) }
        if (!ok) _events.emit(UiEvent.NoCoins)
    }

    /* ---------------- Mark module completed ---------------- */
    private fun markModuleCompleted(moduleId: String) { /* без змін */ }

    /* ---------------- Навігація ---------------- */
    fun next() {
        if (_lives.value == 0) {
            viewModelScope.launch { _events.emit(UiEvent.NoLives) }
            return
        }
        viewModelScope.launch {
            val module = currentModule.value ?: return@launch
            val lastModuleIndex = _modules.value.lastIndex
            if (_currentPageIndex.value < module.pages.lastIndex) {
                _currentPageIndex.value += 1
            } else {
                markModuleCompleted(module.id)
                if (_currentModuleIndex.value < lastModuleIndex) {
                    _currentModuleIndex.value += 1
                    _currentPageIndex.value = 0
                } else {
                    auth.currentUser?.uid?.let { uid ->
                        CourseCompletionChecker.checkCourseCompleted(uid, courseType)
                    }
                }
            }
            saveProgress()
        }
    }
}
