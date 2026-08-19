plugins {
    id("com.android.application")
    kotlin("android")
}
android {
    namespace = "eu.kanade.tachiyomi.extension.en.vidbox"
    compileSdk = 34
    defaultConfig {
        applicationId = "eu.kanade.tachiyomi.extension.en.vidbox"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.4"
    }
}