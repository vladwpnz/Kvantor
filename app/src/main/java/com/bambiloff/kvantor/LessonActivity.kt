package com.bambiloff.kvantor

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bambiloff.kvantor.ui.*   // PageContainer, KvButton, KvBg, KvAccent, KvTextColor
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

class LessonActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // передаємо finish() щоб з кнопки «Меню» повертати назад
        setContent { LessonScreen(onBackToMenu = { finish() }) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonScreen(
    viewModel: LessonViewModel = viewModel(),
    onBackToMenu: () -> Unit
) {
    val modules   by viewModel.modules.collectAsState()
    val mIndex    by viewModel.currentModuleIndex.collectAsState()
    val pIndex    by viewModel.currentPageIndex.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadModules() }

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
        containerColor = KvBg
    ) { padding ->
        PageContainer(Modifier.padding(padding)) {
            when {
                modules.isEmpty() -> {
                    CircularProgressIndicator(color = KvAccent)
                }
                mIndex < modules.size -> {
                    LessonModuleContent(
                        module       = modules[mIndex],
                        pageIndex    = pIndex,
                        isLastModule = mIndex == modules.lastIndex,
                        onNext       = viewModel::next,
                        onBackToMenu = onBackToMenu
                    )
                }
                else -> {
                    CourseFinishedScreen(onBackToMenu)
                }
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
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Модуль: ${module.title}",
            style     = MaterialTheme.typography.titleLarge,
            color     = KvTextColor,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(32.dp))

        when (page) {
            is Page.Theory -> {
                Text(page.text, color = KvTextColor, textAlign = TextAlign.Center)
                done = true
            }
            is Page.Test -> {
                TestPage(page) { done = true }
            }
            is Page.CodingTask -> {
                CodingTaskPage(page) { done = true }
            }
            is Page.Final -> {
                Text(page.message, color = KvTextColor, textAlign = TextAlign.Center)
                done = true
                Spacer(Modifier.height(32.dp))
                KvButton(
                    text = if (isLastModule) "Повернутися в меню" else "До наступного модуля",
                    onClick = if (isLastModule) onBackToMenu else onNext
                )
            }
            null -> { /* нічого */ }
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
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "🎉 Вітаємо!\nВи завершили всі модулі.",
            color     = KvTextColor,
            textAlign = TextAlign.Center,
            style     = MaterialTheme.typography.titleLarge
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
                onClick = { selected = i; result = null },
                colors = RadioButtonDefaults.colors(
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
            color     = KvTextColor,
            textAlign = TextAlign.Center
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CodingTaskPage(task: Page.CodingTask, onAttempt: () -> Unit) {
    val focus = LocalFocusManager.current
    var userCode    by remember(task) { mutableStateOf("") }
    var checkResult by remember(task) { mutableStateOf<Boolean?>(null) }

    Column(
        Modifier
            .fillMaxWidth()
            .imePadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(task.description, color = KvTextColor, textAlign = TextAlign.Center)
        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = userCode,
            onValueChange = { userCode = it },
            label    = { Text("Ваш код") },
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor   = KvAccent,
                unfocusedBorderColor = KvAccent.copy(alpha = .4f),
                cursorColor          = KvAccent,
                focusedLabelColor    = KvTextColor,
                unfocusedLabelColor  = KvTextColor.copy(alpha = .6f),
                focusedTextColor     = KvTextColor,
                unfocusedTextColor   = KvTextColor
            ),
            keyboardActions = KeyboardActions { focus.clearFocus() }
        )

        Spacer(Modifier.height(24.dp))

        KvButton("Надіслати") {
            focus.clearFocus()
            checkResult = userCode.trim() == task.expectedCode.trim()
            onAttempt()
        }

        checkResult?.let { ok ->
            Spacer(Modifier.height(16.dp))
            if (ok) {
                Text("✅ Відповідь правильна", color = KvTextColor)
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("❌ Спробуй ще", color = KvTextColor)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Приклад розв’язку:\n${task.expectedCode}",
                        color     = KvTextColor,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}



