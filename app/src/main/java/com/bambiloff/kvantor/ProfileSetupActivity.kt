package com.bambiloff.kvantor

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import com.google.firebase.Timestamp

class ProfileSetupActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KvantorTheme {
                ProfileSetupScreen { nickname, avatarName ->
                    val userId = FirebaseAuth.getInstance().currentUser?.uid
                    if (userId != null) {
                        val userData = hashMapOf(
                            "nickname" to nickname,
                            "avatarName" to avatarName,  // Зберігаємо рядок
                            "email" to FirebaseAuth.getInstance().currentUser?.email,
                            "createdAt" to Timestamp.now()
                        )

                        FirebaseFirestore.getInstance()
                            .collection("users")
                            .document(userId)
                            .set(userData)
                            .addOnSuccessListener {
                                val intent = Intent(this, WelcomeActivity::class.java)
                                intent.putExtra("nickname", nickname)
                                intent.putExtra("avatarName", avatarName)
                                startActivity(intent)
                                finish()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Помилка при збереженні профілю", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileSetupScreen(
    onContinue: (String, String) -> Unit
) {
    val context = LocalContext.current
    var nickname by remember { mutableStateOf("") }

    // Тепер зберігаємо назву ресурсу як рядок, наприклад "avatar1"
    var selectedAvatarName by remember { mutableStateOf("avatar1") }
    var showAvatarOptions by remember { mutableStateOf(false) }

    // Допоміжна функція для перетворення назви (avatar1) -> ID (R.drawable.avatar1)
    fun getAvatarId(avatarName: String): Int {
        return context.resources.getIdentifier(avatarName, "drawable", context.packageName)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF390D58))
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Привіт! Як тебе звати?",
            fontSize = 24.sp,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontFamily = Rubik
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = nickname,
            onValueChange = { nickname = it },
            label = { Text("Нікнейм", color = Color.White, fontFamily = Rubik) },
            textStyle = LocalTextStyle.current.copy(color = Color.White, fontFamily = Rubik),
            modifier = Modifier.fillMaxWidth(0.85f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Виберіть аватар",
            color = Color.White,
            fontSize = 16.sp,
            fontFamily = Rubik,
            modifier = Modifier.clickable { showAvatarOptions = !showAvatarOptions }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Поточний вибраний аватар
        Image(
            painter = painterResource(id = getAvatarId(selectedAvatarName)),
            contentDescription = "Avatar",
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .clickable {
                    showAvatarOptions = !showAvatarOptions
                }
        )

        // Вибір аватарів
        if (showAvatarOptions) {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                val avatarNames = listOf("avatar1", "avatar2", "avatar3")
                avatarNames.forEach { name ->
                    val avatarId = getAvatarId(name)
                    Image(
                        painter = painterResource(id = avatarId),
                        contentDescription = "Choose avatar",
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .clickable {
                                selectedAvatarName = name
                                showAvatarOptions = false
                            }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (nickname.isNotBlank()) {
                    // Повертаємо нікнейм і назву аватарки
                    onContinue(nickname, selectedAvatarName)
                } else {
                    Toast.makeText(context, "Введи нікнейм", Toast.LENGTH_SHORT).show()
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
