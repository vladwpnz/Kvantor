package com.bambiloff.kvantor

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bambiloff.kvantor.ui.theme.KvantorTheme
import com.bambiloff.kvantor.ui.theme.Rubik
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import androidx.compose.ui.platform.testTag


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var isDarkTheme by remember { mutableStateOf(true) }

            KvantorTheme(darkTheme = isDarkTheme) {
                PythonCourseScreen(
                    isDarkTheme   = isDarkTheme,
                    onToggleTheme = { isDarkTheme = it }
                )
            }
        }
    }
}

@Composable
fun PythonScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("python_header")   // ← ЯКІР ДЛЯ ТЕСТУ
    ) {
        Text("Python: перший урок")
    }
}

@Suppress("DiscouragedApi")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PythonCourseScreen(
    isDarkTheme: Boolean,
    onToggleTheme: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val uid     = FirebaseAuth.getInstance().currentUser?.uid
    val db      = FirebaseFirestore.getInstance()
    val cs      = MaterialTheme.colorScheme

    var avatarResId by remember { mutableStateOf(R.drawable.default_avatar) }
    LaunchedEffect(uid) {
        uid?.let { user ->
            db.collection("users").document(user).get()
                .addOnSuccessListener { doc ->
                    val name = doc.getString("avatarName") ?: "default_avatar"
                    val id   = context.resources.getIdentifier(
                        name, "drawable", context.packageName
                    )
                    avatarResId = if (id != 0) id else R.drawable.default_avatar
                }
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(cs.background)
            .testTag("python_header")
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_back),
                    contentDescription = "Назад",
                    tint = cs.onBackground,
                    modifier = Modifier
                        .size(28.dp)
                        .clickable {
                            context.startActivity(
                                Intent(context, CourseSelectionActivity::class.java)
                                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            )
                            (context as Activity).finish()
                        }
                )

                IconToggleButton(
                    checked = isDarkTheme,
                    onCheckedChange = onToggleTheme
                ) {
                    Icon(
                        imageVector = if (isDarkTheme) Icons.Default.DarkMode else Icons.Default.LightMode,
                        contentDescription = null,
                        tint = cs.primary
                    )
                }

                Image(
                    painter = painterResource(id = avatarResId),
                    contentDescription = "Профіль",
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            context.startActivity(
                                Intent(context, ProfileActivity::class.java)
                            )
                        }
                )
            }

            Text(
                text       = "PYTHON",
                fontSize   = 28.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = Rubik,
                color      = cs.primary
            )

            Spacer(Modifier.height(24.dp))

            // Список тем з фоном 0xFF8C52FF
            val topics = listOf(
                "Вступ" to "Ознайомлення з Python. Напишемо першу програму.",
                "Змінні" to "Що таке змінні, типи даних, як оголошувати.",
                "Цикли" to "Цикли for та while, приклади з практики.",
                "Умови" to "if, else, elif — логіка умов.",
                "Функції" to "Як створювати функції, передавати параметри.",
                "Списки та словники" to "Основи списків та словників у Python."
            )

            topics.forEach { (title, description) ->
                var expanded by remember { mutableStateOf(false) }
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(cs.primary)               // тепер #8C52FF
                        .clickable { expanded = !expanded }
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Text(
                        text       = title,
                        fontSize   = 16.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = Rubik,
                        color      = cs.onPrimary             // білий текст
                    )
                    AnimatedVisibility(expanded) {
                        Text(
                            text       = description,
                            fontSize   = 14.sp,
                            fontFamily = Rubik,
                            color      = cs.onPrimary.copy(alpha = .9f),
                            modifier   = Modifier.padding(top = 6.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
            Spacer(Modifier.weight(1f))

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val interaction = remember { MutableInteractionSource() }
                val pressed by interaction.collectIsPressedAsState()
                val scale by animateFloatAsState(if (pressed) 0.95f else 1f)

                Button(
                    onClick = {
                        uid?.let { user ->
                            db.collection("users")
                                .document(user)
                                .set(
                                    mapOf("progress" to mapOf("python" to mapOf("moduleIndex" to 0, "pageIndex" to 0))),
                                    SetOptions.merge()
                                )
                        }
                        context.startActivity(
                            Intent(context, LessonActivity::class.java).putExtra("courseType", "python")
                        )
                    },
                    modifier          = Modifier
                        .weight(1f)
                        .graphicsLayer { scaleX = scale; scaleY = scale },
                    interactionSource = interaction,
                    shape             = RoundedCornerShape(8.dp),
                    colors            = ButtonDefaults.buttonColors(
                        containerColor = cs.primary,
                        contentColor   = cs.onPrimary
                    )
                ) {
                    Text("Почати курс з початку", fontFamily = Rubik)
                }

                val interaction2 = remember { MutableInteractionSource() }
                val pressed2 by interaction2.collectIsPressedAsState()
                val scale2 by animateFloatAsState(if (pressed2) 0.95f else 1f)

                Button(
                    onClick = {
                        context.startActivity(
                            Intent(context, LessonActivity::class.java).putExtra("courseType", "python")
                        )
                    },
                    modifier          = Modifier
                        .weight(1f)
                        .graphicsLayer { scaleX = scale2; scaleY = scale2 },
                    interactionSource = interaction2,
                    shape             = RoundedCornerShape(8.dp),
                    colors            = ButtonDefaults.buttonColors(
                        containerColor = cs.primary,
                        contentColor   = cs.onPrimary
                    )
                ) {
                    Text("Продовжити курс", fontFamily = Rubik)
                }
            }
        }
    }
}
