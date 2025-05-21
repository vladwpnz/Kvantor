package com.bambiloff.kvantor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bambiloff.kvantor.ui.*
import androidx. compose. ui. unit. sp


class ShopActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                LessonViewModel() as T
        }

        setContent {
            val vm: LessonViewModel = viewModel(factory = factory)

            val lives  by vm.lives.collectAsState()
            val hints  by vm.hints.collectAsState()
            val coins  by vm.coins.collectAsState()

            val snack  = remember { SnackbarHostState() }
            LaunchedEffect(Unit) {
                vm.events.collect { e ->
                    if (e is LessonViewModel.UiEvent.NoCoins)
                        snack.showSnackbar("Недостатньо монет")
                }
            }

            Scaffold(
                snackbarHost   = { SnackbarHost(snack) },
                containerColor = KvBg,
                topBar = {
                    TopAppBar(
                        title          = { Text("Магазин", color = KvTextColor) },
                        navigationIcon = {
                            IconButton(onClick = { finish() }) {
                                Icon(Icons.Default.ArrowBack, null, tint = KvAccent)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = KvBg)
                    )
                }
            ) { pad ->
                Column(
                    modifier = Modifier
                        .padding(pad)
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    /* -------- баланси -------- */
                    StatusRow(lives, hints, coins)

                    /* —— пояснення значків —— */
                    ExplanationRow(
                        icon = Icons.Default.Favorite,
                        text = "❤️ — життя. Коли життя = 0, завдання блокуються."
                    )
                    ExplanationRow(
                        icon = Icons.Default.Lightbulb,
                        text = "💡 — підказки для тестових запитань."
                    )
                    ExplanationRow(
                        icon = Icons.Default.MonetizationOn,
                        text = "₵ — монети. Заробляються за правильні відповіді\nі витрачаються у магазині."
                    )

                    /* -------- товари -------- */
                    ShopItem(
                        icon    = Icons.Default.Favorite,
                        label   = "Купити 1 ❤️  (30₵)",
                        enabled = coins >= 30,
                        onClick = vm::buyLife
                    )

                    ShopItem(
                        icon    = Icons.Default.Lightbulb,
                        label   = "Купити 1 💡 (20₵)",
                        enabled = coins >= 20,
                        onClick = vm::buyHint
                    )
                }
            }
        }
    }
}

/* ---------------- допоміжні компоненти ---------------- */

@Composable
private fun StatusRow(lives: Int, hints: Int, coins: Int) = Row(
    horizontalArrangement = Arrangement.SpaceEvenly,
    modifier              = Modifier
        .fillMaxWidth()
        .padding(bottom = 8.dp)
) {
    StatusChip(Icons.Default.Favorite,       lives,  "Lives")
    StatusChip(Icons.Default.Lightbulb,      hints,  "Hints")
    StatusChip(Icons.Default.MonetizationOn, coins,  "Coins")
}

@Composable
private fun StatusChip(icon: ImageVector, value: Int, label: String) = Row(
    verticalAlignment     = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(4.dp)
) {
    Icon(icon, null, tint = KvAccent, modifier = Modifier.size(16.dp))
    Text(value.toString(), color = KvTextColor, fontSize = 14.sp)
}

@Composable
private fun ExplanationRow(icon: ImageVector, text: String) = Row(
    verticalAlignment     = Alignment.Top,
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    modifier              = Modifier.fillMaxWidth()
) {
    Icon(icon, null, tint = KvAccent, modifier = Modifier.size(16.dp))
    Text(text, color = KvTextColor, style = MaterialTheme.typography.bodySmall)
}

@Composable
private fun ShopItem(
    icon: ImageVector,
    label: String,
    enabled: Boolean,
    onClick: () -> Unit
) = Row(
    verticalAlignment     = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    modifier              = Modifier.fillMaxWidth()
) {
    Icon(icon, null, tint = KvAccent)
    KvantorButton(
        text     = label,
        enabled  = enabled,
        onClick  = onClick,
        modifier = Modifier.weight(1f)
    )
}
