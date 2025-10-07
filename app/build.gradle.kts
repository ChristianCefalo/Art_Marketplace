import java.io.File

val hasGoogleServices = File(rootDir, "app/google-services.json").exists()
if (!hasGoogleServices) {
    logger.lifecycle("google-services.json not found; skipping Google Services plugin for this build.")
}

plugins {
    id("com.android.application")
}

if (hasGoogleServices) {
    logger.lifecycle("google-services.json detected; applying Google Services plugin.")
    apply(plugin = "com.google.gms.google-services")
}

android {
    namespace = "com.example.artmarketplace"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.artmarketplace"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(platform("com.google.firebase:firebase-bom:33.4.0"))
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-auth")

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.recyclerview:recyclerview:1.3.2")

    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("com.google.code.gson:gson:2.10.1")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
