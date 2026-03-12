package mtg.app.core.data.remote

fun interface TokenProvider {
    fun getIdToken(): String?
}
