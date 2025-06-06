plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.bambiloff.kvantor"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.bambiloff.kvantor"
        minSdk = 21
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    // Compose BOM для узгоджених версій
    implementation(platform("androidx.compose:compose-bom:2024.02.01"))

    // Compose UI
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3:1.2.0") // або 1.3.2

    // Іконки
    implementation("androidx.compose.material:material-icons-extended")

    // Firebase Auth + Firestore
    implementation("com.google.firebase:firebase-auth-ktx:22.3.0")
    implementation("com.google.firebase:firebase-firestore-ktx:24.10.3")
    implementation("com.google.firebase:firebase-firestore:24.10.3")
    implementation("com.google.android.gms:play-services-auth:21.0.0")

    // ViewModel + Lifecycle
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.1")

    // Kotlin Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Тестування
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")

    // Debug
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // ✦ Retrofit + Gson (легко серіалізувати json)
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("com.squareup.okhttp3:logging-interceptor:5.0.0-alpha.11")
    implementation ("com.google.firebase:firebase-firestore-ktx:24.7.1")


    implementation ("androidx.compose.material:material-icons-extended:1.4.3")
    /* ──────── ✦ ДОПОВНЕННЯ ДЛЯ UI-ТЕСТІВ Compose ✦ ──────── */

    // той самий BOM, але підхоплюємо його також у тестових конфігураціях,
    // інакше Gradle попросить явну версію для ui-test-junit4
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.02.01"))
    debugImplementation      (platform("androidx.compose:compose-bom:2024.02.01"))

    // базові правила (Rule) AndroidX Test — знадобляться, якщо
    // у тестах будете використовувати createAndroidComposeRule<…>()
    androidTestImplementation("androidx.test:rules:1.5.0")

}
