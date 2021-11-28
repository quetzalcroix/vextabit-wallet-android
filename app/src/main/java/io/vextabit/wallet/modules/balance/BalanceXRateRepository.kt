package io.vextabit.wallet.modules.balance

import io.vextabit.wallet.core.IRateManager
import io.vextabit.wallet.core.subscribeIO
import io.vextabit.coinkit.models.CoinType
import io.horizontalsystems.core.ICurrencyManager
import io.vextabit.xrateskit.entities.LatestRate
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject

class BalanceXRateRepository(
    private val currencyManager: ICurrencyManager,
    private val xRateManager: IRateManager
) {
    val baseCurrency by currencyManager::baseCurrency
    private var coinTypes = listOf<CoinType>()

    private var latestRateDisposable: Disposable? = null
    private var baseCurrencyDisposable: Disposable? = null

    private val itemSubject = PublishSubject.create<Map<CoinType, LatestRate?>>()
    val itemObservable: Observable<Map<CoinType, LatestRate?>> get() = itemSubject
        .doOnSubscribe {
            subscribeForBaseCurrencyUpdate()
            subscribeForLatestRateUpdates()
        }
        .doFinally {
            unsubscribeFromBaseCurrencyUpdate()
            unsubscribeFromLatestRateUpdates()
        }

    private fun subscribeForBaseCurrencyUpdate() {
        baseCurrencyDisposable = currencyManager.baseCurrencyUpdatedSignal
            .subscribeIO {
                unsubscribeFromLatestRateUpdates()
                itemSubject.onNext(getLatestRates())
                subscribeForLatestRateUpdates()
            }
    }

    private fun unsubscribeFromBaseCurrencyUpdate() {
        baseCurrencyDisposable?.dispose()
    }

    fun setCoinTypes(coinTypes: List<CoinType>) {
        unsubscribeFromLatestRateUpdates()
        this.coinTypes = coinTypes
        subscribeForLatestRateUpdates()
    }

    fun getLatestRates(): Map<CoinType, LatestRate?> {
        return coinTypes.map { it to null }.toMap() + xRateManager.latestRate(coinTypes, baseCurrency.code)
    }

    fun refresh() {
        xRateManager.refresh(baseCurrency.code)
    }

    private fun subscribeForLatestRateUpdates() {
        latestRateDisposable = xRateManager.latestRateObservable(coinTypes, baseCurrency.code)
            .subscribeIO {
                itemSubject.onNext(it)
            }
    }

    private fun unsubscribeFromLatestRateUpdates() {
        latestRateDisposable?.dispose()
    }
}