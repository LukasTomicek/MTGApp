package mtg.app.core.domain.config

enum class BackendMode {
    DEV,
    PRODUCTION,
}

expect object BackendEnvironment {
    val mode: BackendMode
    val primaryBaseUrl: String
    val fallbackBaseUrls: List<String>
    fun isLoggableEndpoint(host: String, port: Int): Boolean
}

