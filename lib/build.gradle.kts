import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.jetbrains.compose.compiler)
    id("maven-publish")
}

group = "my.nanihadesuka.lazycolumnscrollbar"
version = "2.2.0"

kotlin {
    android {
        namespace = "my.nanihadesuka.lazycolumnscrollbar"
        compileSdk = 37
        minSdk = 23

        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }

        withHostTest {
            isIncludeAndroidResources = true
        }
    }

    jvm {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    js {
        browser()
        nodejs()
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        nodejs()
    }

    iosArm64()
    iosSimulatorArm64()

    macosArm64()

    sourceSets {
        commonMain.dependencies {
            // Types from these modules are part of the public API
            api(libs.jetbrains.compose.runtime)
            api(libs.jetbrains.compose.foundation)
            api(libs.jetbrains.compose.ui)
            api(libs.jetbrains.compose.animation)
            implementation(libs.jetbrains.compose.ui.tooling.preview)
        }
        getByName("androidHostTest").dependencies {
            implementation(libs.junit)
            implementation(libs.robolectric)
            implementation(libs.jetbrains.compose.material3)
            implementation(libs.jetbrains.compose.ui.test.junit4)
            implementation(libs.androidx.compose.ui.test.manifest)
        }
    }
}

tasks.withType<Test>().configureEach {
    maxParallelForks = (Runtime.getRuntime().availableProcessors() - 1).coerceAtLeast(1)
    // Robolectric needs access to JDK internals on recent JDKs
    jvmArgs(
        "--add-exports=java.base/jdk.internal.access=ALL-UNNAMED",
        "--add-opens=java.base/jdk.internal.access=ALL-UNNAMED",
        "--add-opens=java.base/java.io=ALL-UNNAMED",
        // Robolectric loads its native runtime through System.load
        "--enable-native-access=ALL-UNNAMED",
    )
}

publishing {
    publications.withType<MavenPublication>().configureEach {
        pom {
            name.set("LazyColumnScrollbar")
            description.set("Scrollbars implementation for Compose Multiplatform")
            url.set("https://github.com/nanihadesuka/LazyColumnScrollbar")
            licenses {
                license {
                    name.set("MIT License")
                    url.set("https://opensource.org/licenses/MIT")
                }
            }
            scm {
                url.set("https://github.com/nanihadesuka/LazyColumnScrollbar")
            }
        }
    }
}
