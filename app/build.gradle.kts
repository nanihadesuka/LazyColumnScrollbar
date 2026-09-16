plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.compose.compiler)
}

android {
    namespace = "my.nanihadesuka.lazycolumnscrollbar.sample"
    compileSdk = 37

    defaultConfig {
        applicationId = "my.nanihadesuka.lazycolumnscrollbar"
        minSdk = 23
        targetSdk = 37
        versionCode = 9
        versionName = "2.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        named("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation(libs.androidx.compose.activity)
    implementation(libs.android.material)
    implementation(project(":lazycolumnscrollbar"))
}
