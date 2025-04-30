package com.bambiloff.kvantor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextAlign

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bambiloff.kvantor.ui.*    // PageContainer, KvButton, KvBg, KvAccent, KvTextColor


class LessonActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val courseType = intent.getStringExtra("courseType") ?: "python"
        val factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                LessonViewModel(courseType).apply { loadModules() } as T
        }

        setContent {
            val vm: LessonViewModel = viewModel(factory = factory)
            LessonScreen(vm) { finish() }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun LessonScreen(
    viewModel: LessonViewModel,
    onBackToMenu: () -> Unit
) {
    val modules        by viewModel.modules.collectAsState()
    val currentModIdx  by viewModel.currentModuleIndex.collectAsState()
    val currentPageIdx by viewModel.currentPageIndex.collectAsState()

    // Загальна кількість сторінок у поточному модулі
    val pageCount = modules
        .getOrNull(currentModIdx)
        ?.pages
        ?.size
        ?: 1

    // Від 0f до 1f
    val progress = ((currentPageIdx + 1).coerceAtMost(pageCount))
        .toFloat() / pageCount.toFloat()

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
        // Нижній бар із лінією прогресу
        bottomBar = {
            Column {
                HorizontalDivider(
                    color = KvAccent.copy(alpha = 0.3f),
                    thickness = 1.dp
                )
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp),
                    trackColor = KvAccent.copy(alpha = 0.1f),
                    color      = KvAccent
                )
            }
        },
        containerColor = KvBg
    ) { padding ->
        PageContainer(Modifier.padding(padding)) {
            when {
                modules.isEmpty() -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = KvAccent)
                }
                currentModIdx < modules.size -> LessonModuleContent(
                    module       = modules[currentModIdx],
                    pageIndex    = currentPageIdx,
                    isLastModule = currentModIdx == modules.lastIndex,
                    onNext       = viewModel::next,
                    onBackToMenu = onBackToMenu
                )
                else -> CourseFinishedScreen(onBackToMenu)
            }
        }
    }
}

@Composable
fun LessonModuleContent(
    module: Module,
    pageIndex: Int,
    isLastModule: Boolean,
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
        verticalArrangement   = Arrangement.Center
    ) {
        Text(
            text      = "Модуль: ${module.title}",
            style     = MaterialTheme.typography.titleLarge,
            color     = KvTextColor,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(32.dp))

        when (page) {
            is Page.Theory      -> Text(page.text, color = KvTextColor, textAlign = TextAlign.Center)
                .also { done = true }
            is Page.Test        -> TestPage(page) { done = true }
            is Page.CodingTask  -> CodingTaskView(page) { done = true }
            is Page.Final       -> {
                Text(page.message, color = KvTextColor, textAlign = TextAlign.Center)
                done = true
                Spacer(Modifier.height(32.dp))
                KvButton(
                    text    = if (isLastModule) "Повернутися в меню" else "До наступного модуля",
                    onClick = if (isLastModule) onBackToMenu else onNext
                )
            }
            null                -> { }
        }

        if (done && page !is Page.Final) {
            Spacer(Modifier.height(32.dp))
            KvButton("Далі", onClick = onNext)
        }
    }
}

@Composable
fun CourseFinishedScreen(onBackToMenu: () -> Unit) {
    Column(
        modifier             = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement  = Arrangement.Center
    ) {
        Text(
            text      = "🎉 Вітаємо!\nВи завершили всі модулі.",
            style     = MaterialTheme.typography.titleLarge,
            color     = KvTextColor,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(32.dp))
        KvButton("Повернутися в меню", onClick = onBackToMenu)
    }
}

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
            text      = if (it) "✅ Правильно" else "❌ Неправильно",
            color     = if (it) KvAccent else KvAccent.copy(alpha = .7f),
            textAlign = TextAlign.Center
        )
    }
}
