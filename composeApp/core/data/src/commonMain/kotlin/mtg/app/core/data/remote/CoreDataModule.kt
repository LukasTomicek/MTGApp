package mtg.app.core.data.remote

import io.ktor.client.HttpClient
import kotlinx.serialization.json.Json
import org.koin.dsl.module

val coreDataModule = module {
    single {
        Json {
            ignoreUnknownKeys = true
            explicitNulls = false
        }
    }

    single {
        HttpClient {
            configureAppHttpClient(json = get())
        }
    }

    single { ApiCallHandler(httpClient = get(), json = get(), tokenProvider = get()) }
}
