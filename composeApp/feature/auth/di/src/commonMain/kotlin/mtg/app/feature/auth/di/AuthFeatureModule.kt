package mtg.app.feature.auth.di

import mtg.app.core.domain.config.BackendEnvironment
import mtg.app.feature.auth.data.AuthDataSource
import mtg.app.feature.auth.domain.AuthRepository
import mtg.app.feature.auth.domain.ChangePasswordUseCase
import mtg.app.feature.auth.domain.DeleteAccountUseCase
import mtg.app.feature.auth.domain.ObserveAuthStateUseCase
import mtg.app.feature.auth.domain.ObserveAuthInitializationUseCase
import mtg.app.feature.auth.domain.SendPasswordResetUseCase
import mtg.app.feature.auth.domain.SignInUseCase
import mtg.app.feature.auth.domain.SignInWithGoogleUseCase
import mtg.app.feature.auth.domain.SignOutUseCase
import mtg.app.feature.auth.domain.SignUpUseCase
import mtg.app.feature.auth.infrastructure.AuthService
import mtg.app.feature.auth.infrastructure.DefaultAuthRepository
import mtg.app.feature.auth.infrastructure.FirebaseAuthDataSource
import mtg.app.feature.auth.infrastructure.FirebaseAuthService
import mtg.app.feature.auth.infrastructure.InMemoryAuthSessionStore
import mtg.app.feature.auth.presentation.forgotpassword.ForgotPasswordViewModel
import mtg.app.feature.auth.presentation.signup.SignUpViewModel
import mtg.app.feature.auth.presentation.signin.SignInViewModel
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.dsl.module

private const val FIREBASE_WEB_API_KEY: String = "AIzaSyBKo0nMPhdHTGwpzMotJQnT4mg_1oXAkJc"

val authFeatureModule = module {
    single {
        HttpClient {
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
                json(
                    Json {
                        ignoreUnknownKeys = true
                        explicitNulls = false
                    }
                )
            }
        }
    }

    single { InMemoryAuthSessionStore() }
    factory<AuthService> { FirebaseAuthService(httpClient = get(), firebaseWebApiKey = FIREBASE_WEB_API_KEY) }
    factory<AuthDataSource> { FirebaseAuthDataSource(service = get()) }
    factory<AuthRepository> { DefaultAuthRepository(dataSource = get(), sessionStore = get()) }
    factory { ObserveAuthStateUseCase(repository = get()) }
    factory { ObserveAuthInitializationUseCase(repository = get()) }
    factory { SignInUseCase(repository = get()) }
    factory { SignInWithGoogleUseCase(repository = get()) }
    factory { SignUpUseCase(repository = get()) }
    factory { SendPasswordResetUseCase(repository = get()) }
    factory { ChangePasswordUseCase(repository = get()) }
    factory { SignOutUseCase(repository = get()) }
    factory { DeleteAccountUseCase(repository = get()) }

    factory { SignInViewModel(signIn = get(), signInWithGoogle = get(), observeAuthState = get()) }
    factory { SignUpViewModel(signUp = get(), observeAuthState = get()) }
    factory { ForgotPasswordViewModel(sendPasswordReset = get()) }
}
