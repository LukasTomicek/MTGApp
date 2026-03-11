import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
}

kotlin {
    androidLibrary {
        namespace = "mtg.app.feature.settings.di"
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
            implementation(projects.composeApp.core.presentation)
            implementation(projects.composeApp.feature.auth.domain)
            implementation(projects.composeApp.feature.chat.domain)
            implementation(projects.composeApp.feature.settings.data)
            implementation(projects.composeApp.feature.settings.domain)
            implementation(projects.composeApp.feature.settings.infrastructure)
            implementation(projects.composeApp.feature.settings.presentation)
            implementation(projects.composeApp.feature.welcome.domain)
            implementation(libs.koin.core)
        }
    }
}
