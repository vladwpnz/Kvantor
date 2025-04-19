package com.bambiloff.kvantor

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Окремий Composable для code‑challenge.
 *
 * @param task       – дані сторінки (опис + очікуваний код)
 * @param onResult   – callback у батьківський екран: true ↔ код правильний
 */
@Composable
fun CodingTaskView(
    task: Page.CodingTask,
    onSubmitted: (Boolean) -> Unit      // true / false поки не важливо
) {
    /* 🔸  усі стани — key = task (ідентифікатор сторінки),
           тому при переході вони стираються */
    var userCode   by remember(task) { mutableStateOf("") }
    var submitted  by remember(task) { mutableStateOf(false) }
    var isCorrect  by remember(task) { mutableStateOf<Boolean?>(null) } // null = ще не перевіряли

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
            submitted = true
            isCorrect = userCode.trim() == task.expectedCode.trim()
            onSubmitted(true)                 // повідомляємо екран → з’явиться «Далі»
        },
        enabled = userCode.isNotBlank()       // щось написано
                && (!submitted || isCorrect == false)  // можна спробувати знову
    ) { Text("Надіслати") }

    if (submitted) {
        if (isCorrect == true) {
            Text("✅ Все вірно!")
        } else {
            Text(
                buildString {
                    append("❌ Неправильно.\n")
                    append("Очікувалось: ${task.expectedCode}")
                }
            )
        }
    }

    /*  🔸 AI‑review поки залишаємо заглушкою */
    Spacer(Modifier.height(8.dp))
    Text("🧠 AI Review (заглушка): ${task.codeReviewPlaceholder}")
}

