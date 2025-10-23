plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp")
    alias(libs.plugins.androidx.room)
}

android {
    namespace = "com.example.oportunyfam_mobile_ong"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.oportunyfam_mobile_ong"
        minSdk = 24
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
    // ----------------------------
    // 🧩 Android e Compose
    // ----------------------------
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation("androidx.compose.material:material-icons-extended:1.5.4")
    implementation("androidx.navigation:navigation-compose:2.8.0")
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.foundation.layout.android)

    // ----------------------------
    // 🧠 Testes
    // ----------------------------
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // ----------------------------
    // 🌐 Retrofit + OkHttp
    // ----------------------------
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")

    // ✅ OkHttp unificado — evita conflito do toRequestBody()
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")

    // ----------------------------
    // 🧩 Outras bibliotecas úteis
    // ----------------------------
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("io.coil-kt:coil-compose:2.7.0") // imagens com Coil
    implementation("androidx.datastore:datastore-preferences:1.1.1") // armazenamento local

    // ----------------------------
    // 💾 Room (banco de dados local)
    // ----------------------------
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // ----------------------------
    // 🗺️ Mapas e localização
    // ----------------------------
    implementation("com.google.android.gms:play-services-maps:19.0.0")
    implementation("com.google.maps.android:maps-compose:2.11.4")
    implementation("org.osmdroid:osmdroid-android:6.1.18")
    implementation("com.google.android.gms:play-services-location:21.3.0")
}

// ----------------------------
// ⚙️ Força versões corretas (evita conflito de OkHttp)
// ----------------------------
configurations.all {
    resolutionStrategy {
        force("com.squareup.okhttp3:okhttp:4.11.0")
        force("com.squareup.okhttp3:logging-interceptor:4.11.0")
    }
}

// ----------------------------
// 🧱 Room Schema
// ----------------------------
room {
    schemaDirectory("$projectDir/schemas")
}
