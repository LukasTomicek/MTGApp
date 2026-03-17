package mtg.app.feature.settings.presentation.profile

import androidx.compose.runtime.Composable
import mtg.app.feature.settings.domain.obj.ConfirmCreditsPurchaseRequest
import mtg.app.feature.settings.domain.obj.CreditsProduct

@Composable
expect fun rememberCreditsPurchaseRequester(
    onPurchaseConfirmed: suspend (ConfirmCreditsPurchaseRequest) -> Unit,
    onError: (String) -> Unit,
): CreditsPurchaseRequester

interface CreditsPurchaseRequester {
    fun launch(product: CreditsProduct)
    fun recoverPendingPurchases()
}
