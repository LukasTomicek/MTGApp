package mtg.app.feature.auth.di

import mtg.app.core.data.remote.TokenProvider
import mtg.app.feature.auth.data.AuthDataSource
import mtg.app.feature.auth.domain.AuthDomainService
import mtg.app.feature.auth.domain.AuthRepository
import mtg.app.feature.auth.domain.DefaultAuthDomainService
import mtg.app.feature.auth.infrastructure.AuthService
import mtg.app.feature.auth.infrastructure.DefaultAuthRepository
import mtg.app.feature.auth.infrastructure.FirebaseAuthDataSource
import mtg.app.feature.auth.infrastructure.FirebaseAuthService
import mtg.app.feature.auth.infrastructure.InMemoryAuthSessionStore
import mtg.app.feature.auth.presentation.forgotpassword.ForgotPasswordViewModel
import mtg.app.feature.auth.presentation.signup.SignUpViewModel
import mtg.app.feature.auth.presentation.signin.SignInViewModel
import org.koin.dsl.module

private const val FIREBASE_WEB_API_KEY: String = "AIzaSyBKo0nMPhdHTGwpzMotJQnT4mg_1oXAkJc"

val authFeatureModule = module {
    single { InMemoryAuthSessionStore() }
    single<TokenProvider> {
        TokenProvider {
            get<InMemoryAuthSessionStore>().currentUser.value?.idToken
        }
    }
    factory<AuthService> { FirebaseAuthService(httpClient = get(), firebaseWebApiKey = FIREBASE_WEB_API_KEY) }
    factory<AuthDataSource> { FirebaseAuthDataSource(service = get()) }
    factory<AuthRepository> { DefaultAuthRepository(dataSource = get(), sessionStore = get()) }
    factory<AuthDomainService> { DefaultAuthDomainService(repository = get()) }

    factory { SignInViewModel(authService = get()) }
    factory { SignUpViewModel(authService = get()) }
    factory { ForgotPasswordViewModel(authService = get()) }
}
