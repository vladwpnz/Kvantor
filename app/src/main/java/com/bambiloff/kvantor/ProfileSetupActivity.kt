package com.bambiloff.kvantor

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import com.bambiloff.kvantor.ui.theme.KvantorTheme
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bambiloff.kvantor.ui.theme.Rubik
import androidx.compose.material3.LocalTextStyle



class ProfileSetupActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            KvantorTheme {
                ProfileSetupScreen { nickname, avatarName ->
                    val uid = FirebaseAuth.getInstance().currentUser?.uid
                    if (uid == null) {
                        Toast.makeText(this, "Користувача не знайдено", Toast.LENGTH_SHORT).show()
                        return@ProfileSetupScreen
                    }

                    lifecycleScope.launch {
                        try {
                            // Створюємо профіль + ініціалізуємо ачівки
                            UserBootstrapper.createUserSkeleton(uid, nickname, avatarName)

                            // Сповіщення про першу ачівку
                            Toast.makeText(
                                this@ProfileSetupActivity,
                                "🥳 Відкрито ачівку: «Перший крок»",
                                Toast.LENGTH_SHORT
                            ).show()

                            // Переходимо до вибору курсу
                            startActivity(
                                Intent(
                                    this@ProfileSetupActivity,
                                    CourseSelectionActivity::class.java
                                ).apply {
                                    putExtra("nickname", nickname)
                                    putExtra("avatarName", avatarName)
                                }
                            )
                            finish()
                        } catch (e: Exception) {
                            Toast.makeText(
                                this@ProfileSetupActivity,
                                "Помилка збереження профілю: ${e.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileSetupScreen(
    onContinue: (nickname: String, avatar: String) -> Unit
) {
    val context = LocalContext.current

    var nickname by remember { mutableStateOf("") }
    var selectedAvatar by remember { mutableStateOf("avatar1") }
    var showAvatarOptions by remember { mutableStateOf(false) }

    fun drawableId(name: String): Int =
        context.resources.getIdentifier(name, "drawable", context.packageName)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF390D58))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Привіт! Як тебе звати?",
            fontSize = 24.sp,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontFamily = Rubik,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = nickname,
            onValueChange = { nickname = it },
            label = { Text("Нікнейм", color = Color.White, fontFamily = Rubik) },
            textStyle = LocalTextStyle.current.copy(color = Color.White, fontFamily = Rubik),
            modifier = Modifier.fillMaxWidth(0.85f)
        )

        Spacer(Modifier.height(24.dp))

        Text(
            text = "Виберіть аватар",
            color = Color.White,
            fontSize = 16.sp,
            fontFamily = Rubik,
            modifier = Modifier.clickable { showAvatarOptions = !showAvatarOptions }
        )

        Spacer(Modifier.height(12.dp))

        Image(
            painter = painterResource(id = drawableId(selectedAvatar)),
            contentDescription = "Avatar",
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .clickable { showAvatarOptions = !showAvatarOptions }
        )

        if (showAvatarOptions) {
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf("avatar1", "avatar2", "avatar3").forEach { name ->
                    Image(
                        painter = painterResource(id = drawableId(name)),
                        contentDescription = name,
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .clickable {
                                selectedAvatar = name
                                showAvatarOptions = false
                            }
                    )
                }
            }
        }

        Spacer(Modifier.height(32.dp))

        Button(
            onClick = {
                if (nickname.isBlank()) {
                    Toast.makeText(context, "Введи нікнейм", Toast.LENGTH_SHORT).show()
                } else {
                    onContinue(nickname, selectedAvatar)
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1DE0FF)),
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(50.dp)
        ) {
            Text("Продовжити", color = Color.Black, fontFamily = Rubik)
        }
    }
}
