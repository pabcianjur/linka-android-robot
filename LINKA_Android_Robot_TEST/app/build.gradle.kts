plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.linka.robot"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.linka.robot"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0-test"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
}
