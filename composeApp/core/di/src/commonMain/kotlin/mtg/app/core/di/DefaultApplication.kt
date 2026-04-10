package mtg.app.core.di

import mtg.app.core.presentation.navigation.BottomNavigationBar
import mtg.app.core.presentation.navigation.Route
import mtg.app.core.presentation.navigation.TopNavigationBar
import mtg.app.feature.auth.domain.AuthDomainService
import mtg.app.feature.auth.presentation.forgotpassword.ForgotPasswordDestination
import mtg.app.feature.auth.presentation.forgotpassword.ForgotPasswordDirection
import mtg.app.feature.auth.presentation.forgotpassword.ForgotPasswordScreen
import mtg.app.feature.auth.presentation.forgotpassword.ForgotPasswordViewModel
import mtg.app.feature.auth.presentation.signup.SignUpDestination
import mtg.app.feature.auth.presentation.signup.SignUpDirection
import mtg.app.feature.auth.presentation.signup.SignUpScreen
import mtg.app.feature.auth.presentation.signup.SignUpViewModel
import mtg.app.feature.auth.presentation.signin.SignInDestination
import mtg.app.feature.auth.presentation.signin.SignInDirection
import mtg.app.feature.auth.presentation.signin.SignInScreen
import mtg.app.feature.auth.presentation.signin.SignInViewModel
import mtg.app.feature.chat.presentation.chatdetail.MessageDetailScreen
import mtg.app.feature.chat.presentation.chatdetail.MessageDetailDirection
import mtg.app.feature.chat.presentation.chatdetail.MessageDetailViewModel
import mtg.app.feature.chat.presentation.chatlist.ChatListDestination
import mtg.app.feature.chat.presentation.chatlist.ChatListDirection
import mtg.app.feature.chat.presentation.chatlist.ChatListScreen
import mtg.app.feature.chat.presentation.chatlist.ChatListViewModel
import mtg.app.feature.chat.presentation.publicprofile.PublicProfileDestination
import mtg.app.feature.chat.presentation.publicprofile.PublicProfileScreen
import mtg.app.feature.chat.presentation.publicprofile.PublicProfileViewModel
import mtg.app.feature.notifications.presentation.NotificationsDirection
import mtg.app.feature.notifications.presentation.NotificationsScreen
import mtg.app.feature.notifications.presentation.NotificationsBadgeUiEvent
import mtg.app.feature.notifications.presentation.NotificationsBadgeViewModel
import mtg.app.feature.notifications.presentation.NotificationsViewModel
import mtg.app.feature.settings.presentation.SettingsDirection
import mtg.app.feature.settings.presentation.SettingsScreen
import mtg.app.feature.settings.presentation.SettingsViewModel
import mtg.app.feature.settings.presentation.privacypolicy.PrivacyPolicyScreen
import mtg.app.feature.settings.presentation.profile.ProfileScreen
import mtg.app.feature.settings.presentation.profile.ProfileViewModel
import mtg.app.feature.settings.presentation.termsofuse.TermsOfUseScreen
import mtg.app.feature.trade.presentation.buylist.BuyListScreen
import mtg.app.feature.trade.presentation.buylist.BuyListUiEvent
import mtg.app.feature.trade.presentation.buylist.BuyListViewModel
import mtg.app.feature.trade.presentation.collection.CollectionScreen
import mtg.app.feature.trade.presentation.collection.CollectionUiEvent
import mtg.app.feature.trade.presentation.collection.CollectionViewModel
import mtg.app.feature.map.presentation.map.MapScreen
import mtg.app.feature.map.presentation.map.MapViewModel
import mtg.app.feature.trade.presentation.marketplace.MarketPlaceDirection
import mtg.app.feature.trade.presentation.marketplace.MarketPlaceScreen
import mtg.app.feature.trade.presentation.marketplace.MarketPlaceViewModel
import mtg.app.feature.trade.presentation.selllist.SellListScreen
import mtg.app.feature.trade.presentation.selllist.SellListUiEvent
import mtg.app.feature.trade.presentation.selllist.SellListViewModel
import mtg.app.feature.trade.presentation.trade.TradeDirection
import mtg.app.feature.trade.presentation.trade.TradeScreen
import mtg.app.feature.trade.presentation.trade.TradeViewModel
import mtg.app.feature.welcome.domain.WelcomeService
import mtg.app.feature.welcome.presentation.mapguide.MapGuideDirection
import mtg.app.feature.welcome.presentation.mapguide.MapGuideScreen
import mtg.app.feature.welcome.presentation.mapguide.MapGuideViewModel
import mtg.app.feature.welcome.presentation.setupprofile.SetupProfileDirection
import mtg.app.feature.welcome.presentation.setupprofile.SetupProfileScreen
import mtg.app.feature.welcome.presentation.setupprofile.SetupProfileViewModel
import mtg.app.feature.welcome.presentation.tradeguide.TradeGuideDirection
import mtg.app.feature.welcome.presentation.tradeguide.TradeGuideScreen
import mtg.app.feature.welcome.presentation.tradeguide.TradeGuideViewModel
import mtg.app.feature.welcome.presentation.welcome.WelcomeDestination
import mtg.app.feature.welcome.presentation.welcome.WelcomeDirection
import mtg.app.feature.welcome.presentation.welcome.WelcomeScreen
import mtg.app.feature.welcome.presentation.welcome.WelcomeViewModel
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.savedstate.read
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import mtg.app.core.domain.obj.AuthContext
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import org.koin.compose.koinInject

