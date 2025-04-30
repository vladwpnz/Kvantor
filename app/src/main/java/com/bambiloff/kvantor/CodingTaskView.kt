package com.bambiloff.kvantor

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import java.net.SocketTimeoutException    // <- імпорт для таймауту

/**
 * Окремий Composable для code-challenge з підключенням до Ollama.
 *
 * @param task       – дані сторінки (опис + очікуваний код)
 * @param onSubmitted – callback у батьківський екран: true/false
 */
@Composable
fun CodingTaskView(
    task: Page.CodingTask,
    onSubmitted: (Boolean) -> Unit
) {
    val api       = remember { OllamaApi.create() }
    val coroutine = rememberCoroutineScope()

    // менеджери для клавіатури та фокусу
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var userCode   by remember(task) { mutableStateOf("") }
    var submitted  by remember(task) { mutableStateOf(false) }
    var isCorrect  by remember(task) { mutableStateOf<Boolean?>(null) }
    var aiReview   by remember(task) { mutableStateOf<String?>(null) }
    var isLoading  by remember(task) { mutableStateOf(false) }
    var error      by remember(task) { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
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
                // ховаємо клавіатуру і втрачаємо фокус
                focusManager.clearFocus()
                keyboardController?.hide()

                // перевіряємо базову правильність
                submitted = true
                isCorrect = userCode.trim() == task.expectedCode.trim()
                onSubmitted(isCorrect == true)

                // готуємо новий запит
                aiReview = null
                error = null

                coroutine.launch {
                    isLoading = true
                    try {
                        val request = CodeReviewRequest(
                            task = task.description,
                            code = userCode
                        )
                        val response = api.reviewCode(request)
                        aiReview = response.response

                    } catch (e: SocketTimeoutException) {
                        // окрема обробка таймауту
                        error = "⚠️ Таймаут: AI-рев'ю не отримано, спробуйте пізніше."
                    } catch (e: Exception) {
                        // всі інші помилки
                        error = "❗ Помилка AI-рев'ю: ${e.localizedMessage}"
                    } finally {
                        isLoading = false
                    }
                }
            },
            enabled = userCode.isNotBlank() && (!isLoading)
        ) {
            Text("Надіслати")
        }

        Spacer(Modifier.height(16.dp))

        if (submitted) {
            if (isCorrect == true) {
                Text("✅ Все вірно!", color = MaterialTheme.colorScheme.primary)
            } else {
                Text(
                    buildString {
                        append("❌ Неправильно.\n")
                        append("Очікувалось: ${task.expectedCode}")
                    },
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        if (isLoading) {
            CircularProgressIndicator()
        } else {
            aiReview?.let {
                Text(
                    "🧠 AI-рецензія:\n$it",
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }

        error?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }
    }
}