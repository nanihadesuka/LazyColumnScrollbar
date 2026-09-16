import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.jetbrains.compose.compiler)
    id("maven-publish")
}

object MySettings {
    val versionName: String = "2.2.0"
    val namespace = "my.nanihadesuka.lazycolumnscrollbar"
}

group = MySettings.namespace
version = MySettings.versionName

kotlin {
    android {
        namespace = MySettings.namespace
        compileSdk = 37
        minSdk = 23

        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }

        withHostTest {
            isIncludeAndroidResources = true
        }

        androidResources {
            enable = true
        }
    }

    jvm()

    js {
        browser()
        nodejs()
        binaries.executable()
        binaries.library()
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        nodejs()
        binaries.executable()
        binaries.library()
    }

    iosArm64()
    iosSimulatorArm64()

    macosArm64()

    sourceSets {
        commonMain.dependencies {
            implementation(libs.jetbrains.compose.foundation)
            implementation(libs.jetbrains.compose.material3)
            implementation(libs.jetbrains.compose.runtime)
            implementation(libs.jetbrains.compose.ui)
            implementation(libs.jetbrains.compose.animation)
            implementation(libs.jetbrains.compose.ui.tooling.preview)
        }
        androidMain.dependencies {
            implementation(libs.jetbrains.compose.ui.tooling.preview)
        }
        getByName("androidHostTest").dependencies {
            implementation(libs.junit)
            implementation(libs.jetbrains.compose.ui.test.junit4)
            implementation(libs.robolectric)
            implementation(libs.androidx.compose.ui.test.manifest)
        }
    }
}
