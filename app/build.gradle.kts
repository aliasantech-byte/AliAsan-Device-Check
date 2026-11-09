
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.aliasantech.devicecheck"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.aliasantech.devicecheck"
        minSdk = 23
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    signingConfigs {
        create("release") {
            storeFile = file(
                System.getenv("ALIASAN_KEYSTORE_FILE") ?: "aliasan-release.jks"
            )
            storePassword = System.getenv("ALIASAN_KEYSTORE_PASSWORD")
            keyAlias = System.getenv("ALIASAN_KEY_ALIAS")
            keyPassword = System.getenv("ALIASAN_KEY_PASSWORD")
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = false
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
