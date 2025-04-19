package com.bambiloff.kvantor

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KvantorTheme {
                PythonCourseScreen()
            }
        }
    }
}

@Composable
fun PythonCourseScreen() {
    val context = LocalContext.current
    val uid = FirebaseAuth.getInstance().currentUser?.uid

    // За замовчуванням показуємо default_avatar, поки не завантажимо з БД
    var avatarResId by remember { mutableIntStateOf(R.drawable.default_avatar) }

    // Функція для перетворення рядка в ID
    fun getAvatarId(name: String): Int {
        return context.resources.getIdentifier(name, "drawable", context.packageName)
    }

    // Завантажуємо avatarName з Firestore
    LaunchedEffect(uid) {
        if (uid != null) {
            FirebaseFirestore.getInstance().collection("users").document(uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        // Зчитуємо рядок
                        val avatarName = document.getString("avatarName") ?: "default_avatar"
                        val resId = getAvatarId(avatarName)
                        avatarResId = if (resId != 0) resId else R.drawable.default_avatar
                    }
                }
        }
    }

    val topics = listOf(
        "Вступ" to "Ознайомлення з Python. Напишемо першу програму.",
        "Змінні" to "Що таке змінні, типи даних, як оголошувати.",
        "Цикли" to "Цикли for та while, приклади з практики.",
        "Умови" to "if, else, elif — логіка умов.",
        "Функції" to "Як створювати функції, передавати параметри.",
        "Списки та словники" to "Основи списків та словників у Python."
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF390D58))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Image(
                    painter = painterResource(id = avatarResId),
                    contentDescription = "Profile",
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            context.startActivity(Intent(context, ProfileActivity::class.java))
                        }
                )
            }

            Text(
                text = "PYTHON",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = Rubik,
                color = Color(0xFF1DE0FF)
            )

            Spacer(modifier = Modifier.height(8.dp))

            topics.forEach { (title, description) ->
                var expanded by remember { mutableStateOf(false) }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .background(Color(0xFF512C77), shape = RoundedCornerShape(6.dp))
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
                    AnimatedVisibility(visible = expanded) {
                        Text(
                            text = description,
                            fontSize = 14.sp,
                            fontFamily = Rubik,
                            color = Color.LightGray,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    // Перехід на екран уроків
                    context.startActivity(Intent(context, LessonActivity::class.java))
                },
                modifier = Modifier.align(Alignment.CenterHorizontally),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1DE0FF))
            ) {
                Text("Почати курс", color = Color.Black, fontFamily = Rubik)
            }
        }
    }
}
