import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
}

kotlin {
    androidLibrary {
        namespace = "mtg.app.feature.chat.infrastructure"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            implementation(projects.composeApp.core.data)
            implementation(projects.composeApp.core.domain)
            implementation(projects.composeApp.feature.chat.data)
            implementation(projects.composeApp.feature.chat.domain)
            implementation(libs.ktor.client.core)
            implementation(libs.kotlinx.serialization.json)
        }
    }
}
