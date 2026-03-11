package mtg.app.feature.chat.presentation.chatlist

object ChatListDestination {
    const val route: String = "messages"
    const val chatRoutePattern: String = "messages/chat/{chatId}"

    fun chatRoute(chatId: String): String = "messages/chat/$chatId"
}
