package com.bambiloff.kvantor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bambiloff.kvantor.ui.*
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

/* ───────────── Activity ───────────── */
class LessonActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val courseType = intent.getStringExtra("courseType") ?: "python"
        val uid        = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                LessonViewModel(courseType).apply { loadModules() } as T
        }

        setContent {
            val vm: LessonViewModel = viewModel(factory = factory)
            LessonScreen(
                viewModel    = vm,
                courseType   = courseType,
                uid          = uid,           // передаємо UID
                onBackToMenu = { finish() }
            )
        }
    }
}

/* ───────────── Екран з модулями ───────────── */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun LessonScreen(
    viewModel: LessonViewModel,
    courseType: String,
    uid: String,
    onBackToMenu: () -> Unit
) {
    val modules        by viewModel.modules.collectAsState()
    val currentModIdx  by viewModel.currentModuleIndex.collectAsState()
    val currentPageIdx by viewModel.currentPageIndex.collectAsState()

    val pageCount = modules.getOrNull(currentModIdx)?.pages?.size ?: 1
    val progress  = ((currentPageIdx + 1).coerceAtMost(pageCount)).toFloat() / pageCount

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kvantor", color = KvTextColor) },
                actions = {
                    TextButton(onClick = onBackToMenu) {
                        Text("Меню", color = KvTextColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = KvBg)
            )
        },
        bottomBar = {
            Column {
                HorizontalDivider(color = KvAccent.copy(.3f), thickness = 1.dp)
                LinearProgressIndicator(
                    progress   = { progress },
                    modifier   = Modifier.fillMaxWidth().height(4.dp),
                    color      = KvAccent,
                    trackColor = KvAccent.copy(.1f)
                )
            }
        },
        containerColor = KvBg
    ) { padding ->
        PageContainer(Modifier.padding(padding)) {
            when {
                modules.isEmpty() -> Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator(color = KvAccent) }

                currentModIdx < modules.size -> LessonModuleContent(
                    module       = modules[currentModIdx],
                    pageIndex    = currentPageIdx,
                    isLastModule = currentModIdx == modules.lastIndex,
                    courseType   = courseType,
                    uid          = uid,              // ← передаємо
                    onNext       = viewModel::next,
                    onBackToMenu = onBackToMenu
                )

                else -> CourseFinishedScreen(onBackToMenu)
            }
        }
    }
}

/* ───────────── Контент сторінки ───────────── */
@Composable
fun LessonModuleContent(
    module: Module,
    pageIndex: Int,
    isLastModule: Boolean,
    courseType: String,
    uid: String,
    onNext: () -> Unit,
    onBackToMenu: () -> Unit
) {
    val page = module.pages.getOrNull(pageIndex)
    var done by remember(pageIndex) { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        /* HERO‑іконка */
        AnimatedVisibility(visible = true, enter = fadeIn()) {
            if (courseType == "javascript") {
                Icon(
                    Icons.Filled.Code,
                    contentDescription = null,
                    tint     = KvAccent,
                    modifier = Modifier.size(72.dp)
                )
            } else {
                Text("💻", fontSize = 64.sp)
            }
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text      = "Модуль: ${module.title}",
            style     = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color     = KvTextColor,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(32.dp))

        /* ——— контент сторінки ——— */
        when (page) {
            is Page.Theory     -> Text(page.text, color = KvTextColor, textAlign = TextAlign.Center)
                .also { done = true }

            is Page.Test       -> TestPage(page)       { done = true }
            is Page.CodingTask -> CodingTaskView(page) { done = true }

            is Page.Final      -> {
                // 1. Розблоковуємо ачивку один раз
                LaunchedEffect(uid, courseType) {
                    val achId = if (courseType == "python") "PY_MASTER" else "JS_SAMURAI"
                    AchievementManager.unlockAchievement(uid, achId)
                }

                // 2. Показуємо фінальний текст і кнопку
                Text(page.message, color = KvTextColor, textAlign = TextAlign.Center)
                done = true
                Spacer(Modifier.height(32.dp))
                KvButton(
                    text    = if (isLastModule) "Повернутися в меню" else "До наступного модуля",
                    onClick = if (isLastModule) onBackToMenu else onNext
                )
            }

            null -> {}
        }

        if (done && page !is Page.Final) {
            Spacer(Modifier.height(32.dp))
            KvButton("Далі", onClick = onNext)
        }
    }
}

/* ───────────── Фінальний екран ───────────── */
@Composable
fun CourseFinishedScreen(onBackToMenu: () -> Unit) {
    Column(
        Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "🎉 Вітаємо!\nВи завершили всі модулі.",
            style     = MaterialTheme.typography.titleLarge,
            color     = KvTextColor,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(32.dp))
        KvButton("Повернутися в меню", onClick = onBackToMenu)
    }
}

/* ───────────── Сторінка‑тест ───────────── */
@Composable
fun TestPage(test: Page.Test, onDone: () -> Unit) {
    var selected by remember(test) { mutableStateOf(-1) }
    var result   by remember(test) { mutableStateOf<Boolean?>(null) }

    Text(test.question, color = KvTextColor, textAlign = TextAlign.Center)
    Spacer(Modifier.height(24.dp))

    test.answers.forEachIndexed { i, ans ->
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = selected == i,
                onClick  = { selected = i; result = null },
                colors   = RadioButtonDefaults.colors(
                    selectedColor   = KvTextColor,
                    unselectedColor = KvTextColor
                )
            )
            Spacer(Modifier.width(8.dp))
            Text(ans, color = KvTextColor)
        }
    }

    Spacer(Modifier.height(24.dp))

    KvButton("Перевірити", enabled = selected != -1) {
        result = selected == test.correctAnswerIndex
        onDone()
    }

    result?.let {
        Spacer(Modifier.height(16.dp))
        Text(
            if (it) "✅ Правильно" else "❌ Неправильно",
            color     = if (it) KvAccent else KvAccent.copy(.7f),
            textAlign = TextAlign.Center
        )
    }
}
