package com.bambiloff.kvantor

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.OnBackPressedCallback
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bambiloff.kvantor.ui.theme.KvantorTheme
import com.bambiloff.kvantor.ui.theme.Rubik
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class JavaScriptMainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /* ── апаратна кнопка «Назад» → повертаємось до вибору курсу ── */
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    startActivity(
                        Intent(this@JavaScriptMainActivity, CourseSelectionActivity::class.java)
                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    )
                    finish()
                }
            }
        )

        setContent {
            KvantorTheme { JavaScriptCourseScreen() }
        }
    }
}

@Composable
fun JavaScriptCourseScreen() {
    val context = LocalContext.current
    val uid = FirebaseAuth.getInstance().currentUser?.uid
    val db = FirebaseFirestore.getInstance()

    /* ---- аватар ---- */
    var avatarResId by remember { mutableStateOf(R.drawable.default_avatar) }
    LaunchedEffect(uid) {
        uid?.let { user ->
            db.collection("users").document(user)
                .get()
                .addOnSuccessListener { doc ->
                    val name = doc.getString("avatarName") ?: "default_avatar"
                    val id = context.resources.getIdentifier(name, "drawable", context.packageName)
                    avatarResId = if (id != 0) id else R.drawable.default_avatar
                }
        }
    }

    val topics = listOf(
        "Вступ" to "Що таке JavaScript, його роль у веб-розробці.",
        "Змінні та типи" to "var, let, const і базові типи даних.",
        "Функції" to "Оголошення та виклик, стрілочні функції.",
        "Цикли та умови" to "for, while, if/else — керування потоком.",
        "Масиви та об'єкти" to "Методи масивів, властивості об'єктів.",
        "DOM та події" to "Маніпуляція DOM-деревом, обробники подій."
    )

    Box(
        Modifier
            .fillMaxSize()
            .background(Color(0xFF0D2A39))
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            /* ---- Верхній рядок: стрілка назад + аватар ---- */
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                /* ←  кнопка Назад */
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_back),
                    contentDescription = "Назад",
                    modifier = Modifier
                        .size(28.dp)
                        .clickable {
                            context.startActivity(
                                Intent(context, CourseSelectionActivity::class.java)
                                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            )
                            (context as Activity).finish()
                        },
                    tint = Color.White          // tint іде наприкінці
                )


                /* аватар */
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

            /* ---- заголовок ---- */
            Text(
                "JAVASCRIPT",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = Rubik,
                color = Color(0xFFF7DF1E)
            )

            Spacer(Modifier.height(24.dp))

            /* ---- список тем ---- */
            topics.forEach { (title, description) ->
                var expanded by remember { mutableStateOf(false) }
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .background(Color(0xFF174861), RoundedCornerShape(6.dp))
                        .clickable { expanded = !expanded }
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = Rubik,
                        color = Color.White
                    )
                    AnimatedVisibility(expanded) {
                        Text(
                            text = description,
                            fontSize = 14.sp,
                            fontFamily = Rubik,
                            color = Color(0xFFE0E0E0),
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            /* ---- кнопки почати / продовжити ---- */
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = {
                            uid?.let { user ->
                                db.collection("users").document(user)
                                    .update(
                                        "progress.javascript",
                                        mapOf("moduleIndex" to 0, "pageIndex" to 0)
                                    )
                                    .addOnCompleteListener {
                                        context.startActivity(
                                            Intent(context, LessonActivity::class.java)
                                                .putExtra("courseType", "javascript")
                                        )
                                    }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF7DF1E))
                    ) {
                        Text("Почати курс з початку", color = Color.Black, fontFamily = Rubik)
                    }

                    Button(
                        onClick = {
                            context.startActivity(
                                Intent(context, LessonActivity::class.java)
                                    .putExtra("courseType", "javascript")
                            )
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF7DF1E))
                    ) {
                        Text("Продовжити курс", color = Color.Black, fontFamily = Rubik)
                    }
                }

            }
        }
    }
}
