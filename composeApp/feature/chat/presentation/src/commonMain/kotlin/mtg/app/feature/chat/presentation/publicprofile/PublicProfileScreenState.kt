package mtg.app.feature.chat.presentation.publicprofile

import mtg.app.feature.chat.domain.UserReview
import mtg.app.feature.chat.domain.UserSellOffer

data class PublicProfileScreenState(
    // Target user
    val targetUid: String = "",
    val nickname: String = "",

    // Rating summary
    val ratingAverage: Double = 0.0,
    val ratingCount: Int = 0,

    // Reviews
    val reviews: List<UserReview> = emptyList(),

    // Seller offers
    val sellOffers: List<UserSellOffer> = emptyList(),
)
