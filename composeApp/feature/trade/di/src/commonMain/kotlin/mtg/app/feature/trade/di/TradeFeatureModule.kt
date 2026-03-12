package mtg.app.feature.trade.di

import mtg.app.feature.trade.data.ScryfallDataSource
import mtg.app.feature.trade.data.TradeDataSource
import mtg.app.feature.trade.data.remote.DefaultScryfallDataSource
import mtg.app.feature.trade.data.remote.DefaultRemoteTradeDataSource
import mtg.app.feature.trade.domain.DefaultTradeService
import mtg.app.feature.trade.domain.TradeService
import mtg.app.feature.trade.domain.TradeRepository
import mtg.app.feature.trade.infrastructure.DefaultTradeRepository
import mtg.app.feature.trade.presentation.buylist.BuyListViewModel
import mtg.app.feature.trade.presentation.collection.CollectionViewModel
import mtg.app.feature.trade.presentation.marketplace.MarketPlaceViewModel
import mtg.app.feature.trade.presentation.selllist.SellListViewModel
import mtg.app.feature.trade.presentation.trade.TradeViewModel
import mtg.app.feature.trade.presentation.utils.SellListTransferStore
import org.koin.dsl.module

val tradeFeatureModule = module {
    factory<ScryfallDataSource> { DefaultScryfallDataSource(httpClient = get()) }
    factory<TradeDataSource> { DefaultRemoteTradeDataSource(apiCallHandler = get()) }
    factory<TradeRepository> { DefaultTradeRepository(scryfallDataSource = get(), dataSource = get()) }
    factory<TradeService> { DefaultTradeService(repository = get()) }
    single { SellListTransferStore() }
    factory { TradeViewModel() }
    factory {
        CollectionViewModel(
            tradeService = get(),
            sellListTransferStore = get(),
            authService = get(),
        )
    }
    factory {
        MarketPlaceViewModel(
            authService = get(),
            tradeService = get(),
        )
    }
    factory {
        BuyListViewModel(
            tradeService = get(),
            authService = get(),
        )
    }
    factory {
        SellListViewModel(
            tradeService = get(),
            sellListTransferStore = get(),
            authService = get(),
        )
    }
}