@Composable
fun DefaultApplication() {
    val navController = rememberNavController()
    val uriHandler = LocalUriHandler.current

    NavHost(
        navController = navController,
        startDestination = Route.Launch.value,
    ) {
        composable(route = Route.Launch.value) {
            val authService = koinInject<AuthDomainService>()
            val welcomeService = koinInject<WelcomeService>()

            LaunchedEffect(Unit) {
                authService.isInitialized.filter { it }.first()
                val user = authService.currentUser.first()
                if (user == null) {
                    navController.navigate(Route.AuthSignIn.value) {
                        popUpTo(Route.Launch.value) { inclusive = true }
                        launchSingleTop = true
                    }
                } else {
                    val onboardingCompleted = runCatching {
                        welcomeService.loadOnboardingCompleted(
                            context = AuthContext(uid = user.uid, idToken = user.idToken),
                        )
                    }.getOrDefault(false)
                    if (onboardingCompleted) {
                        navController.navigate(Route.Home.value) {
                            popUpTo(Route.Launch.value) { inclusive = true }
                            launchSingleTop = true
                        }
                    } else {
                        navController.navigate(Route.Welcome.value) {
                            popUpTo(Route.Launch.value) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }
            }

            Box(modifier = Modifier.fillMaxSize())
        }

        composable(route = SignInDestination.route) {
            val viewModel = koinInject<SignInViewModel>()
            val uiState by viewModel.state.collectAsState()

            LaunchedEffect(viewModel) {
                viewModel.direction.collect { direction ->
                    when (direction) {
                        SignInDirection.NavigateToHome -> {
                            navController.navigate(Route.Launch.value) {
                                popUpTo(Route.AuthSignIn.value) { inclusive = true }
                                launchSingleTop = true
                            }
                        }

                        SignInDirection.NavigateToSignUp -> {
                            navController.navigate(Route.AuthSignUp.value)
                        }

                        SignInDirection.NavigateToForgotPassword -> {
                            navController.navigate(Route.AuthForgotPassword.value)
                        }
                    }
                }
            }

            SignInScreen(uiState = uiState, onUiEvent = viewModel::onUiEvent)
        }

        composable(route = SignUpDestination.route) {
            val viewModel = koinInject<SignUpViewModel>()
            val uiState by viewModel.state.collectAsState()

            LaunchedEffect(viewModel) {
                viewModel.direction.collect { direction ->
                    when (direction) {
                        SignUpDirection.NavigateToHome -> {
                            navController.navigate(Route.Launch.value) {
                                popUpTo(Route.AuthSignIn.value) { inclusive = true }
                                launchSingleTop = true
                            }
                        }

                        SignUpDirection.NavigateToSignIn -> {
                            navController.navigate(Route.AuthSignIn.value) {
                                popUpTo(Route.AuthSignUp.value) { inclusive = true }
                            }
                        }
                    }
                }
            }

            SignUpScreen(uiState = uiState, onUiEvent = viewModel::onUiEvent)
        }

        composable(route = ForgotPasswordDestination.route) {
            val viewModel = koinInject<ForgotPasswordViewModel>()
            val uiState by viewModel.state.collectAsState()

            LaunchedEffect(viewModel) {
                viewModel.direction.collect { direction ->
                    when (direction) {
                        ForgotPasswordDirection.NavigateToSignIn -> {
                            navController.navigate(Route.AuthSignIn.value) {
                                popUpTo(Route.AuthForgotPassword.value) { inclusive = true }
                            }
                        }
                    }
                }
            }

            ForgotPasswordScreen(uiState = uiState, onUiEvent = viewModel::onUiEvent)
        }

        composable(route = WelcomeDestination.route) {
            val viewModel = koinInject<WelcomeViewModel>()
            val uiState by viewModel.state.collectAsState()

            LaunchedEffect(viewModel) {
                viewModel.direction.collect { direction ->
                    when (direction) {
                        WelcomeDirection.NavigateToMapGuide -> navController.navigate(WelcomeDestination.mapGuideRoute)
                    }
                }
            }

            WelcomeScreen(
                uiState = uiState,
                onUiEvent = viewModel::onUiEvent,
            )
        }

        composable(route = WelcomeDestination.setupProfileRoute) {
            val viewModel = koinInject<SetupProfileViewModel>()
            val uiState by viewModel.state.collectAsState()

            LaunchedEffect(viewModel) {
                viewModel.direction.collect { direction ->
                    when (direction) {
                        SetupProfileDirection.NavigateBack -> navController.popBackStack()
                        SetupProfileDirection.NavigateToHome -> {
                            navController.navigate(Route.Home.value) {
                                popUpTo(WelcomeDestination.route) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    }
                }
            }

            SetupProfileScreen(uiState = uiState, onUiEvent = viewModel::onUiEvent)
        }

        composable(route = WelcomeDestination.mapGuideRoute) {
            val viewModel = koinInject<MapGuideViewModel>()
            val uiState by viewModel.state.collectAsState()

            LaunchedEffect(viewModel) {
                viewModel.direction.collect { direction ->
                    when (direction) {
                        MapGuideDirection.NavigateBack -> navController.popBackStack()
                        MapGuideDirection.NavigateToTradeGuide -> navController.navigate(WelcomeDestination.tradeGuideRoute)
                    }
                }
            }

            MapGuideScreen(uiState = uiState, onUiEvent = viewModel::onUiEvent)
        }

        composable(route = WelcomeDestination.tradeGuideRoute) {
            val viewModel = koinInject<TradeGuideViewModel>()
            val uiState by viewModel.state.collectAsState()

            LaunchedEffect(viewModel) {
                viewModel.direction.collect { direction ->
                    when (direction) {
                        TradeGuideDirection.NavigateBack -> navController.popBackStack()
                        TradeGuideDirection.NavigateToSetupProfile -> navController.navigate(WelcomeDestination.setupProfileRoute)
                    }
                }
            }

            TradeGuideScreen(uiState = uiState, onUiEvent = viewModel::onUiEvent)
        }

        composable(route = Route.Home.value) {
            HomeScreenWithBottomNav(
                onNavigateToAuth = {
                    navController.navigate(Route.AuthSignIn.value) {
                        popUpTo(Route.Home.value) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onNavigateToOnboarding = {
                    navController.navigate(Route.Welcome.value) {
                        popUpTo(Route.Home.value) { inclusive = false }
                        launchSingleTop = true
                    }
                },
            )
        }
    }
}

@Composable
private fun HomeScreenWithBottomNav(
    onNavigateToAuth: () -> Unit,
    onNavigateToOnboarding: () -> Unit,
) {
    val navController = rememberNavController()
    val uriHandler = LocalUriHandler.current
    val notificationsBadgeViewModel = koinInject<NotificationsBadgeViewModel>()
    val notificationsBadgeState by notificationsBadgeViewModel.state.collectAsState()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val destinationRoute = backStackEntry?.destination?.route
    var selectedBottomRoute by remember { mutableStateOf<Route>(Route.Trade) }
    var handleTopBarBack by remember { mutableStateOf<(() -> Boolean)?>(null) }
    var chatDetailCounterpartUid by remember { mutableStateOf("") }
    val currentRoute = selectedBottomRoute
    val isChatDetailRoute = destinationRoute?.startsWith("messages/chat/") == true
    val showBackButton = destinationRoute != Route.Trade.value &&
        destinationRoute != Route.Map.value &&
        destinationRoute != Route.Messages.value &&
        destinationRoute != Route.Settings.value

    LaunchedEffect(destinationRoute) {
        destinationRoute.toSelectableBottomRoute()?.let { route ->
            selectedBottomRoute = route
        }
        handleTopBarBack = null
        if (!isChatDetailRoute) {
            chatDetailCounterpartUid = ""
        }
        notificationsBadgeViewModel.onUiEvent(NotificationsBadgeUiEvent.RefreshRequested)
    }

    Scaffold(
        topBar = {
            TopNavigationBar(
                title = backStackEntry?.destination.toTopBarTitle(),
                showBackButton = showBackButton,
                showNotificationButton = destinationRoute == Route.Trade.value || destinationRoute == Route.Messages.value,
                hasUnreadNotifications = notificationsBadgeState.data.hasUnread,
                showProfileButton = isChatDetailRoute && chatDetailCounterpartUid.isNotBlank(),
                onBackClick = {
                    val handled = handleTopBarBack?.invoke() == true
                    if (!handled) {
                        navController.popBackStack()
                    }
                },
                onProfileClick = {
                    if (isChatDetailRoute && chatDetailCounterpartUid.isNotBlank()) {
                        navController.navigate(PublicProfileDestination.route(chatDetailCounterpartUid))
                    } else {
                        navController.navigate(Route.SettingsProfile.value)
                    }
                },
                onNotificationClick = {
                    navController.navigate(Route.Notifications.value) {
                        launchSingleTop = true
                    }
                },
            )
        },
        bottomBar = {
            BottomNavigationBar(
                currentRoute = currentRoute,
                onNavigate = { route ->
                    selectedBottomRoute = route
                    navController.navigate(route.value) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
            )
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Route.Trade.value,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            composable(route = Route.Trade.value) {
                val viewModel = koinInject<TradeViewModel>()
                val uiState by viewModel.state.collectAsState()

                LaunchedEffect(viewModel) {
                    viewModel.direction.collect { direction ->
                        when (direction) {
                            TradeDirection.NavigateToCollection -> navController.navigate(Route.Collection.value)
                            TradeDirection.NavigateToMarketPlace -> navController.navigate(Route.MarketPlace.value)
                            TradeDirection.NavigateToBuyList -> navController.navigate(Route.BuyList.value)
                            TradeDirection.NavigateToSellList -> navController.navigate(Route.SellList.value)
                        }
                    }
                }

                TradeScreen(uiState = uiState, onUiEvent = viewModel::onUiEvent)
            }
            composable(route = Route.Collection.value) {
                val viewModel = koinInject<CollectionViewModel>()
                val uiState by viewModel.state.collectAsState()
                SideEffect {
                    handleTopBarBack = {
                        if (uiState.data.isAddMode) {
                            viewModel.onUiEvent(CollectionUiEvent.ExitAddModeClicked)
                            true
                        } else {
                            false
                        }
                    }
                }
                CollectionScreen(uiState = uiState, onUiEvent = viewModel::onUiEvent)
            }
            composable(route = Route.MarketPlace.value) {
                val viewModel = koinInject<MarketPlaceViewModel>()
                val uiState by viewModel.state.collectAsState()

                LaunchedEffect(viewModel) {
                    viewModel.direction.collect { direction ->
                        when (direction) {
                            is MarketPlaceDirection.NavigateToChat -> {
                                navController.navigate(ChatListDestination.chatRoute(direction.chatId))
                            }
                            is MarketPlaceDirection.NavigateToPublicProfile -> {
                                navController.navigate(PublicProfileDestination.route(direction.uid))
                            }

                            MarketPlaceDirection.None -> Unit
                        }
                    }
                }

                MarketPlaceScreen(uiState = uiState, onUiEvent = viewModel::onUiEvent)
            }
            composable(route = Route.Settings.value) {
                val viewModel = koinInject<SettingsViewModel>()
                val uiState by viewModel.state.collectAsState()

                LaunchedEffect(viewModel) {
                    viewModel.direction.collect { direction ->
                        when (direction) {
                            SettingsDirection.NavigateToAuth -> onNavigateToAuth()
                            SettingsDirection.NavigateToProfile -> navController.navigate(Route.SettingsProfile.value)
                            SettingsDirection.NavigateToOnboarding -> onNavigateToOnboarding()
                            SettingsDirection.NavigateToPrivacyPolicy -> navController.navigate(Route.SettingsPrivacyPolicy.value)
                            SettingsDirection.NavigateToTermsOfUse -> navController.navigate(Route.SettingsTermsOfUse.value)
                        }
                    }
                }

                SettingsScreen(uiState = uiState, onUiEvent = viewModel::onUiEvent)
            }
            composable(route = Route.SettingsProfile.value) {
                val viewModel = koinInject<ProfileViewModel>()
                val uiState by viewModel.state.collectAsState()
                ProfileScreen(
                    uiState = uiState,
                    onUiEvent = viewModel::onUiEvent,
                )
            }
            composable(route = Route.SettingsPrivacyPolicy.value) {
                PrivacyPolicyScreen()
            }
            composable(route = Route.SettingsTermsOfUse.value) {
                TermsOfUseScreen()
            }
            composable(route = Route.Map.value) {
                val viewModel = koinInject<MapViewModel>()
                val uiState by viewModel.state.collectAsState()
                MapScreen(uiState = uiState, onUiEvent = viewModel::onUiEvent)
            }
            composable(route = Route.BuyList.value) {
                val viewModel = koinInject<BuyListViewModel>()
                val uiState by viewModel.state.collectAsState()
                SideEffect {
                    handleTopBarBack = {
                        if (uiState.data.isAddMode) {
                            viewModel.onUiEvent(BuyListUiEvent.ExitAddModeClicked)
                            true
                        } else {
                            false
                        }
                    }
                }
                BuyListScreen(uiState = uiState, onUiEvent = viewModel::onUiEvent)
            }
            composable(route = Route.SellList.value) {
                val viewModel = koinInject<SellListViewModel>()
                val uiState by viewModel.state.collectAsState()
                SideEffect {
                    handleTopBarBack = {
                        if (uiState.data.isAddMode) {
                            viewModel.onUiEvent(SellListUiEvent.ExitAddModeClicked)
                            true
                        } else {
                            false
                        }
                    }
                }
                SellListScreen(uiState = uiState, onUiEvent = viewModel::onUiEvent)
            }
            composable(route = Route.Messages.value) {
                val viewModel = koinInject<ChatListViewModel>()
                val uiState by viewModel.state.collectAsState()

                LaunchedEffect(viewModel) {
                    viewModel.direction.collect { direction ->
                        when (direction) {
                            is ChatListDirection.NavigateToChat -> {
                                navController.navigate(ChatListDestination.chatRoute(direction.chatId))
                            }

                            ChatListDirection.None -> Unit
                        }
                    }
                }

                ChatListScreen(uiState = uiState, onUiEvent = viewModel::onUiEvent)
            }
            composable(route = ChatListDestination.chatRoutePattern) { backStackEntry ->
                val viewModel = koinInject<MessageDetailViewModel>()
                val uiState by viewModel.state.collectAsState()
                val chatId = backStackEntry.arguments?.read { getStringOrNull("chatId") }.orEmpty()
                LaunchedEffect(viewModel) {
                    viewModel.direction.collect { direction ->
                        when (direction) {
                            MessageDetailDirection.CloseChat -> {
                                navController.popBackStack()
                            }

                            is MessageDetailDirection.OpenExternalUrl -> {
                                uriHandler.openUri(direction.url)
                            }

                            MessageDetailDirection.None -> Unit
                        }
                    }
                }
                MessageDetailScreen(
                    chatId = chatId,
                    uiState = uiState,
                    onUiEvent = viewModel::onUiEvent,
                    onCounterpartUidResolved = { chatDetailCounterpartUid = it },
                )
            }
            composable(route = PublicProfileDestination.routePattern) { backStackEntry ->
                val viewModel = koinInject<PublicProfileViewModel>()
                val uiState by viewModel.state.collectAsState()
                val uid = backStackEntry.arguments?.read { getStringOrNull("uid") }.orEmpty()
                PublicProfileScreen(
                    uid = uid,
                    uiState = uiState,
                    onUiEvent = viewModel::onUiEvent,
                )
            }
            composable(route = Route.Notifications.value) {
                val viewModel = koinInject<NotificationsViewModel>()
                val uiState by viewModel.state.collectAsState()

                LaunchedEffect(viewModel) {
                    viewModel.direction.collect { direction ->
                        when (direction) {
                            is NotificationsDirection.NavigateToChat -> {
                                navController.navigate(ChatListDestination.chatRoute(direction.chatId))
                            }
                            is NotificationsDirection.NavigateToPublicProfile -> {
                                navController.navigate(PublicProfileDestination.route(direction.uid))
                            }

                            NotificationsDirection.None -> Unit
                        }
                    }
                }

                NotificationsScreen(uiState = uiState, onUiEvent = viewModel::onUiEvent)
            }
        }
    }
}

private fun String?.toSelectableBottomRoute(): Route? {
    return when {
        this == Route.Map.value -> Route.Map
        this == Route.Trade.value ||
            this == Route.Collection.value ||
            this == Route.BuyList.value ||
            this == Route.SellList.value ||
            this == Route.MarketPlace.value -> Route.Trade
        this == Route.Messages.value || this?.startsWith("messages/chat/") == true -> Route.Messages
        this == Route.Settings.value ||
            this == Route.SettingsProfile.value ||
            this == Route.SettingsPrivacyPolicy.value ||
            this == Route.SettingsTermsOfUse.value -> Route.Settings
        else -> null
    }
}

private fun NavDestination?.toBottomRoute(): Route {
    if (this == null) return Route.Map

    return when {
        hierarchy.any { it.route == Route.Map.value } -> Route.Map
        hierarchy.any { it.route == Route.Collection.value } -> Route.Trade
        hierarchy.any { it.route == Route.BuyList.value } -> Route.Trade
        hierarchy.any { it.route == Route.SellList.value } -> Route.Trade
        hierarchy.any { it.route == Route.MarketPlace.value } -> Route.Trade
        hierarchy.any { it.route == Route.Trade.value } -> Route.Trade
        hierarchy.any { it.route == Route.Messages.value } -> Route.Messages
        hierarchy.any { (it.route ?: "").startsWith("messages/chat/") } -> Route.Messages
        hierarchy.any { it.route == Route.Notifications.value } -> Route.Messages
        hierarchy.any { it.route == Route.Settings.value } -> Route.Settings
        hierarchy.any { it.route == Route.SettingsProfile.value } -> Route.Settings
        hierarchy.any { it.route == Route.SettingsPrivacyPolicy.value } -> Route.Settings
        hierarchy.any { it.route == Route.SettingsTermsOfUse.value } -> Route.Settings
        else -> Route.Map
    }
}

private fun NavDestination?.toTopBarTitle(): String {
    val route = this?.route
    return when {
        route == Route.Trade.value -> "Trade"
        route == Route.Messages.value -> "Messages"
        route == Route.Notifications.value -> "Notifications"
        route?.startsWith("messages/chat/") == true -> "Chat"
        route?.startsWith("${PublicProfileDestination.routeBase}/") == true -> "User Profile"
        route == Route.Settings.value -> "Settings"
        route == Route.SettingsProfile.value -> "Profile"
        route == Route.SettingsPrivacyPolicy.value -> "Privacy Policy"
        route == Route.SettingsTermsOfUse.value -> "Terms of Use"
        route == Route.Collection.value -> "Collection"
        route == Route.MarketPlace.value -> "MarketPlace"
        route == Route.Map.value -> "Map"
        route == Route.BuyList.value -> "Buy List"
        route == Route.SellList.value -> "Sell List"
        else -> "MTG"
    }
}
