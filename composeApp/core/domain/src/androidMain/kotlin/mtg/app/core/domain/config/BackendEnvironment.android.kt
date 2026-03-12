package mtg.app.core.domain.config

import android.content.pm.ApplicationInfo

actual object BackendEnvironment {
    // Update this before first production rollout.
    private const val PROD_BASE_URL = "https://mtgapp-backend.onrender.com"
    private const val DEV_DEVICE_BASE_URL = "http://192.168.1.13:8080"
    private const val DEV_EMULATOR_BASE_URL = "http://10.0.2.2:8080"

    actual val mode: BackendMode
        get() = if (isDebuggableBuild()) BackendMode.DEV else BackendMode.PRODUCTION

    actual val primaryBaseUrl: String
        get() = when (mode) {
            BackendMode.DEV -> DEV_DEVICE_BASE_URL
            BackendMode.PRODUCTION -> PROD_BASE_URL
        }

    actual val fallbackBaseUrls: List<String>
        get() = when (mode) {
            BackendMode.DEV -> listOf(DEV_EMULATOR_BASE_URL)
            BackendMode.PRODUCTION -> emptyList()
        }

    actual fun isLoggableEndpoint(host: String, port: Int): Boolean {
        val candidates = listOf(primaryBaseUrl) + fallbackBaseUrls
        return candidates.any { candidate ->
            val parsed = parseHostPort(candidate) ?: return@any false
            parsed.first.equals(host, ignoreCase = true) && parsed.second == port
        }
    }

    private fun parseHostPort(baseUrl: String): Pair<String, Int>? {
        val normalized = baseUrl.removePrefix("http://").removePrefix("https://")
        val withoutPath = normalized.substringBefore("/")
        val host = withoutPath.substringBefore(":").trim()
        if (host.isBlank()) return null
        val port = withoutPath.substringAfter(":", missingDelimiterValue = "").toIntOrNull()
            ?: if (baseUrl.startsWith("https://")) 443 else 80
        return host to port
    }

    private fun isDebuggableBuild(): Boolean {
        return runCatching {
            val activityThreadClass = Class.forName("android.app.ActivityThread")
            val currentApplication = activityThreadClass
                .getMethod("currentApplication")
                .invoke(null) as? android.app.Application
            val flags = currentApplication?.applicationInfo?.flags ?: 0
            (flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        }.getOrDefault(false)
    }
}
