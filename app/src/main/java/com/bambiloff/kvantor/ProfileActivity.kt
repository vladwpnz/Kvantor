package com.bambiloff.kvantor

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bambiloff.kvantor.ui.theme.KvantorTheme
import com.bambiloff.kvantor.ui.theme.Rubik
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Help
import com.bambiloff.kvantor.Achievement





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

@SuppressLint("DiscouragedApi")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ProfileScreen() {
    val context = LocalContext.current
    val uid     = FirebaseAuth.getInstance().currentUser?.uid
    val db      = FirebaseFirestore.getInstance()

    // стани UI
    var nickname    by remember { mutableStateOf("Завантаження...") }
    var avatarResId by remember { mutableIntStateOf(R.drawable.default_avatar) }
    var achievements by remember { mutableStateOf<List<Achievement>>(emptyList()) }

    // Додаємо мапу зі списком відомих аватарів, щоб уникнути getIdentifier
    fun getAvatarId(name: String): Int = when (name) {
        "avatar1" -> R.drawable.avatar1
        "avatar2" -> R.drawable.avatar2
        "avatar3" -> R.drawable.avatar3
        else      -> R.drawable.default_avatar
    }

    // Завантажуємо профіль і список ачівок
    LaunchedEffect(uid) {
        uid?.let { id ->
            try {
                // — Профіль
                val userDoc = db.collection("users").document(id).get().await()
                nickname = userDoc.getString("nickname") ?: "Без імені"
                avatarResId = getAvatarId(userDoc.getString("avatarName") ?: "")

                // — Підколекція achievements
                val snap = db.collection("users")
                    .document(id)
                    .collection("achievements")
                    .get()
                    .await()

                achievements = snap.documents.map { d ->
                    Achievement(
                        id       = d.id,
                        unlocked = (d.getBoolean("unlocked") == true)
                    )
                }
            } catch (_: Exception) {
                // логувати за потреби
            }
        }
    }

    val unlockedCount = achievements.count { it.unlocked }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Профіль",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontFamily = Rubik
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { (context as? ComponentActivity)?.finish() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF390D58))
            )
        },
        containerColor = Color(0xFF390D58)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF390D58))
                .padding(padding)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Аватар
            Image(
                painter = painterResource(id = avatarResId),
                contentDescription = "Avatar",
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
            )

            Spacer(Modifier.height(16.dp))

            // Нікнейм
            Text(
                text = nickname,
                color = Color.White,
                fontSize = 24.sp,
                fontFamily = Rubik
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Ваш особистий профіль",
                color = Color.LightGray,
                fontSize = 14.sp,
                fontFamily = Rubik
            )

            // Лічильник ачівок
            Spacer(Modifier.height(24.dp))
            Text(
                text = "Ачівки: $unlockedCount / ${achievements.size}",
                color = Color.White,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(Modifier.height(16.dp))

            // Сітка із іконками ачівок
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.fillMaxHeight(),
                contentPadding = PaddingValues(8.dp)
            ) {
                items(achievements) { ach ->
                    val icon = when (ach.id) {
                        "WELCOME"    -> Icons.Filled.EmojiEvents
                        "PY_MASTER"  -> Icons.Filled.Code
                        "JS_SAMURAI" -> Icons.Filled.Code
                        else         -> Icons.AutoMirrored.Filled.Help
                    }
                    val title = when (ach.id) {
                        "WELCOME"    -> "Перший крок"
                        "PY_MASTER"  -> "Python-Майстер"
                        "JS_SAMURAI" -> "JS-Самурай"
                        else         -> ach.id
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .padding(8.dp)
                            .alpha(if (ach.unlocked) 1f else 0.3f)
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = title,
                            modifier = Modifier.size(48.dp),
                            tint = Color.White
                        )
                        Text(
                            text = title,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}
