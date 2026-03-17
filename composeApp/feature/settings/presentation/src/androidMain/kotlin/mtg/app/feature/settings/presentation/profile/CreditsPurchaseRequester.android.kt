package mtg.app.feature.settings.presentation.profile

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesResponseListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import mtg.app.feature.settings.domain.obj.ConfirmCreditsPurchaseRequest
import mtg.app.feature.settings.domain.obj.CreditsProduct
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Composable
actual fun rememberCreditsPurchaseRequester(
    onPurchaseConfirmed: suspend (ConfirmCreditsPurchaseRequest) -> Unit,
    onError: (String) -> Unit,
): CreditsPurchaseRequester {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val activity = remember(context) { context.findActivity() }

    val requester = remember(context, coroutineScope, onPurchaseConfirmed, onError) {
        AndroidCreditsPurchaseRequester(
            context = context.applicationContext,
            coroutineScope = coroutineScope,
            onPurchaseConfirmed = onPurchaseConfirmed,
            onError = onError,
        )
    }

    DisposableEffect(requester) {
        onDispose { requester.dispose() }
    }

    LaunchedEffect(requester) {
        requester.connect()
    }

    return remember(requester, activity, onError) {
        object : CreditsPurchaseRequester {
            override fun launch(product: CreditsProduct) {
                val resolvedActivity = activity
                if (resolvedActivity == null) {
                    onError("Unable to open Google Play purchase flow")
                    return
                }
                requester.launchPurchase(
                    activity = resolvedActivity,
                    product = product,
                )
            }

            override fun recoverPendingPurchases() {
                requester.recoverPendingPurchases()
            }
        }
    }
}

