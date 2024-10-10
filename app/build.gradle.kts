plugins {
    id("kotlin-android")
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.jetbrains.compose.compiler)
}

android {
    namespace = "my.nanihadesuka.lazycolumnscrollbar.sample"
    compileSdk = 34

    defaultConfig {
        applicationId = "my.nanihadesuka.lazycolumnscrollbar"
        minSdk = 21
        targetSdk = 34
        versionCode = 9
        versionName = "2.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        named("release") {
            @Suppress("UnstableApiUsage")
            postprocessing {
                isMinifyEnabled = false
                proguardFile("proguard-rules.pro")
                isRemoveUnusedCode = true
                isObfuscate = false
                isOptimizeCode = true
                isRemoveUnusedResources = true
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(compose.material3)
    implementation(compose.ui)
    implementation(compose.preview)
    implementation(libs.androidx.compose.activity)
    implementation(libs.android.material)
    implementation(project(":lib"))
}
