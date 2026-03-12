import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
}

kotlin {
    androidLibrary {
        namespace = "mtg.app.feature.welcome.di"
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
            implementation(projects.composeApp.core.presentation)
            implementation(projects.composeApp.feature.auth.domain)
            implementation(projects.composeApp.feature.trade.domain)
            implementation(projects.composeApp.feature.welcome.data)
            implementation(projects.composeApp.feature.welcome.domain)
            implementation(projects.composeApp.feature.welcome.infrastructure)
            implementation(projects.composeApp.feature.welcome.presentation)
            implementation(libs.ktor.client.core)
            implementation(libs.koin.core)
        }
    }
}
