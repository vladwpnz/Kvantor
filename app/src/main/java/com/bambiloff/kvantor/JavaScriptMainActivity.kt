/* ───────────────────────── JavaScriptMainActivity.kt ───────────────────────── */
package com.bambiloff.kvantor

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
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
import androidx.compose.ui.unit.sp // KvBg = #390D58, KvAccent = #8C52FF
import com.bambiloff.kvantor.ui.theme.KvantorTheme
import com.bambiloff.kvantor.ui.theme.Rubik
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.ui.text.style.TextAlign
import androidx. compose. ui. platform. testTag


class JavaScriptMainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /* апаратна «Назад» → вибір курсу */
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
            var dark by remember { mutableStateOf(true) }
            KvantorTheme(darkTheme = dark) { JavaScriptMenu(dark, onToggle = { dark = it }) }
        }
    }
}
@Composable
fun JavaScriptScreen() {
    Text(
        "Курс JavaScript",
        modifier = Modifier.testTag("js_header")
    )
}

/* ------------------------- сам екран ------------------------- */
@Composable
private fun JavaScriptMenu(dark: Boolean, onToggle: (Boolean) -> Unit) {

    /* базові кольори (повністю як у Python‑екрані) */
    val bg      = if (dark) KvBg else Color(0xFFF5F5F5)
    val accent  = KvAccent
    val textClr = if (dark) KvTextColor else Color(0xFF1A1A1A)

    val context = LocalContext.current
    val uid  = FirebaseAuth.getInstance().currentUser?.uid
    val db   = FirebaseFirestore.getInstance()

    /* аватар без reflection */
    var avatar by remember { mutableStateOf(R.drawable.default_avatar) }
    LaunchedEffect(uid) {
        uid?.let { u ->
            db.collection("users").document(u).get()
                .addOnSuccessListener { d ->
                    avatar = when (d.getString("avatarName")) {
                        "avatar1" -> R.drawable.avatar1
                        "avatar2" -> R.drawable.avatar2
                        "avatar3" -> R.drawable.avatar3
                        "avatar4" -> R.drawable.avatar4
                        else      -> R.drawable.default_avatar
                    }
                }
        }
    }

    /* список модулів */
    val topics = listOf(
        "Вступ"            to "Що таке JavaScript, його роль у веб‑розробці.",
        "Змінні та типи"    to "var, let, const і базові типи даних.",
        "Функції"          to "Оголошення та виклик, стрілочні функції.",
        "Цикли та умови"    to "for, while, if/else — керування потоком.",
        "Масиви та об'єкти" to "Методи масивів, властивості об'єктів.",
        "DOM та події"      to "Маніпуляція DOM‑деревом, обробники подій."
    )

    /* ----------------- UI ----------------- */
    Box(Modifier.fillMaxSize().background(bg)) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            /* top‑row */
            Row(
                Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_back),
                    contentDescription = null,
                    tint = textClr,
                    modifier = Modifier.size(28.dp).clickable {
                        context.startActivity(
                            Intent(context, CourseSelectionActivity::class.java)
                                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        )
                        (context as Activity).finish()
                    }
                )

                IconToggleButton(dark, onToggle) {
                    Icon(
                        if (dark) Icons.Default.DarkMode else Icons.Default.LightMode,
                        null,
                        tint = accent
                    )
                }

                Image(
                    painter = painterResource(id = avatar),
                    contentDescription = null,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            context.startActivity(Intent(context, ProfileActivity::class.java))
                        }
                )
            }

            /* заголовок */
            Text(
                "JAVASCRIPT",
                fontSize   = 28.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = Rubik,
                color      = accent
            )

            Spacer(Modifier.height(24.dp))

            /* модулі (фон = чистий KvAccent) */
            topics.forEach { (title, descr) ->
                var expand by remember { mutableStateOf(false) }
                Column(
                    Modifier.fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .background(accent, RoundedCornerShape(8.dp))
                        .clickable { expand = !expand }
                        .padding(16.dp)
                ) {
                    Text(title, color = Color.White, fontSize = 16.sp, fontFamily = Rubik)
                    AnimatedVisibility(expand) {
                        Text(
                            descr,
                            color = Color.White.copy(alpha = .9f),
                            fontSize = 14.sp,
                            fontFamily = Rubik,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            /* нижні кнопки: квадратні, без анімації */
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                BasicPurpleButton(
                    text = "Почати курс з початку",
                    modifier = Modifier.weight(1f)
                ) {
                    uid?.let { user ->
                        db.collection("users").document(user)
                            .update(
                                "progress.javascript",
                                mapOf("moduleIndex" to 0, "pageIndex" to 0)
                            )
                    }
                    context.startActivity(
                        Intent(context, LessonActivity::class.java)
                            .putExtra("courseType", "javascript")
                    )
                }

                BasicPurpleButton(
                    text = "Продовжити курс",
                    modifier = Modifier.weight(1f)
                ) {
                    context.startActivity(
                        Intent(context, LessonActivity::class.java)
                            .putExtra("courseType", "javascript")
                    )
                }
            }
        }
    }
}

/* ---------------- квадратна кнопка на KvAccent ---------------- */
@Composable
private fun BasicPurpleButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
          // прибираємо фіксовану висоту
               modifier = modifier,                // без height → стандартна висота
        shape    = RoundedCornerShape(8.dp),
        colors   = ButtonDefaults.buttonColors(
            containerColor = KvAccent,
            contentColor   = Color.White
        )
    ) {
        Text(
            text,
            fontFamily = Rubik,
                       modifier = Modifier.fillMaxWidth(),
                       textAlign = TextAlign.Left
        )
    }
}


