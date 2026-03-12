package mtg.app.feature.chat.di

import mtg.app.feature.chat.data.MessagesDataSource
import mtg.app.feature.chat.data.remote.DefaultRemoteMessagesDataSource
import mtg.app.feature.chat.domain.ChatService
import mtg.app.feature.chat.domain.DefaultChatService
import mtg.app.feature.chat.domain.MessagesRepository
import mtg.app.feature.chat.infrastructure.DefaultMessagesRepository
import mtg.app.feature.chat.presentation.chatdetail.MessageDetailViewModel
import mtg.app.feature.chat.presentation.chatlist.ChatListViewModel
import mtg.app.feature.chat.presentation.publicprofile.PublicProfileViewModel
import org.koin.dsl.module

val chatFeatureModule = module {
    factory<MessagesDataSource> { DefaultRemoteMessagesDataSource(apiCallHandler = get()) }
    factory<MessagesRepository> { DefaultMessagesRepository(dataSource = get()) }
    factory<ChatService> { DefaultChatService(repository = get()) }

    factory {
        ChatListViewModel(
            authService = get(),
            chatService = get(),
        )
    }
    factory {
        MessageDetailViewModel(
            authService = get(),
            chatService = get(),
        )
    }
    factory {
        PublicProfileViewModel(
            authService = get(),
            chatService = get(),
        )
    }
}
