plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    kotlin("kapt")
}

android {
    namespace = "com.example.cryptotrader"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.cryptotrader"
        minSdk = 28
        targetSdk = 36
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

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.10"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        // Kotlin 1.9.x 默认为 17；如果使用 1.8.x，则可以是 1.8 或 11
        jvmTarget = "17"
    }
    kapt {
        javacOptions {
            option("-J--add-opens=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED")
            option("-J--add-opens=jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED")
            option("-J--add-opens=jdk.compiler/com.sun.tools.javac.comp=ALL-UNNAMED")
            option("-J--add-opens=jdk.compiler/com.sun.tools.javac.file=ALL-UNNAMED")
            option("-J--add-opens=jdk.compiler/com.sun.tools.javac.main=ALL-UNNAMED")
            option("-J--add-opens=jdk.compiler/com.sun.tools.javac.model=ALL-UNNAMED")
            option("-J--add-opens=jdk.compiler/com.sun.tools.javac.processing=ALL-UNNAMED")
            option("-J--add-opens=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED")
        }
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")
    implementation("androidx.activity:activity-compose:1.7.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.1")

    // Jetpack Compose (与 kotlin 1.9.22 兼容)
    implementation("androidx.compose.ui:ui:1.8.3")
    implementation("androidx.compose.material3:material3:1.2.1")
    implementation("androidx.compose.material:material:1.8.3")
    implementation("androidx.compose.material:material-icons-extended:1.7.8")
    debugImplementation("androidx.compose.ui:ui-tooling:1.8.3")
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.8.3")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.8.3")

    // Navigation Compose
    implementation("androidx.navigation:navigation-compose:2.7.1")

    // Hilt
    implementation("com.google.dagger:hilt-android:2.51.1")
    kapt("com.google.dagger:hilt-compiler:2.51.1")
    implementation("androidx.hilt:hilt-navigation-compose:1.0.0")

    // Retrofit & OkHttp
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.10.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.10.0")

    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")

    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // Jetpack Security for encrypted storage
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // Coroutines – 使用 Kotlin 1.9.x 对应版本
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")

    // 图表库
    implementation("com.github.PhilJay:MPAndroidChart:3.1.0")

    // 单元测试
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}