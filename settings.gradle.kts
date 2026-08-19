pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
rootProject.name = "aniyomi-extensions"

val srcDir = file("src")
if (srcDir.exists()) {
    srcDir.listFiles(File::isDirectory)?.forEach { lang ->
        lang.listFiles(File::isDirectory)?.forEach { ext ->
            if (ext.isDirectory && ext.name != "build" && ext.name != ".git") {
                include(":src:${lang.name}:${ext.name}")
                val bf = File(ext, "build.gradle.kts")
                if (!bf.exists()) {
                    bf.writeText("""
                        plugins {
                            id("com.android.application")
                            kotlin("android")
                        }
                        android {
                            namespace = "eu.kanade.tachiyomi.extension.${lang.name}.${ext.name}"
                            compileSdk = 34
                            defaultConfig {
                                applicationId = "eu.kanade.tachiyomi.extension.${lang.name}.${ext.name}"
                                minSdk = 24
                                targetSdk = 34
                                versionCode = 1
                                versionName = "1.4"
                            }
                        }
                    """.trimIndent())
                }
            }
        }
    }
}
