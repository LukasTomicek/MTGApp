package mtg.app.feature.settings.presentation.profile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import mtg.app.feature.settings.domain.obj.ConfirmCreditsPurchaseRequest
import mtg.app.feature.settings.domain.obj.CreditsProduct
import platform.Foundation.NSError
import platform.Foundation.NSSet
import platform.Foundation.setWithObject
import platform.StoreKit.SKMutablePayment
import platform.StoreKit.SKPayment
import platform.StoreKit.SKPaymentQueue
import platform.StoreKit.SKPaymentTransaction
import platform.StoreKit.SKPaymentTransactionObserverProtocol
import platform.StoreKit.SKPaymentTransactionState
import platform.StoreKit.SKProduct
import platform.StoreKit.SKProductsRequest
import platform.StoreKit.SKProductsRequestDelegateProtocol
import platform.StoreKit.SKProductsResponse
import platform.StoreKit.SKRequest
import platform.darwin.NSObject

@Composable
actual fun rememberCreditsPurchaseRequester(
    onPurchaseConfirmed: suspend (ConfirmCreditsPurchaseRequest) -> Unit,
    onError: (String) -> Unit,
): CreditsPurchaseRequester {
    val coroutineScope = rememberCoroutineScope()
    val requester = remember(coroutineScope, onPurchaseConfirmed, onError) {
        IosCreditsPurchaseRequester(
            coroutineScope = coroutineScope,
            onPurchaseConfirmed = onPurchaseConfirmed,
            onError = onError,
        )
    }

    DisposableEffect(requester) {
        requester.start()
        onDispose { requester.dispose() }
    }

    LaunchedEffect(requester) {
        requester.recoverPendingPurchases()
    }

    return remember(requester) {
        object : CreditsPurchaseRequester {
            override fun launch(product: CreditsProduct) {
                requester.launchPurchase(product)
            }

            override fun recoverPendingPurchases() {
                requester.recoverPendingPurchases()
            }
        }
    }
}

private class IosCreditsPurchaseRequester(
    private val coroutineScope: CoroutineScope,
    private val onPurchaseConfirmed: suspend (ConfirmCreditsPurchaseRequest) -> Unit,
    private val onError: (String) -> Unit,
) : NSObject(), SKProductsRequestDelegateProtocol, SKPaymentTransactionObserverProtocol {
    private var pendingProductId: String? = null
    private var activeRequest: SKProductsRequest? = null
    private var disposed: Boolean = false

    fun start() {
        SKPaymentQueue.defaultQueue().addTransactionObserver(this)
    }

    fun dispose() {
        disposed = true
        activeRequest?.cancel()
        activeRequest = null
        SKPaymentQueue.defaultQueue().removeTransactionObserver(this)
    }

    fun launchPurchase(product: CreditsProduct) {
        if (!SKPaymentQueue.canMakePayments()) {
            onError("In-app purchases are disabled")
            return
        }
        pendingProductId = product.productId
        activeRequest?.cancel()
        activeRequest = SKProductsRequest(productIdentifiers = NSSet.setWithObject(product.productId)).also { request ->
            request.delegate = this
            request.start()
        }
    }

    fun recoverPendingPurchases() {
        SKPaymentQueue.defaultQueue().transactions.forEach { transaction ->
            val paymentTransaction = transaction as? SKPaymentTransaction ?: return@forEach
            handleTransaction(paymentTransaction)
        }
    }

    override fun productsRequest(request: SKProductsRequest, didReceiveResponse: SKProductsResponse) {
        val product = didReceiveResponse.products.firstOrNull() as? SKProduct
        if (product == null) {
            onError("App Store product not found")
            return
        }
        val payment = SKMutablePayment.paymentWithProduct(product)
        SKPaymentQueue.defaultQueue().addPayment(payment)
    }

    override fun request(request: SKRequest, didFailWithError: NSError) {
        onError(didFailWithError.localizedDescription)
    }

    override fun paymentQueue(queue: SKPaymentQueue, updatedTransactions: List<*>) {
        updatedTransactions.forEach { item ->
            val transaction = item as? SKPaymentTransaction ?: return@forEach
            handleTransaction(transaction)
        }
    }

    private fun handleTransaction(transaction: SKPaymentTransaction) {
        if (disposed) return
        when (transaction.transactionState) {
            SKPaymentTransactionState.SKPaymentTransactionStatePurchased -> confirmAndFinish(transaction)
            SKPaymentTransactionState.SKPaymentTransactionStateRestored -> {
                SKPaymentQueue.defaultQueue().finishTransaction(transaction)
            }
            SKPaymentTransactionState.SKPaymentTransactionStateFailed -> {
                val errorMessage = transaction.error?.localizedDescription ?: "App Store purchase failed"
                SKPaymentQueue.defaultQueue().finishTransaction(transaction)
                onError(errorMessage)
            }
            SKPaymentTransactionState.SKPaymentTransactionStateDeferred -> {
                onError("Purchase is pending approval")
            }
            SKPaymentTransactionState.SKPaymentTransactionStatePurchasing -> Unit
            else -> Unit
        }
    }

    private fun confirmAndFinish(transaction: SKPaymentTransaction) {
        val productId = transaction.payment.productIdentifier.trim()
        val transactionId = transaction.transactionIdentifier?.trim().orEmpty()
        if (productId.isBlank() || transactionId.isBlank()) {
            onError("App Store purchase is missing identifiers")
            return
        }

        coroutineScope.launch {
            runCatching {
                onPurchaseConfirmed(
                    ConfirmCreditsPurchaseRequest(
                        platform = "ios",
                        productId = productId,
                        storeTransactionId = transactionId,
                        purchaseToken = null,
                    ),
                )
                SKPaymentQueue.defaultQueue().finishTransaction(transaction)
            }.onFailure { throwable ->
                onError(throwable.message ?: "Failed to confirm App Store purchase")
            }
        }
    }
}
