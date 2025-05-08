package com.bambiloff.kvantor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.bambiloff.kvantor.ui.theme.KvantorTheme

class AiAssistantActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { KvantorTheme { AiAssistantScreen() } }
    }
}
