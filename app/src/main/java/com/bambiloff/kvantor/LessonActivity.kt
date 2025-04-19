package com.bambiloff.kvantor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

class LessonActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { LessonScreen() }
    }
}

/* ---------- Головний екран уроку ---------- */

@Composable
fun LessonScreen(viewModel: LessonViewModel = viewModel()) {

    val modules     by viewModel.modules.collectAsState()
    val moduleIndex by viewModel.currentModuleIndex.collectAsState()
    val pageIndex   by viewModel.currentPageIndex.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadModules() }

    when {
        modules.isEmpty() -> Box(Modifier.fillMaxSize(), Alignment.Center) {
            CircularProgressIndicator()
        }

        moduleIndex < modules.size -> {
            LessonModuleContent(
                module    = modules[moduleIndex],
                pageIndex = pageIndex,
                onNext    = viewModel::next
            )
        }

        else -> Box(Modifier.fillMaxSize(), Alignment.Center) {
            Text("🎉 Вітаємо! Ви завершили всі модулі.")
        }
    }
}

/* ---------- Вміст одного модуля ---------- */

@Composable
fun LessonModuleContent(
    module: Module,
    pageIndex: Int,
    onNext: () -> Unit
) {
    val page = module.pages.getOrNull(pageIndex)
    var isPageComplete by remember(pageIndex) { mutableStateOf(false) }

    Scaffold { padding ->
        Column(
            Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {

            Text("Модуль: ${module.title}", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(16.dp))

            page?.let {
                when (it) {
                    is Page.Theory -> {
                        Text(it.text)
                        isPageComplete = true
                    }

                    is Page.Test ->
                        TestPage(it) { isPageComplete = true }

                    is Page.CodingTask ->
                        CodingTaskView(it) {
                            isPageComplete = true        // ✅ «Далі» з’являється після будь‑якої спроби
                        }

                    is Page.Final -> {
                        Text(it.message)
                        isPageComplete = true

                        Spacer(Modifier.height(24.dp))
                        Button(onClick = onNext) {
                            Text("До наступного модуля")
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            if (isPageComplete && page !is Page.Final) {
                Button(onClick = onNext) { Text("Далі") }
            }
        }
    }
}

/* ---------- Сторінка‑тест ---------- */

@Composable
fun TestPage(test: Page.Test, onSubmitted: () -> Unit) {
    /* 🔸  стан «підв’язуємо» до самого питання, тож
       при переході на інше питання усе скидається  */
    var selectedIndex by remember(test) { mutableStateOf(-1) }
    var submitted     by remember(test) { mutableStateOf(false) }

    Text("Питання: ${test.question}")
    Spacer(Modifier.height(8.dp))

    test.answers.forEachIndexed { index, answer ->
        Row(Modifier.padding(4.dp)) {
            RadioButton(
                selected = selectedIndex == index,
                /* 🔸 дозволяємо міняти відповідь навіть після сабміту */
                onClick  = {
                    selectedIndex = index
                    /* якщо вже натискав «Перевірити» — обнуляємо, аби можна було
                       перевірити ще раз */
                    submitted = false
                }
            )
            Text(answer, Modifier.padding(start = 8.dp))
        }
    }

    Spacer(Modifier.height(8.dp))

    Button(
        onClick = {
            submitted = true
            onSubmitted()          // 👉 повідомляємо екран, щоби показати «Далі»
        },
        enabled = selectedIndex != -1              // щось вибрано
                && (!submitted)                  // ще не перевірили
    ) { Text("Перевірити") }

    if (submitted) {
        val correct = selectedIndex == test.correctAnswerIndex
        Text(if (correct) "✅ Правильно!" else "❌ Неправильно")
        /* 🔸 якщо помилився — учень може змінити вибір і натиснути ще раз */
    }
}


/* ---------- Сторінка‑кодинг‑челендж ---------- */

@Composable
fun CodingTaskPage(task: Page.CodingTask, onAnsweredCorrect: () -> Unit) {
    var userCode    by remember { mutableStateOf("") }
    var checkResult by remember { mutableStateOf<Boolean?>(null) }

    Text("Завдання: ${task.description}")
    Spacer(Modifier.height(8.dp))

    OutlinedTextField(
        value = userCode,
        onValueChange = { userCode = it },
        label = { Text("Ваш код") },
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
    )

    Spacer(Modifier.height(8.dp))

    Button(
        onClick = {
            val ok = userCode.trim() == task.expectedCode.trim()
            checkResult = ok
            if (ok) onAnsweredCorrect()
        }
    ) { Text("Надіслати") }

    Spacer(Modifier.height(8.dp))

    when (checkResult) {
        true  -> Text("✅ Відповідь правильна")
        false -> Column {
            Text("❌ Спробуй ще")
            Text("Правильний приклад: ${task.expectedCode}")
        }
        null  -> {}
    }
}
