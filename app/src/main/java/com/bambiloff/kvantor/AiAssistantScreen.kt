package com.bambiloff.kvantor

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bambiloff.kvantor.ui.theme.Rubik
import androidx.compose.material.icons.filled.ArrowBack

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiAssistantScreen(vm: AiAssistantViewModel = viewModel()) {
    val chat by vm.chat.collectAsState()
    var input by remember { mutableStateOf("") }
    val ctx = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI-помічник", fontFamily = Rubik) },
                navigationIcon = {
                    IconButton(onClick = { (ctx as? Activity)?.finish() }) {
                        @Suppress("DEPRECATION")  // suppress deprecated ArrowBack warning
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { pad ->
        Column(
            Modifier
                .padding(pad)
                .fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                reverseLayout = true
            ) {
                items(chat.reversed()) { msg ->
                    MessageBubble(msg)
                }
            }

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Запитай…") }
                )
                IconButton(
                    onClick = {
                        if (input.isNotBlank()) {
                            vm.send(input.trim())
                            input = ""
                        }
                    }
                ) {
                    @Suppress("DEPRECATION")  // suppress deprecated Send warning
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "send"
                    )
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(msg: ChatMessage) {
    val isUser = msg.role == ChatMessage.Role.USER
    val bg = if (isUser)
        MaterialTheme.colorScheme.primaryContainer
    else
        MaterialTheme.colorScheme.secondaryContainer

    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            color = bg,
            shape = MaterialTheme.shapes.medium,
            tonalElevation = 2.dp
        ) {
            Text(
                text = msg.text,
                Modifier.padding(12.dp),
                fontFamily = Rubik
            )
        }
    }
}
