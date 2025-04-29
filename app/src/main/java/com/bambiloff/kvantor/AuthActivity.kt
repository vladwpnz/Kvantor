package com.bambiloff.kvantor

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bambiloff.kvantor.ui.theme.KvantorTheme
import com.bambiloff.kvantor.ui.theme.Rubik
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.ui.text.style.TextAlign

class AuthActivity : ComponentActivity() {

    /* ---------- Firebase ---------- */
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    /* ---------- Android ---------- */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        /* UI */
        setContent {
            KvantorTheme {
                AuthScreen(
                    onGoogleSignIn      = ::launchGoogleSignIn,
                    onEmailLogin        = ::signInWithEmail,
                    onNavigateToRegister = {
                        startActivity(Intent(this, RegisterActivity::class.java))
                    }
                )
            }
        }
    }

    /* ---------- Google One-Tap / Sign-In ---------- */

    private val googleSignInLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            if (task.isSuccessful) {
                task.result?.let(::firebaseAuthWithGoogle)
            } else {
                Toast.makeText(this, "Помилка Google-входу", Toast.LENGTH_SHORT).show()
            }
        }

    private fun launchGoogleSignIn() = googleSignInLauncher.launch(googleSignInClient.signInIntent)

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                navigateBasedOnUserProfile()
            } else {
                Toast.makeText(this, "Помилка авторизації через Google", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /* ---------- Email / password ---------- */

    private fun signInWithEmail(email: String, pass: String) {
        auth.signInWithEmailAndPassword(email, pass).addOnCompleteListener {
            if (it.isSuccessful) {
                navigateBasedOnUserProfile()
            } else {
                Toast.makeText(this, "Невірна пошта або пароль", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /* ---------- Куди переходимо після входу ---------- */

    private fun navigateBasedOnUserProfile() {
        val uid = auth.currentUser?.uid ?: return
        val users = FirebaseFirestore.getInstance().collection("users").document(uid)

        users.get().addOnSuccessListener { doc ->
            // 1) профілю ще нема → на налаштування
            if (!doc.exists()) {
                startActivity(Intent(this, ProfileSetupActivity::class.java))
                finish()
                return@addOnSuccessListener
            }

            // 2) профіль є → дивимося selectedCourse
            val selectedCourse = doc.getString("selectedCourse")
            val destination = when (selectedCourse) {
                "python"     -> MainActivity::class.java              // наш існуючий Python-екран
                "javascript" -> JavaScriptMainActivity::class.java  // екран JS-курсу
                else         -> CourseSelectionActivity::class.java   // немає вибору → обрати курс
            }

            startActivity(Intent(this, destination))
            finish()
        }.addOnFailureListener {
            // на всяк випадок, якщо не змогли прочитати документ
            startActivity(Intent(this, CourseSelectionActivity::class.java))
            finish()
        }
    }

    /* ---------- UI ---------- */

    @Composable
    private fun AuthScreen(
        onGoogleSignIn: () -> Unit,
        onEmailLogin: (String, String) -> Unit,
        onNavigateToRegister: () -> Unit
    ) {
        var email            by remember { mutableStateOf("") }
        var password         by remember { mutableStateOf("") }
        var passwordVisible  by remember { mutableStateOf(false) }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF390D58))
                .padding(vertical = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(horizontal = 32.dp, vertical = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    "KVANTOR",
                    color       = Color(0xFF1DE0FF),
                    fontSize    = 48.sp,
                    fontFamily  = Rubik,
                    fontWeight  = FontWeight.Bold,
                    textAlign   = TextAlign.Center
                )

                Spacer(Modifier.height(32.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label       = { Text("Email", color = Color.White, fontFamily = Rubik) },
                    singleLine  = true,
                    modifier    = Modifier.fillMaxWidth(),
                    textStyle   = LocalTextStyle.current.copy(color = Color.White, fontFamily = Rubik)
                )

                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password", color = Color.White, fontFamily = Rubik) },
                    singleLine  = true,
                    modifier    = Modifier.fillMaxWidth(),
                    textStyle   = LocalTextStyle.current.copy(color = Color.White, fontFamily = Rubik),
                    visualTransformation =
                        if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                    }
                )

                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = { onEmailLogin(email.trim(), password) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8C52FF))
                ) { Text("Увійти", fontFamily = Rubik) }

                Spacer(Modifier.height(6.dp))

                OutlinedButton(
                    onClick = onNavigateToRegister,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) { Text("Реєстрація", fontFamily = Rubik) }

                Spacer(Modifier.height(10.dp))

                OutlinedButton(
                    onClick = onGoogleSignIn,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) { Text("Продовжити з Google", fontFamily = Rubik) }
            }
        }
    }
}

