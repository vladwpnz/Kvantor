package com.bambiloff.kvantor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/* кольори бренду */
val KvBg        = Color(0xFF390D58)
val KvAccent    = Color(0xFF8C52FF)
val KvTextColor = Color.White

/* універсальна кнопка */
@Composable
fun KvantorButton(
    text: String,
    enabled: Boolean = true,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) = Button(
    onClick  = onClick,
    enabled  = enabled,
    modifier = modifier,
    colors   = ButtonDefaults.buttonColors(
        containerColor         = KvAccent,
        contentColor           = Color.White,
        disabledContainerColor = KvAccent.copy(.4f),
        disabledContentColor   = Color.White.copy(.6f)
    ),
    shape = ButtonDefaults.filledTonalShape
) { Text(text) }

/* обгортка сторінки */
@Composable
fun PageContainer(content: @Composable BoxScope.() -> Unit) = Box(
    modifier = Modifier
        .fillMaxSize()
        .background(KvBg)
        .padding(24.dp),
    contentAlignment = Alignment.Center,
    content = content
)
