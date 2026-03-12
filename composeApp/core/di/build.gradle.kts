import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    androidLibrary {
        namespace = "mtg.app.core.di"
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
            api(projects.composeApp.core.presentation)
            implementation(projects.composeApp.feature.auth.di)
            implementation(projects.composeApp.feature.auth.domain)
            implementation(projects.composeApp.feature.auth.presentation)
            implementation(projects.composeApp.feature.welcome.di)
            implementation(projects.composeApp.feature.welcome.domain)
            implementation(projects.composeApp.feature.welcome.presentation)
            implementation(projects.composeApp.feature.trade.di)
            implementation(projects.composeApp.feature.trade.presentation)
            implementation(projects.composeApp.feature.map.di)
            implementation(projects.composeApp.feature.map.presentation)
            implementation(projects.composeApp.feature.settings.di)
            implementation(projects.composeApp.feature.settings.presentation)
            implementation(projects.composeApp.feature.chat.di)
            implementation(projects.composeApp.feature.chat.presentation)
            implementation(projects.composeApp.feature.notifications.di)
            implementation(projects.composeApp.feature.notifications.presentation)
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.androidx.navigation.compose)
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
        }
    }
}
