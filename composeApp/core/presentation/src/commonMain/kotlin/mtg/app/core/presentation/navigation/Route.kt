package mtg.app.core.presentation.navigation

sealed interface Route {
    val value: String

    data object AuthSignIn : Route {
        override val value: String = "auth/sign_in"
    }

    data object AuthSignUp : Route {
        override val value: String = "auth/sign_up"
    }

    data object AuthForgotPassword : Route {
        override val value: String = "auth/forgot_password"
    }

    data object Welcome : Route {
        override val value: String = "welcome"
    }

    data object Home : Route {
        override val value: String = "home"
    }

    data object Launch : Route {
        override val value: String = "launch"
    }

    data object Trade : Route {
        override val value: String = "trade"
    }

    data object Settings : Route {
        override val value: String = "settings"
    }

    data object SettingsProfile : Route {
        override val value: String = "settings/profile"
    }

    data object SettingsPrivacyPolicy : Route {
        override val value: String = "settings/privacy_policy"
    }

    data object SettingsTermsOfUse : Route {
        override val value: String = "settings/terms_of_use"
    }

    data object Collection : Route {
        override val value: String = "collection"
    }

    data object Map : Route {
        override val value: String = "map"
    }

    data object BuyList : Route {
        override val value: String = "buy_list"
    }

    data object SellList : Route {
        override val value: String = "sell_list"
    }

    data object MarketPlace : Route {
        override val value: String = "market_place"
    }

    data object Messages : Route {
        override val value: String = "messages"
    }

    data object Notifications : Route {
        override val value: String = "notifications"
    }
}
