package mtg.app.feature.settings.domain.obj

data class SettingsProfile(
    val nickname: String,
    val credits: Int,
)

data class CreditsProduct(
    val productId: String,
    val credits: Int,
    val label: String,
) {
    companion object {
        val defaultPackages: List<CreditsProduct> = listOf(
            CreditsProduct(
                productId = "mtglocaltrade.credits.10",
                credits = 10,
                label = "Buy 10 credits",
            ),
            CreditsProduct(
                productId = "mtglocaltrade.credits.50",
                credits = 50,
                label = "Buy 50 credits",
            ),
            CreditsProduct(
                productId = "mtglocaltrade.credits.100",
                credits = 100,
                label = "Buy 100 credits",
            ),
        )
    }
}

data class ConfirmCreditsPurchaseRequest(
    val platform: String,
    val productId: String,
    val storeTransactionId: String,
    val purchaseToken: String? = null,
)
