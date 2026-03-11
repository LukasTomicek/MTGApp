package mtg.app.feature.chat.di

import mtg.app.feature.chat.data.MessagesDataSource
import mtg.app.feature.chat.domain.DeleteChatThreadUseCase
import mtg.app.feature.chat.domain.ConfirmDealUseCase
import mtg.app.feature.chat.domain.LoadChatMessagesUseCase
import mtg.app.feature.chat.domain.LoadChatMetaUseCase
import mtg.app.feature.chat.domain.LoadMessageThreadsUseCase
import mtg.app.feature.chat.domain.LoadUserNicknameUseCase
import mtg.app.feature.chat.domain.LoadUserReviewsUseCase
import mtg.app.feature.chat.domain.LoadUserRatingSummaryUseCase
import mtg.app.feature.chat.domain.LoadUserSellOffersUseCase
import mtg.app.feature.chat.domain.MessagesRepository
import mtg.app.feature.chat.domain.ProposeDealUseCase
import mtg.app.feature.chat.domain.SendChatMessageUseCase
import mtg.app.feature.chat.domain.HasRatedChatUseCase
import mtg.app.feature.chat.domain.SubmitUserRatingUseCase
import mtg.app.feature.chat.infrastructure.DefaultMessagesDataSource
import mtg.app.feature.chat.infrastructure.DefaultMessagesRepository
import mtg.app.feature.chat.infrastructure.service.DefaultMessagesService
import mtg.app.feature.chat.infrastructure.service.MessagesService
import mtg.app.feature.chat.presentation.chatdetail.MessageDetailViewModel
import mtg.app.feature.chat.presentation.chatlist.ChatListViewModel
import mtg.app.feature.chat.presentation.publicprofile.PublicProfileViewModel
import org.koin.dsl.module

val chatFeatureModule = module {
    factory<MessagesService> { DefaultMessagesService(httpClient = get()) }
    factory<MessagesDataSource> { DefaultMessagesDataSource(service = get()) }
    factory<MessagesRepository> { DefaultMessagesRepository(dataSource = get()) }

    factory { LoadMessageThreadsUseCase(repository = get()) }
    factory { DeleteChatThreadUseCase(repository = get()) }
    factory { LoadChatMetaUseCase(repository = get()) }
    factory { LoadChatMessagesUseCase(repository = get()) }
    factory { SendChatMessageUseCase(repository = get()) }
    factory { ProposeDealUseCase(repository = get()) }
    factory { ConfirmDealUseCase(repository = get()) }
    factory { LoadUserNicknameUseCase(repository = get()) }
    factory { LoadUserRatingSummaryUseCase(repository = get()) }
    factory { LoadUserReviewsUseCase(repository = get()) }
    factory { LoadUserSellOffersUseCase(repository = get()) }
    factory { HasRatedChatUseCase(repository = get()) }
    factory { SubmitUserRatingUseCase(repository = get()) }

    factory {
        ChatListViewModel(
            observeAuthState = get(),
            loadMessageThreads = get(),
            deleteChatThread = get(),
        )
    }
    factory {
        MessageDetailViewModel(
            observeAuthState = get(),
            loadChatMeta = get(),
            loadChatMessages = get(),
            sendChatMessage = get(),
            proposeDealUseCase = get(),
            confirmDealUseCase = get(),
            deleteChatThreadUseCase = get(),
            loadUserRatingSummary = get(),
            hasRatedChat = get(),
            submitUserRating = get(),
        )
    }
    factory {
        PublicProfileViewModel(
            observeAuthState = get(),
            loadUserNickname = get(),
            loadUserRatingSummary = get(),
            loadUserReviews = get(),
            loadUserSellOffers = get(),
        )
    }
}
