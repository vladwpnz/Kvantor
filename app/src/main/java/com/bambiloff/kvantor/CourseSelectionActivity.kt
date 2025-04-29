package com.bambiloff.kvantor

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.shape.RoundedCornerShape


/** Екран, що з'являється одразу після налаштування профілю */
class CourseSelectionActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { CourseSelectionScreen(::openCourse) }
    }

    private fun openCourse(courseId: String) {
        val uid  = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val user = FirebaseFirestore.getInstance().collection("users").document(uid)

        // зберегли вибір
        user.update("selectedCourse", courseId)
            .addOnSuccessListener {
                val target = when (courseId) {
                    "python"     -> MainActivity::class.java
                    "javascript" -> JavaScriptMainActivity::class.java
                    else         -> MainActivity::class.java
                }
                startActivity(Intent(this, target))
                finish()
            }
    }
}

@Composable
fun CourseSelectionScreen(onSelect: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF390D58))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text  = "Обери курс",
            color = Color.White,
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(Modifier.height(32.dp))

        /* ----------  PYTHON  ---------- */
        Button(
            onClick = { onSelect("python") },          // ← Ось цього й бракувало
            modifier = Modifier.fillMaxWidth(),
            shape    = RoundedCornerShape(8.dp),
            colors   = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF1DE0FF),
                contentColor   = Color.Black          // чорний текст видно на бірюзовому
            )
        ) { Text("Python") }

        Spacer(Modifier.height(16.dp))

        /* ----------  JAVASCRIPT  ---------- */
        Button(
            onClick = { onSelect("javascript") },
            modifier = Modifier.fillMaxWidth(),
            shape    = RoundedCornerShape(8.dp),
            colors   = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF1DE0FF),
                contentColor   = Color.Black
            )
        ) { Text("JavaScript") }
    }
}


