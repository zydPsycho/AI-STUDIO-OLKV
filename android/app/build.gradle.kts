import java.util.Properties

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.blackmark.bloodlink"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.blackmark.bloodlink"
        minSdk = 26
        targetSdk = 35
        versionCode = 3
        versionName = "1.2"
        // This is the public Supabase client configuration; RLS remains the data boundary.
        val defaultSupabaseUrl = "https://whvrmzfesmdmwmkxtcsg.supabase.co"
        val defaultSupabaseKey = "sb_publishable_sOR8HAGbrmnxJWEwskD7uQ_YIy41jfV"
        val supabaseUrl = System.getenv("SUPABASE_URL") ?: (project.findProperty("SUPABASE_URL") as String? ?: localProperties.getProperty("SUPABASE_URL", defaultSupabaseUrl))
        val supabaseKey = System.getenv("SUPABASE_PUBLISHABLE_KEY") ?: (project.findProperty("SUPABASE_PUBLISHABLE_KEY") as String? ?: localProperties.getProperty("SUPABASE_PUBLISHABLE_KEY", defaultSupabaseKey))
        buildConfigField("String", "SUPABASE_URL", "\"${supabaseUrl.replace("\\\"", "\\\\\"")}\"")
        buildConfigField("String", "SUPABASE_PUBLISHABLE_KEY", "\"${supabaseKey.replace("\\\"", "\\\\\"")}\"")
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2025.01.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation("androidx.activity:activity-compose:1.10.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("io.coil-kt:coil-compose:2.7.0")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.datastore:datastore-preferences:1.1.2")
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    implementation("com.google.code.gson:gson:2.11.0")

    debugImplementation("androidx.compose.ui:ui-tooling")
}
