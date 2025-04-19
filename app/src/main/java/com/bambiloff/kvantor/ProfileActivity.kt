package com.bambiloff.kvantor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bambiloff.kvantor.ui.theme.KvantorTheme
import com.bambiloff.kvantor.ui.theme.Rubik
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KvantorTheme {
                ProfileScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen() {
    val context = LocalContext.current
    val uid = FirebaseAuth.getInstance().currentUser?.uid
    val db = FirebaseFirestore.getInstance()

    var nickname by remember { mutableStateOf("Завантаження...") }

    // За замовчуванням беремо default_avatar
    var avatarResId by remember { mutableIntStateOf(R.drawable.default_avatar) }

    fun getAvatarId(avatarName: String): Int {
        return context.resources.getIdentifier(avatarName, "drawable", context.packageName)
    }

    // Завантажуємо дані профілю
    LaunchedEffect(uid) {
        uid?.let {
            db.collection("users").document(it).get()
                .addOnSuccessListener { document ->
                    nickname = document.getString("nickname") ?: "Без імені"

                    // Зчитуємо avatarName
                    val avatarName = document.getString("avatarName") ?: "default_avatar"
                    val resId = getAvatarId(avatarName)
                    avatarResId = if (resId != 0) resId else R.drawable.default_avatar
                }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Профіль",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontFamily = Rubik
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        (context as? ComponentActivity)?.finish()
                    }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Назад", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF390D58)
                )
            )
        },
        containerColor = Color(0xFF390D58)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = avatarResId),
                contentDescription = "Avatar",
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = nickname,
                color = Color.White,
                fontSize = 24.sp,
                fontFamily = Rubik
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Ваш особистий профіль",
                color = Color.LightGray,
                fontSize = 14.sp,
                fontFamily = Rubik
            )
        }
    }
}
