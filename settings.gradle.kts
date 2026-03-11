enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositories {
        mavenLocal()
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

rootProject.name = "Mtg"

include(":androidApp")
include(":composeApp")

includeCoreModules(
    listOf(
        ModuleSuffix.Domain,
        ModuleSuffix.Presentation,
        ModuleSuffix.DI,
    )
)

listOf(
    "auth",
    "welcome",
    "trade",
    "map",
    "settings",
    "chat",
    "notifications",
).forEach { featureName ->
    includeFeatureModules(
        featureName = featureName,
        suffixes = listOf(
            ModuleSuffix.Domain,
            ModuleSuffix.Data,
            ModuleSuffix.Infrastructure,
            ModuleSuffix.DI,
            ModuleSuffix.Presentation,
        ),
    )
}

fun includeCoreModules(suffixes: List<ModuleSuffix>) {
    includeModule(
        prefix = ":composeApp",
        moduleName = "core",
        suffixes = suffixes,
    )
}

fun includeFeatureModules(featureName: String, suffixes: List<ModuleSuffix>) {
    includeModule(
        prefix = ":composeApp:feature",
        moduleName = featureName,
        suffixes = suffixes,
    )
}

fun includeModule(
    prefix: String,
    moduleName: String,
    suffixes: List<ModuleSuffix>,
) {
    suffixes.forEach { suffix ->
        val fullName = "$prefix:$moduleName:${suffix.projectPath}"
        val sanitizedPrefix = prefix.removePrefix(":")
        val path = "${sanitizedPrefix.replace(":", "/")}/$moduleName/${suffix.filePath}"
        includeModule(fullName = fullName, path = path)
    }
}

fun includeModule(fullName: String, path: String) {
    include(fullName)
    val project = project(fullName)
    project.projectDir = File(settingsDir, path)
    require(project.projectDir.isDirectory) { "${project.projectDir} doesn't exist" }
    require(project.buildFile.isFile) { "${project.buildFile} doesn't exist" }
}

sealed class ModuleSuffix(name: String) {
    val projectPath: String = name
    val filePath: String = name.replace(":", "/")

    object DI : ModuleSuffix("di")
    object Presentation : ModuleSuffix("presentation")
    object Domain : ModuleSuffix("domain")
    object Data : ModuleSuffix("data")
    object Infrastructure : ModuleSuffix("infrastructure")
}
