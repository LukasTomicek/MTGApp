package mtg.app.feature.settings.data.remote

import io.ktor.http.HttpMethod
import mtg.app.core.data.remote.ApiCallHandler
import mtg.app.core.domain.obj.AuthContext
import mtg.app.feature.settings.data.SettingsDataSource
import mtg.app.feature.settings.data.remote.dto.ConfirmCreditsPurchaseRequestDto
import mtg.app.feature.settings.data.remote.dto.OwnProfileResponseDto
import mtg.app.feature.settings.data.remote.dto.WalletBalanceResponseDto
import mtg.app.feature.settings.domain.obj.ConfirmCreditsPurchaseRequest
import mtg.app.feature.settings.domain.obj.SettingsProfile

class DefaultRemoteSettingsDataSource(
    private val apiCallHandler: ApiCallHandler,
) : SettingsDataSource {
    override suspend fun loadOwnProfile(context: AuthContext): SettingsProfile {
        val response = apiCallHandler.apiRequest<OwnProfileResponseDto>(
            path = "/v1/users/me/profile",
            idToken = context.idToken,
        )
        return SettingsProfile(
            nickname = response.nickname?.trim().orEmpty(),
            credits = response.credits,
        )
    }

    override suspend fun confirmCreditsPurchase(
        context: AuthContext,
        request: ConfirmCreditsPurchaseRequest,
    ): Int {
        val response = apiCallHandler.apiRequest<WalletBalanceResponseDto>(
            path = "/v1/users/me/wallet/purchases/confirm",
            method = HttpMethod.Post,
            idToken = context.idToken,
            body = ConfirmCreditsPurchaseRequestDto(
                platform = request.platform,
                productId = request.productId,
                storeTransactionId = request.storeTransactionId,
                purchaseToken = request.purchaseToken,
            ),
        )
        return response.credits
    }
}
