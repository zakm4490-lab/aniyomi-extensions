plugins {
    id("com.android.application")
    kotlin("android")
}
android {
    namespace = "eu.kanade.tachiyomi.extension.en.streamingunity"
    compileSdk = 34
    defaultConfig {
        applicationId = "eu.kanade.tachiyomi.extension.en.streamingunity"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.4"
    }
}