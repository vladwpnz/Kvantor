package com.bambiloff.kvantor

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bambiloff.kvantor.ui.theme.KvantorTheme
import com.bambiloff.kvantor.ui.theme.Rubik
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class AuthActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()

        setContent {
            KvantorTheme {
                AuthScreen()
            }
        }
    }

    /**
     * Після успішного входу перевіряємо профіль у Firestore
     * і переходимо на відповідний екран.
     */
    private fun navigateBasedOnUserProfile() {
        val uid = auth.currentUser?.uid ?: return
        val usersRef = FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)

        usersRef.get()
            .addOnSuccessListener { doc ->
                if (!doc.exists()) {
                    // Якщо профілю не було — нехай все одно налаштує профіль
                    startActivity(Intent(this, ProfileSetupActivity::class.java))
                } else {
                    // У всіх інших випадках — вибір курсу
                    startActivity(Intent(this, CourseSelectionActivity::class.java))
                }
                finish()
            }
            .addOnFailureListener {
                // Якщо щось пішло не так — теж до вибору
                startActivity(Intent(this, CourseSelectionActivity::class.java))
                finish()
            }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun AuthScreen() {
        val activity = this@AuthActivity
        val context = LocalContext.current

        // стани
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var passwordVisible by remember { mutableStateOf(false) }

        // Snackbar
        val snackbarHostState = remember { SnackbarHostState() }
        val scope = rememberCoroutineScope()

        // Налаштування Google Sign-In клієнта
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        val googleClient: GoogleSignInClient = GoogleSignIn.getClient(context, gso)

        // Launcher для Google-входу
        val googleLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult()
        ) { result: ActivityResult ->
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            if (task.isSuccessful) {
                task.result?.let { acct ->
                    val cred = GoogleAuthProvider.getCredential(acct.idToken, null)
                    auth.signInWithCredential(cred)
                        .addOnCompleteListener { t ->
                            if (t.isSuccessful) {
                                activity.navigateBasedOnUserProfile()
                            } else {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Помилка авторизації через Google")
                                }
                            }
                        }
                }
            } else {
                scope.launch {
                    snackbarHostState.showSnackbar("Помилка Google-входу")
                }
            }
        }

        Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            containerColor = Color(0xFF390D58)
        ) { paddingValues ->
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color(0xFF390D58))
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    Modifier
                        .fillMaxWidth(0.9f)
                        .padding(horizontal = 32.dp, vertical = 48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "KVANTOR",
                        color      = Color(0xFF1DE0FF),
                        fontSize   = 48.sp,
                        fontFamily = Rubik,
                        textAlign  = TextAlign.Center
                    )

                    Spacer(Modifier.height(32.dp))

                    // Email-поле
                    OutlinedTextField(
                        value         = email,
                        onValueChange = { email = it },
                        label         = { Text("Email", color = Color.White, fontFamily = Rubik) },
                        singleLine    = true,
                        modifier      = Modifier.fillMaxWidth(),
                        textStyle     = LocalTextStyle.current.copy(color = Color.White, fontFamily = Rubik)
                    )

                    Spacer(Modifier.height(16.dp))

                    // Password-поле
                    OutlinedTextField(
                        value               = password,
                        onValueChange       = { password = it },
                        label               = { Text("Password", color = Color.White, fontFamily = Rubik) },
                        singleLine          = true,
                        modifier            = Modifier.fillMaxWidth(),
                        textStyle           = LocalTextStyle.current.copy(color = Color.White, fontFamily = Rubik),
                        visualTransformation = if (passwordVisible)
                            VisualTransformation.None
                        else
                            PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(
                                onClick = { passwordVisible = !passwordVisible }
                            ) {
                                Icon(
                                    imageVector = if (passwordVisible)
                                        Icons.Filled.Visibility
                                    else
                                        Icons.Filled.VisibilityOff,
                                    contentDescription = if (passwordVisible) "Сховати пароль" else "Показати пароль",
                                    tint = Color.White
                                )
                            }
                        }
                    )

                    Spacer(Modifier.height(24.dp))

                    // Кнопка "Увійти"
                    Button(
                        onClick = {
                            auth.signInWithEmailAndPassword(email.trim(), password)
                                .addOnCompleteListener { t ->
                                    if (t.isSuccessful) {
                                        activity.navigateBasedOnUserProfile()
                                    } else {
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Невірна пошта або пароль")
                                        }
                                    }
                                }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape    = RoundedCornerShape(6.dp),
                        colors   = ButtonDefaults.buttonColors(containerColor = Color(0xFF8C52FF))
                    ) {
                        Text("Увійти", fontFamily = Rubik)
                    }

                    Spacer(Modifier.height(6.dp))

                    // Кнопка "Реєстрація"
                    OutlinedButton(
                        onClick  = {
                            context.startActivity(Intent(context, RegisterActivity::class.java))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape    = RoundedCornerShape(6.dp),
                        colors   = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                    ) {
                        Text("Реєстрація", fontFamily = Rubik)
                    }

                    Spacer(Modifier.height(10.dp))

                    // Кнопка "Продовжити з Google"
                    OutlinedButton(
                        onClick  = {
                            googleLauncher.launch(googleClient.signInIntent)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape    = RoundedCornerShape(6.dp),
                        colors   = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                    ) {
                        Text("Продовжити з Google", fontFamily = Rubik)
                    }
                }
            }
        }
    }
}
