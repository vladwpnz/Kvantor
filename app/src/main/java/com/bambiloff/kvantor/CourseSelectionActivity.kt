package com.bambiloff.kvantor

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.bambiloff.kvantor.ui.theme.KvantorTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com. google. firebase. firestore. BuildConfig
/**  MAIN ACTIVITY — вибір курсу  */
class CourseSelectionActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
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

    /**  Натискання кнопки курсу  */
    private fun openCourse(courseId: String) {

        /* ── DEBUG-збірка: минаємо Firestore, одразу стартуємо цільову Activity ── */
        if (BuildConfig.DEBUG) {
            launchTarget(courseId)
            return
        }

        /* ── Release: зберігаємо вибір у Firestore, після успіху відкриваємо курс ── */
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .update("selectedCourse", courseId)
            .addOnSuccessListener { launchTarget(courseId) }
    }

    /**  Відкриває потрібну Activity та (у Release) закриває поточну  */
    private fun launchTarget(courseId: String) {
        val target = when (courseId) {
            "python"     -> MainActivity::class.java
            "javascript" -> JavaScriptMainActivity::class.java
            else         -> MainActivity::class.java
        }
        startActivity(Intent(this, target))

        if (!BuildConfig.DEBUG) {
            finish()                    // Закриваємо тільки в релізі
        }
    }
}

@SuppressLint("DiscouragedApi")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseSelectionScreen(
    onSelect: (String) -> Unit,
    isDarkTheme: Boolean,
    onToggleTheme: (Boolean) -> Unit
) {
    val cs  = MaterialTheme.colorScheme
    val ctx = LocalContext.current

    /* --------- Аватар з Firestore --------- */
    var avatarResId by remember { mutableStateOf(R.drawable.default_avatar) }
    LaunchedEffect(FirebaseAuth.getInstance().currentUser?.uid) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@LaunchedEffect
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { doc ->
                val name = doc.getString("avatarName") ?: "default_avatar"
                val id   = ctx.resources.getIdentifier(name, "drawable", ctx.packageName)
                avatarResId = if (id != 0) id else R.drawable.default_avatar
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(cs.background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        /* ─────────── Top Row ─────────── */
        Row(
            Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            IconToggleButton(
                modifier = Modifier.testTag("toggle_theme"),
                checked         = isDarkTheme,
                onCheckedChange = onToggleTheme
            ) {
                Icon(
                    imageVector       = if (isDarkTheme) Icons.Default.DarkMode else Icons.Default.LightMode,
                    contentDescription = "Перемкнути тему",
                    tint               = cs.primary
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    modifier = Modifier.testTag("btn_shop"),
                    onClick = {
                        ctx.startActivity(Intent(ctx, ShopActivity::class.java))
                    }
                ) {
                    Icon(
                        Icons.Default.ShoppingCart,
                        contentDescription = "Магазин",
                        tint               = cs.primary
                    )
                }

                Spacer(Modifier.width(8.dp))

                Image(
                    painter            = painterResource(id = avatarResId),
                    contentDescription = "Профіль",
                    modifier           = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .testTag("avatar")
                        .clickable {
                            ctx.startActivity(Intent(ctx, ProfileActivity::class.java))
                        }
                )
            }
        }

        /* ─────────── Заголовок ─────────── */
        Icon(
            imageVector       = Icons.Default.School,
            contentDescription = null,
            tint              = cs.primary,
            modifier          = Modifier.size(96.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text  = "Обери курс",
            style = MaterialTheme.typography.headlineMedium,
            color = cs.onBackground
        )
        Spacer(Modifier.height(32.dp))

        /* ─────────── Кнопки курсів ─────────── */
        CourseButton(
            title       = "Python",
            description = "Базові концепції та практичні задачі",
            onClick     = { onSelect("python") },
            cs          = cs,
            tag         = "btn_python"
        )
        Spacer(Modifier.height(8.dp))
        CourseButton(
            title       = "JavaScript",
            description = "Основи веб-розробки та DOM-маніпуляції",
            onClick     = { onSelect("javascript") },
            cs          = cs,
            tag         = "btn_js"
        )
        Spacer(Modifier.height(8.dp))
        CourseButton(
            title       = "AI-помічник",
            description = "Чат із ШІ та code-review",
            onClick     = {
                ctx.startActivity(Intent(ctx, AiAssistantActivity::class.java))
            },
            cs          = cs,
            tag         = "btn_ai"
        )
    }
}

/* ───────── helper Composable ───────── */
@Suppress("SameParameterValue")
@Composable
private fun CourseButton(
    title: String,
    description: String,
    onClick: () -> Unit,
    cs: ColorScheme,
    tag: String
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale  by animateFloatAsState(if (pressed) 0.95f else 1f)

    Button(
        onClick           = onClick,
        interactionSource = interaction,
        modifier          = Modifier
            .fillMaxWidth()
            .testTag(tag)
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
