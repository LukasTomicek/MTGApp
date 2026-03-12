package mtg.app.core.data.remote

import mtg.app.core.domain.config.BackendEnvironment
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

fun HttpClientConfig<*>.configureAppHttpClient(json: Json) {
    defaultRequest {
        headers.append(HttpHeaders.UserAgent, "MtgLocalTrade/1.0 (contact: support@amicara.ai)")
        headers.append(HttpHeaders.Accept, ContentType.Application.Json.toString())
    }

    install(Logging) {
        logger = object : Logger {
            override fun log(message: String) {
                println("TradeBE: $message")
            }
        }
        level = LogLevel.INFO
        filter { request ->
            BackendEnvironment.isLoggableEndpoint(
                host = request.url.host,
                port = request.url.port,
            )
        }
    }

    install(ContentNegotiation) {
        json(json)
    }
}
