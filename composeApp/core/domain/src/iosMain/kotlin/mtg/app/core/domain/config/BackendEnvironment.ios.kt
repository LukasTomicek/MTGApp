package mtg.app.core.domain.config

actual object BackendEnvironment {
    // Update this before first production rollout.
    private const val PROD_BASE_URL = "https://api.mtglocaltrade.com"
    private const val DEV_BASE_URL = "http://192.168.1.13:8080"

    actual val mode: BackendMode = BackendMode.DEV

    actual val primaryBaseUrl: String = when (mode) {
        BackendMode.DEV -> DEV_BASE_URL
        BackendMode.PRODUCTION -> PROD_BASE_URL
    }

    actual val fallbackBaseUrls: List<String> = emptyList()

    actual fun isLoggableEndpoint(host: String, port: Int): Boolean {
        val normalized = primaryBaseUrl.removePrefix("http://").removePrefix("https://")
        val withoutPath = normalized.substringBefore("/")
        val expectedHost = withoutPath.substringBefore(":")
        val expectedPort = withoutPath.substringAfter(":", missingDelimiterValue = "")
            .toIntOrNull()
            ?: if (primaryBaseUrl.startsWith("https://")) 443 else 80
        return expectedHost.equals(host, ignoreCase = true) && expectedPort == port
    }
}

