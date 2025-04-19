package com.bambiloff.kvantor

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bambiloff.kvantor.ui.theme.KvantorTheme
import com.bambiloff.kvantor.ui.theme.Rubik // ← імпорт шрифту

class WelcomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val nickname = intent.getStringExtra("nickname") ?: "Гість"
        val avatarResId = intent.getIntExtra("avatarResId", R.drawable.default_avatar)

        setContent {
            KvantorTheme {
                WelcomeScreen(nickname, avatarResId) {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
        }
    }
}

@Composable
fun WelcomeScreen(nickname: String, avatarResId: Int, onStartClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF390D58))
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = avatarResId),
            contentDescription = "Avatar",
            modifier = Modifier.size(128.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Привіт, $nickname!",
            color = Color(0xFF1DE0FF),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = Rubik
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Ласкаво просимо до Kvantor!\nЦе додаток, де ти зможеш вивчити програмування у формі квестів.",
            color = Color.White,
            fontSize = 16.sp,
            lineHeight = 22.sp,
            fontFamily = Rubik
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onStartClick,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1DE0FF))
        ) {
            Text("Розпочати", color = Color.Black, fontFamily = Rubik)
        }
    }
}
