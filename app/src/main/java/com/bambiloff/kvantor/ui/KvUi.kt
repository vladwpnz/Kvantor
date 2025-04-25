package com.bambiloff.kvantor.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/* ---------- фірмові кольори ---------- */
val KvBg        = Color(0xFF390D58)      // ЄДИНИЙ фон
val KvAccent    = Color(0xFF8C52FF)      // Кнопка
val KvTextColor = Color.White

/* ---------- кнопка ---------- */
@Composable
fun KvButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit
) = Button(
    onClick  = onClick,
    enabled  = enabled,
    modifier = modifier,
    colors   = ButtonDefaults.buttonColors(
        containerColor = KvAccent,
        contentColor   = Color.White
    ),
    shape = ButtonDefaults.filledTonalShape
) { Text(text) }
/* ---------- контейнер сторінки ---------- */
@Composable
fun PageContainer(
    modifier: Modifier = Modifier,
    contentAlignment: Alignment = Alignment.Center,
    content: @Composable ColumnScope.() -> Unit
) = Box(
    modifier = modifier
        .fillMaxSize()
        .background(KvBg)          // використовуємо новий колір фону
        .padding(24.dp),
    contentAlignment = contentAlignment
) {
    Column(
        modifier            = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        content             = content
    )
}