private class AndroidCreditsPurchaseRequester(
    context: Context,
    private val coroutineScope: CoroutineScope,
    private val onPurchaseConfirmed: suspend (ConfirmCreditsPurchaseRequest) -> Unit,
    private val onError: (String) -> Unit,
) {
    private var productDetailsById: Map<String, ProductDetails> = emptyMap()
    private var isDisposed: Boolean = false
    private var connectionContinuation: CancellableContinuation<Unit>? = null

    private val billingClient: BillingClient = BillingClient.newBuilder(context)
        .setListener { billingResult, purchases ->
            handlePurchaseUpdate(
                billingResult = billingResult,
                purchases = purchases,
            )
        }
        .enablePendingPurchases(
            com.android.billingclient.api.PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()
                .build(),
        )
        .build()

    suspend fun connect() {
        if (billingClient.isReady) return
        suspendCancellableCoroutine { continuation ->
            connectionContinuation = continuation
            billingClient.startConnection(
                object : BillingClientStateListener {
                    override fun onBillingSetupFinished(billingResult: BillingResult) {
                        val active = connectionContinuation ?: return
                        connectionContinuation = null
                        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                            active.resume(Unit)
                        } else {
                            active.resumeWithException(
                                IllegalStateException(
                                    billingResult.debugMessage.ifBlank { "Google Play Billing unavailable" },
                                ),
                            )
                        }
                    }

                    override fun onBillingServiceDisconnected() {
                        connectionContinuation = null
                    }
                },
            )
            continuation.invokeOnCancellation {
                connectionContinuation = null
            }
        }
    }

    fun launchPurchase(activity: Activity, product: CreditsProduct) {
        coroutineScope.launch {
            runCatching {
                connect()
                val productDetails = loadProductDetails(product.productId)
                val flowParams = BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(
                        listOf(
                            BillingFlowParams.ProductDetailsParams.newBuilder()
                                .setProductDetails(productDetails)
                                .build(),
                        ),
                    )
                    .build()
                val result = billingClient.launchBillingFlow(activity, flowParams)
                if (result.responseCode != BillingClient.BillingResponseCode.OK) {
                    error(result.debugMessage.ifBlank { "Unable to launch Google Play purchase flow" })
                }
            }.onFailure { throwable ->
                onError(throwable.message ?: "Unable to launch Google Play purchase flow")
            }
        }
    }

    fun recoverPendingPurchases() {
        coroutineScope.launch {
            runCatching {
                connect()
                val purchases = queryPurchases()
                purchases.forEach(::processPurchasedItem)
            }.onFailure { throwable ->
                onError(throwable.message ?: "Failed to recover Google Play purchases")
            }
        }
    }

    fun dispose() {
        isDisposed = true
        connectionContinuation = null
        billingClient.endConnection()
    }

    private suspend fun loadProductDetails(productId: String): ProductDetails {
        productDetailsById[productId]?.let { return it }
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(productId)
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build(),
                ),
            )
            .build()

        return suspendCancellableCoroutine { continuation ->
            billingClient.queryProductDetailsAsync(params) { billingResult, detailsResult ->
                if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
                    continuation.resumeWithException(
                        IllegalStateException(
                            billingResult.debugMessage.ifBlank { "Failed to load Google Play product details" },
                        ),
                    )
                    return@queryProductDetailsAsync
                }

                val details = detailsResult.productDetailsList.firstOrNull()
                if (details == null) {
                    continuation.resumeWithException(IllegalStateException("Google Play product not found"))
                    return@queryProductDetailsAsync
                }

                productDetailsById = productDetailsById + (productId to details)
                continuation.resume(details)
            }
        }
    }

    private suspend fun queryPurchases(): List<Purchase> {
        return suspendCancellableCoroutine { continuation ->
            billingClient.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder()
                    .setProductType(BillingClient.ProductType.INAPP)
                    .build(),
                PurchasesResponseListener { billingResult, purchases ->
                    if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
                        continuation.resumeWithException(
                            IllegalStateException(
                                billingResult.debugMessage.ifBlank { "Failed to query Google Play purchases" },
                            ),
                        )
                    } else {
                        continuation.resume(purchases)
                    }
                },
            )
        }
    }

    private fun handlePurchaseUpdate(
        billingResult: BillingResult,
        purchases: MutableList<Purchase>?,
    ) {
        if (isDisposed) return
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> purchases.orEmpty().forEach(::processPurchasedItem)
            BillingClient.BillingResponseCode.USER_CANCELED -> Unit
            else -> onError(billingResult.debugMessage.ifBlank { "Google Play purchase failed" })
        }
    }

    private fun processPurchasedItem(purchase: Purchase) {
        if (purchase.purchaseState != Purchase.PurchaseState.PURCHASED) {
            if (purchase.purchaseState == Purchase.PurchaseState.PENDING) {
                onError("Purchase is pending")
            }
            return
        }

        val productId = purchase.products.firstOrNull()
        if (productId.isNullOrBlank()) {
            onError("Google Play purchase is missing product id")
            return
        }

        coroutineScope.launch {
            runCatching {
                onPurchaseConfirmed(
                    ConfirmCreditsPurchaseRequest(
                        platform = "android",
                        productId = productId,
                        storeTransactionId = purchase.orderId?.trim().orEmpty().ifBlank { purchase.purchaseToken },
                        purchaseToken = purchase.purchaseToken,
                    ),
                )
                consumePurchase(purchase.purchaseToken)
            }.onFailure { throwable ->
                onError(throwable.message ?: "Failed to confirm Google Play purchase")
            }
        }
    }

    private suspend fun consumePurchase(purchaseToken: String) {
        suspendCancellableCoroutine<Unit> { continuation ->
            billingClient.consumeAsync(
                ConsumeParams.newBuilder()
                    .setPurchaseToken(purchaseToken)
                    .build(),
            ) { billingResult, _ ->
                if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
                    continuation.resumeWithException(
                        IllegalStateException(
                            billingResult.debugMessage.ifBlank { "Failed to consume Google Play purchase" },
                        ),
                    )
                } else {
                    continuation.resume(Unit)
                }
            }
        }
    }
}

private tailrec fun Context.findActivity(): Activity? {
    return when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
}
