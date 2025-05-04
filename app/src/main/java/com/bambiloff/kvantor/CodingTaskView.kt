package com.bambiloff.kvantor

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import java.net.SocketTimeoutException
import androidx.compose.ui.graphics.Color   // ← додайте це



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CodingTaskView(
    task: Page.CodingTask,
    onSubmitted: (Boolean) -> Unit
) {
    val api       = remember { OllamaApi.create() }
    val coroutine = rememberCoroutineScope()

    val focusManager       = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var userCode  by remember(task) { mutableStateOf("") }
    var submitted by remember(task) { mutableStateOf(false) }
    var isCorrect by remember(task) { mutableStateOf<Boolean?>(null) }
    var aiReview  by remember(task) { mutableStateOf<String?>(null) }
    var isLoading by remember(task) { mutableStateOf(false) }
    var error     by remember(task) { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // ── Завдання ────────────────────────────────────────────────────────────────
        Text(
            text  = "Завдання: ${task.description}",
            color = KvTextColor
        )

        Spacer(Modifier.height(8.dp))

        // ── Поле вводу ─────────────────────────────────────────────────────────────
        OutlinedTextField(
            value = userCode,
            onValueChange = { userCode = it },
            label = { Text("Ваш код", color = KvTextColor.copy(alpha = .6f)) },
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor         = KvTextColor,
                unfocusedTextColor       = KvTextColor,
                cursorColor              = KvAccent,
                focusedBorderColor       = KvAccent,
                unfocusedBorderColor     = KvTextColor.copy(alpha = .5f),
                focusedLabelColor        = KvAccent,
                unfocusedLabelColor      = KvTextColor.copy(alpha = .6f),
                focusedPlaceholderColor  = KvTextColor.copy(alpha = .4f),
                unfocusedPlaceholderColor= KvTextColor.copy(alpha = .4f),
                focusedContainerColor    = Color.Transparent,
                unfocusedContainerColor  = Color.Transparent
            )
        )

        Spacer(Modifier.height(8.dp))

        // ── Кнопка "Надіслати" ───────────────────────────────────────────────────
        Button(
            onClick = {
                focusManager.clearFocus()
                keyboardController?.hide()

                submitted = true
                isCorrect = userCode.trim() == task.expectedCode.trim()
                onSubmitted(isCorrect == true)

                aiReview = null
                error    = null

                coroutine.launch {
                    isLoading = true
                    try {
                        val response = api.reviewCode(
                            CodeReviewRequest(task.description, userCode)
                        )
                        aiReview = response.response
                    } catch (e: SocketTimeoutException) {
                        error = "⚠️ Таймаут: AI‑рев'ю не отримано, спробуйте пізніше."
                    } catch (e: Exception) {
                        error = "❗ Помилка AI‑рев'ю: ${e.localizedMessage}"
                    } finally {
                        isLoading = false
                    }
                }
            },
            enabled = userCode.isNotBlank() && !isLoading,
            colors  = ButtonDefaults.buttonColors(
                containerColor = KvAccent,
                contentColor   = KvTextColor
            )
        ) {
            Text("Надіслати")
        }

        Spacer(Modifier.height(16.dp))

        // ── Локальна перевірка ────────────────────────────────────────────────────
        if (submitted) {
            if (isCorrect == true) {
                Text("✅ Все вірно!", color = KvAccent)
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

        // ── AI‑рев'ю або прогрес ─────────────────────────────────────────────────
        if (isLoading) {
            CircularProgressIndicator(color = KvAccent)
        } else {
            aiReview?.let {
                Text(
                    text  = "🧠 AI‑рецензія:\n$it",
                    color = KvTextColor
                )
            }
        }

        // ── Помилки ──────────────────────────────────────────────────────────────
        error?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }
    }
}
