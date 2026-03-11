package mtg.app.feature.auth.presentation.signin

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions

@Composable
actual fun rememberGoogleIdTokenRequester(
    onTokenReceived: (String) -> Unit,
    onError: (String) -> Unit,
): () -> Unit {
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        val account = runCatching { task.getResult(Exception::class.java) }.getOrNull()
        val idToken = account?.idToken
        if (idToken.isNullOrBlank()) {
            onError("Google sign in failed")
        } else {
            onTokenReceived(idToken)
        }
    }

    return remember(context, launcher, onTokenReceived, onError) {
        {
            val webClientId = context.resolveDefaultWebClientId()
            if (webClientId.isNullOrBlank()) {
                onError("Missing default_web_client_id in google-services config")
                return@remember
            }

            val activity = context.findActivity()
            if (activity == null) {
                onError("Unable to open Google sign in")
                return@remember
            }

            val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken(webClientId)
                .build()

            val client = GoogleSignIn.getClient(activity, options)
            launcher.launch(client.signInIntent)
        }
    }
}

private fun Context.resolveDefaultWebClientId(): String? {
    val id = resources.getIdentifier("default_web_client_id", "string", packageName)
    if (id == 0) return null
    return runCatching { getString(id) }.getOrNull()
}

private tailrec fun Context.findActivity(): Activity? {
    return when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
}
