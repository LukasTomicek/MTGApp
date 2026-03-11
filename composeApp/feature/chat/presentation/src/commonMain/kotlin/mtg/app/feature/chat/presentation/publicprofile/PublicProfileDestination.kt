package mtg.app.feature.chat.presentation.publicprofile

object PublicProfileDestination {
    const val routeBase: String = "profile/public"
    const val routePattern: String = "$routeBase/{uid}"

    fun route(uid: String): String = "$routeBase/$uid"
}
