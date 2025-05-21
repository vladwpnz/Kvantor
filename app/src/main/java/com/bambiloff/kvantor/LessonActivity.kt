package com.bambiloff.kvantor

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bambiloff.kvantor.ui.*
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.collectLatest

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
                uid          = uid,
                onBackToMenu = { finish() }
            )
        }
    }
}

/* ───────────── Екран з модулями ───────────── */
@SuppressLint("DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun LessonScreen(
    viewModel: LessonViewModel,
    courseType: String,
    uid: String,
    onBackToMenu: () -> Unit
) {
    /* ----------- стани ------------- */
    val modules        by viewModel.modules.collectAsState()
    val currentModIdx  by viewModel.currentModuleIndex.collectAsState()
    val currentPageIdx by viewModel.currentPageIndex.collectAsState()

    val lives          by viewModel.lives.collectAsState()
    val hints          by viewModel.hints.collectAsState()
    val coins          by viewModel.coins.collectAsState()
    val showHint       by viewModel.showHint.collectAsState()
    val timeLeft   by viewModel.timeToNextLife.collectAsState()

    val livesLabel = if (timeLeft > 0 && lives < 10)
        "$lives (${String.format("%02d:%02d", timeLeft/60, timeLeft%60)})"
    else "$lives"

    val snack = remember { SnackbarHostState() }


    /* ловимо повідомлення-події від VM */
    LaunchedEffect(Unit) {
        viewModel.events.collectLatest {
            when (it) {
                LessonViewModel.UiEvent.NoLives ->
                    snack.showSnackbar("У вас закінчились життя. Спробуйте пізніше")
                LessonViewModel.UiEvent.NoHints ->
                    snack.showSnackbar("Підказок більше нема")
                LessonViewModel.UiEvent.NoCoins ->      // ← нова гілка
                    snack.showSnackbar("Недостатньо монет для покупки")
            }
        }
    }

    /* ----------- прогрес ------------- */
    val pageCount = modules.getOrNull(currentModIdx)?.pages?.size ?: 1
    val progress  = ((currentPageIdx + 1).coerceAtMost(pageCount)).toFloat() / pageCount

    Scaffold(
        snackbarHost = { SnackbarHost(snack) },

        /* ---- TOP ---- */
        topBar = {
            TopAppBar(
                title = { Text("Kvantor", color = KvTextColor) },
                actions = {
                    TextButton(
                        onClick = {
                            viewModel.saveProgress()   // ← додано
                            onBackToMenu()
                        }
                    ) {
                        Text("Меню", color = KvTextColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = KvBg)
            )
        },

        /* ---- BOTTOM ---- */
        bottomBar = {
            Column {
                /* статус-бар з життя/підказки/монети */
                Row(
                    Modifier
                        .fillMaxWidth()
                        .background(KvBg)
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatusChip(Icons.Default.Favorite,       livesLabel, "Lives")   // ← livesLabel
                    StatusChip(Icons.Default.Lightbulb,      hints.toString(),  "Hints")
                    StatusChip(Icons.Default.MonetizationOn, coins.toString(),  "Coins")
                }

                /* прогрес */
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
                    uid          = uid,
                    vm           = viewModel,          // ← передаємо VM
                    onNext       = viewModel::next,
                    onBackToMenu = onBackToMenu
                )

                else -> CourseFinishedScreen(onBackToMenu)
            }

            /* показуємо підказку під усім контентом (як snackbar) */
            showHint?.let { hint ->
                LaunchedEffect(hint) {
                    snack.showSnackbar("💡 $hint")
                    viewModel.clearHint()
                }
            }
        }
    }
}

/* ───────────── Контент модуля ───────────── */
@Composable
fun LessonModuleContent(
    module: Module,
    pageIndex: Int,
    isLastModule: Boolean,
    courseType: String,
    uid: String,
    vm: LessonViewModel,          // отримали
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
        /* HERO-іконка */
        AnimatedVisibility(visible = true, enter = fadeIn()) {
            if (courseType == "javascript") {
                Icon(Icons.Filled.Code, null, tint = KvAccent, modifier = Modifier.size(72.dp))
            } else Text("💻", fontSize = 64.sp)
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

            is Page.Test       -> TestPage(page, vm) { done = true }
            is Page.CodingTask -> CodingTaskView(page) { done = true }

            is Page.Final      -> {
                LaunchedEffect(uid, courseType) {
                    val achId = if (courseType == "python") "PY_MASTER" else "JS_SAMURAI"
                    AchievementManager.unlockAchievement(uid, achId)
                }

                Text(page.message, color = KvTextColor, textAlign = TextAlign.Center)
                done = true
                Spacer(Modifier.height(32.dp))
                KvantorButton(
                    text    = if (isLastModule) "Повернутися в меню" else "До наступного модуля",
                    onClick = if (isLastModule) onBackToMenu else onNext
                )
            }

            null -> {}
        }

        if (done && page !is Page.Final) {
            Spacer(Modifier.height(32.dp))
            KvantorButton("Далі", onClick = onNext)
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
        KvantorButton("Повернутися в меню", onClick = onBackToMenu)
    }
}

/* ───────────── Сторінка-тест ───────────── */
@Composable
fun TestPage(
    test: Page.Test,
    vm: LessonViewModel,
    onDone: () -> Unit
) {
    var selected by remember(test) { mutableStateOf(-1) }
    var checked  by remember(test) { mutableStateOf(false) }

    Text(test.question, color = KvTextColor, textAlign = TextAlign.Center)
    Spacer(Modifier.height(24.dp))

    test.answers.forEachIndexed { idx, ans ->
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = selected == idx,
                onClick  = { selected = idx; checked = false },
                colors   = RadioButtonDefaults.colors(
                    selectedColor   = KvTextColor,
                    unselectedColor = KvTextColor
                )
            )
            Spacer(Modifier.width(8.dp))
            Text(ans, color = KvTextColor)
        }
    }

    /* ---- ПІДКАЗКА ---- */
    if (test.hint != null) {
        Spacer(Modifier.height(8.dp))
        KvantorButton(
            text    = "Підказка (${vm.hints.collectAsState().value})",
            enabled = vm.hints.collectAsState().value > 0,
            onClick = { vm.requestHint(test) }   // ← явне ім’я параметра
        )
    }

    /* ---- Перевірка ---- */
    Spacer(Modifier.height(24.dp))
    KvantorButton(
        text    = "Перевірити",
        enabled = selected != -1,
        onClick = {
            vm.checkAnswer(test, selected)
            checked = true
            onDone()
        }
    )

    /* ---- Результат ---- */
    val correct = remember(checked) { selected == test.correctAnswerIndex }
    if (checked) {
        Spacer(Modifier.height(16.dp))
        Text(
            if (correct) "✅ Правильно (+10₵)" else "❌ Неправильно (-1 ❤️)",
            color     = if (correct) KvAccent else KvAccent.copy(.7f),
            textAlign = TextAlign.Center
        )
    }
}

/* ───────────── Допоміжне: статус-чіп ───────────── */
@Composable
private fun StatusChip(icon: ImageVector, valueText: String, label: String) = Row(
    verticalAlignment     = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(4.dp)
) {
    Icon(icon, null, tint = KvAccent, modifier = Modifier.size(16.dp))
    Text(valueText, color = KvTextColor, fontSize = 14.sp)
    Text(label,      color = KvTextColor, fontSize = 12.sp)
}
