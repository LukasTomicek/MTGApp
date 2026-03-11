package mtg.app.feature.auth.infrastructure

import io.ktor.client.HttpClient

expect class FirebaseAuthService(
    httpClient: HttpClient,
    firebaseWebApiKey: String,
) : AuthService
