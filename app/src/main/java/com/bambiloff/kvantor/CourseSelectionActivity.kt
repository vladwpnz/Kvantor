package com.bambiloff.kvantor

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.bambiloff.kvantor.ui.theme.KvantorTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.ui.platform.LocalContext



/** Екран вибору курсу */
class CourseSelectionActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Стартуємо завжди в темній темі
            var isDarkTheme by remember { mutableStateOf(true) }

            KvantorTheme(darkTheme = isDarkTheme) {
                CourseSelectionScreen(
                    onSelect      = ::openCourse,
                    isDarkTheme   = isDarkTheme,
                    onToggleTheme = { isDarkTheme = it }
                )
            }
        }
    }

    private fun openCourse(courseId: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .update("selectedCourse", courseId)
            .addOnSuccessListener {
                val target = when (courseId) {
                    "python"     -> MainActivity::class.java
                    "javascript" -> JavaScriptMainActivity::class.java
                    else         -> MainActivity::class.java
                }
                startActivity(Intent(this, target))
                finish()
            }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseSelectionScreen(
    onSelect: (String) -> Unit,
    isDarkTheme: Boolean,
    onToggleTheme: (Boolean) -> Unit
) {
    val cs  = MaterialTheme.colorScheme
    val ctx = LocalContext.current      // ← потрібен для запуску AiAssistantActivity

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(cs.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        /* ───────── Перемикач теми ───────── */
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            IconToggleButton(
                checked          = isDarkTheme,
                onCheckedChange  = onToggleTheme
            ) {
                Icon(
                    imageVector       = if (isDarkTheme) Icons.Default.DarkMode else Icons.Default.LightMode,
                    contentDescription = null,
                    tint              = cs.primary
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        /* Ілюстрація */
        Icon(
            imageVector = Icons.Default.School,
            contentDescription = null,
            tint = cs.primary,
            modifier = Modifier.size(96.dp)
        )

        Spacer(Modifier.height(16.dp))

        /* Заголовок */
        Text(
            text  = "Обери курс",
            style = MaterialTheme.typography.headlineMedium,
            color = cs.onBackground
        )

        Spacer(Modifier.height(32.dp))

        /* ───────── Курс Python ───────── */
        CourseButton(
            title       = "Python",
            description = "Базові концепції та практичні задачі",
            onClick     = { onSelect("python") },
            cs          = cs
        )

        Spacer(Modifier.height(8.dp))

        /* ───────── Курс JavaScript ───────── */
        CourseButton(
            title       = "JavaScript",
            description = "Основи веб-розробки та DOM-маніпуляції",
            onClick     = { onSelect("javascript") },
            cs          = cs
        )

        Spacer(Modifier.height(8.dp))

        /* ───────── НОВА КНОПКА: AI‑помічник ───────── */
        CourseButton(
            title       = "AI‑помічник",
            description = "Чат із ШІ та code‑review",
            onClick     = {
                ctx.startActivity(
                    Intent(ctx, AiAssistantActivity::class.java)
                )
            },
            cs          = cs
        )
    }
}

@Composable
private fun CourseButton(
    title: String,
    description: String,
    onClick: () -> Unit,
    cs: ColorScheme
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(   // ← label додано
        targetValue = if (pressed) 0.95f else 1f,
        label = ""
    )

    Button(
        onClick           = onClick,
        interactionSource = interaction,
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .padding(vertical = 4.dp),
        shape  = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = cs.primary,
            contentColor   = cs.onPrimary
        )
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(
                text  = description,
                style = MaterialTheme.typography.bodySmall,
                color = cs.onPrimary.copy(alpha = .9f)
            )
        }
    }
}
