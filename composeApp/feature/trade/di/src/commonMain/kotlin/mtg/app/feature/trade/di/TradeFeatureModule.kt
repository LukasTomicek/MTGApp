package mtg.app.feature.trade.di

import mtg.app.core.domain.config.BackendEnvironment
import mtg.app.feature.trade.data.TradeDataSource
import mtg.app.feature.trade.domain.TradeRepository
import mtg.app.feature.trade.domain.LoadTradeListEntriesUseCase
import mtg.app.feature.trade.domain.LoadMapPinsUseCase
import mtg.app.feature.trade.domain.LoadMarketPlaceSellersUseCase
import mtg.app.feature.trade.domain.LoadRecentMarketPlaceCardsUseCase
import mtg.app.feature.trade.domain.ResolveTradeCardsByExactNamesUseCase
import mtg.app.feature.trade.domain.ReplaceMapPinsUseCase
import mtg.app.feature.trade.domain.ReplaceTradeListEntriesUseCase
import mtg.app.feature.trade.domain.SearchTradeCardPrintsUseCase
import mtg.app.feature.trade.domain.SearchMarketPlaceCardsUseCase
import mtg.app.feature.trade.domain.SearchTradeCardsUseCase
import mtg.app.feature.trade.domain.EnsureMarketPlaceChatUseCase
import mtg.app.feature.trade.infrastructure.DefaultTradeRepository
import mtg.app.feature.trade.infrastructure.ScryfallTradeDataSource
import mtg.app.feature.trade.infrastructure.service.ScryfallTradeService
import mtg.app.feature.trade.infrastructure.service.TradeService
import mtg.app.feature.trade.presentation.buylist.BuyListViewModel
import mtg.app.feature.trade.presentation.collection.CollectionViewModel
import mtg.app.feature.trade.presentation.marketplace.MarketPlaceViewModel
import mtg.app.feature.trade.presentation.selllist.SellListViewModel
import mtg.app.feature.trade.presentation.trade.TradeViewModel
import mtg.app.feature.trade.presentation.utils.SellListTransferStore
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.dsl.module

val tradeFeatureModule = module {
    single {
        HttpClient {
            defaultRequest {
                headers.append(HttpHeaders.UserAgent, "MtgLocalTrade/1.0 (contact: support@amicara.ai)")
                headers.append(HttpHeaders.Accept, ContentType.Application.Json.toString())
            }
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        println("TradeBE: $message")
                    }
                }
                level = LogLevel.INFO
                filter { request ->
                    BackendEnvironment.isLoggableEndpoint(
                        host = request.url.host,
                        port = request.url.port,
                    )
                }
            }
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        explicitNulls = false
                    }
                )
            }
        }
    }

    factory<TradeService> { ScryfallTradeService(httpClient = get()) }
    factory<TradeDataSource> { ScryfallTradeDataSource(service = get()) }
    factory<TradeRepository> { DefaultTradeRepository(dataSource = get()) }
    factory { SearchTradeCardsUseCase(repository = get()) }
    factory { ResolveTradeCardsByExactNamesUseCase(repository = get()) }
    factory { SearchTradeCardPrintsUseCase(repository = get()) }
    factory { SearchMarketPlaceCardsUseCase(repository = get()) }
    factory { LoadRecentMarketPlaceCardsUseCase(repository = get()) }
    factory { LoadMarketPlaceSellersUseCase(repository = get()) }
    factory { EnsureMarketPlaceChatUseCase(repository = get()) }
    factory { LoadTradeListEntriesUseCase(repository = get()) }
    factory { LoadMapPinsUseCase(repository = get()) }
    factory { ReplaceTradeListEntriesUseCase(repository = get()) }
    factory { ReplaceMapPinsUseCase(repository = get()) }
    single { SellListTransferStore() }
    factory { TradeViewModel() }
    factory {
        CollectionViewModel(
            searchCards = get(),
            resolveCardsByExactNames = get(),
            searchCardPrints = get(),
            sellListTransferStore = get(),
            observeAuthState = get(),
            loadTradeListEntries = get(),
            replaceTradeListEntries = get(),
        )
    }
    factory {
        MarketPlaceViewModel(
            observeAuthState = get(),
            searchMarketPlaceCards = get(),
            loadRecentMarketPlaceCards = get(),
            loadMarketPlaceSellers = get(),
            ensureMarketPlaceChat = get(),
            loadMapPins = get(),
            replaceMapPins = get(),
        )
    }
    factory {
        BuyListViewModel(
            searchCards = get(),
            searchCardPrints = get(),
            observeAuthState = get(),
            loadTradeListEntries = get(),
            replaceTradeListEntries = get(),
            loadMapPins = get(),
            replaceMapPins = get(),
        )
    }
    factory {
        SellListViewModel(
            searchCards = get(),
            searchCardPrints = get(),
            sellListTransferStore = get(),
            observeAuthState = get(),
            loadTradeListEntries = get(),
            replaceTradeListEntries = get(),
            loadMapPins = get(),
            replaceMapPins = get(),
        )
    }
}
