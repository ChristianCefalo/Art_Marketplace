val hasGoogleServices = rootProject.file("app/google-services.json").exists()

plugins {
    id("com.android.application")
}

if (hasGoogleServices) {
    logger.lifecycle("google-services.json detected; applying Google Services plugin.")
    pluginManager.apply("com.google.gms.google-services")
} else {
    logger.lifecycle("google-services.json not found; skipping Google Services plugin for this build.")
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
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.auth)

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.recyclerview)

    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp.logging)
    implementation(libs.gson)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
