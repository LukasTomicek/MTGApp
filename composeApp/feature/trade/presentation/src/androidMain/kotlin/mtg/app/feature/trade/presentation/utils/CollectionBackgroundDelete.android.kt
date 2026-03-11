package mtg.app.feature.trade.presentation.utils

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.work.BackoffPolicy
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import mtg.app.core.domain.config.BackendEnvironment
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.TimeUnit

actual fun scheduleCollectionBackgroundDelete(
    uid: String,
    idToken: String,
): Boolean {
    val context = resolveApplicationContext() ?: return false
    val input = Data.Builder()
        .putString(DeleteCollectionWorker.KEY_UID, uid)
        .putString(DeleteCollectionWorker.KEY_ID_TOKEN, idToken)
        .build()

    val request = OneTimeWorkRequestBuilder<DeleteCollectionWorker>()
        .setInputData(input)
        .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 10, TimeUnit.SECONDS)
        .build()

    WorkManager.getInstance(context).enqueueUniqueWork(
        "delete_collection_$uid",
        ExistingWorkPolicy.REPLACE,
        request,
    )
    return true
}

private fun resolveApplicationContext(): Context? {
    val app = runCatching {
        val activityThread = Class.forName("android.app.ActivityThread")
        val currentApplication = activityThread.getMethod("currentApplication")
        currentApplication.invoke(null) as? Application
    }.getOrNull()
    return app?.applicationContext
}

internal class DeleteCollectionWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val uid = inputData.getString(KEY_UID)?.trim().orEmpty()
        val idToken = inputData.getString(KEY_ID_TOKEN)?.trim().orEmpty()
        if (uid.isBlank() || idToken.isBlank()) return Result.failure()

        val encodedUid = Uri.encode(uid)
        val endpoint = "${BackendEnvironment.primaryBaseUrl}/v1/bridge/users/$encodedUid/collection"

        val connection = (URL(endpoint).openConnection() as? HttpURLConnection) ?: return Result.retry()
        return try {
            val startedAt = System.currentTimeMillis()
            println("TradeBE: request start method=DELETE url=$endpoint")
            connection.requestMethod = "DELETE"
            connection.connectTimeout = 20_000
            connection.readTimeout = 20_000
            connection.doInput = true
            connection.connect()

            val code = connection.responseCode
            val durationMs = System.currentTimeMillis() - startedAt
            if (code in 200..299) {
                println("TradeBE: request result method=DELETE url=$endpoint status=$code success=true durationMs=$durationMs")
                Result.success()
            } else if (code in 500..599 || code == 429) {
                println("TradeBE: request result method=DELETE url=$endpoint status=$code success=false durationMs=$durationMs")
                Result.retry()
            } else {
                println("TradeBE: request result method=DELETE url=$endpoint status=$code success=false durationMs=$durationMs")
                Result.failure()
            }
        } catch (t: Throwable) {
            println("TradeBE: request error method=DELETE url=$endpoint error=${t.message.orEmpty()}")
            Result.retry()
        } finally {
            connection.disconnect()
        }
    }

    companion object {
        const val KEY_UID = "uid"
        const val KEY_ID_TOKEN = "id_token"
    }
}
