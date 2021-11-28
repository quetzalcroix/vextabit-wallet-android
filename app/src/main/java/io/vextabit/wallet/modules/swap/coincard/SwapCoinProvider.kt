package io.vextabit.wallet.modules.swap.coincard

import io.vextabit.wallet.core.IAdapterManager
import io.vextabit.wallet.core.ICoinManager
import io.vextabit.wallet.core.IRateManager
import io.vextabit.wallet.core.IWalletManager
import io.vextabit.wallet.entities.CurrencyValue
import io.vextabit.wallet.modules.swap.SwapMainModule.Blockchain
import io.vextabit.wallet.modules.swap.SwapMainModule.CoinBalanceItem
import io.vextabit.wallet.modules.swap.SwapMainModule.Dex
import io.vextabit.coinkit.models.Coin
import io.vextabit.coinkit.models.CoinType
import io.horizontalsystems.core.ICurrencyManager
import java.math.BigDecimal
import java.util.*

class SwapCoinProvider(
    private val dex: Dex,
    private val coinManager: ICoinManager,
    private val walletManager: IWalletManager,
    private val adapterManager: IAdapterManager,
    private val currencyManager: ICurrencyManager,
    private val xRateManager: IRateManager
) {

    fun getCoins(): List<CoinBalanceItem> {
        val enabledCoinItems = walletItems.filter { item ->
            val zeroBalance = item.balance == BigDecimal.ZERO
            dexSupportsCoin(item.coin) && !zeroBalance
        }.sortedBy { it.coin.title.lowercase(Locale.ENGLISH) }

        val disabledCoinItems = coinManager.coins.filter { coin ->
            dexSupportsCoin(coin) && !enabledCoinItems.any { it.coin == coin }
        }.map { coin ->
            val balance = balance(coin)

            CoinBalanceItem(coin, balance, getFiatValue(coin, balance))
        }.sortedBy { it.coin.title.lowercase(Locale.ENGLISH) }

        return enabledCoinItems + disabledCoinItems
    }

    private fun dexSupportsCoin(coin: Coin) = when (coin.type) {
        CoinType.Ethereum, is CoinType.Erc20 -> dex.blockchain == Blockchain.Ethereum
        CoinType.BinanceSmartChain, is CoinType.Bep20 -> dex.blockchain == Blockchain.BinanceSmartChain
        else -> false
    }

    private val walletItems: List<CoinBalanceItem>
        get() = walletManager.activeWallets.map { wallet ->
            val balance = adapterManager.getBalanceAdapterForWallet(wallet)?.balanceData?.available

            CoinBalanceItem(wallet.coin, balance, getFiatValue(wallet.coin, balance))
        }

    private fun getFiatValue(coin: Coin, balance: BigDecimal?): CurrencyValue? {
        return balance?.let {
            getXRate(coin)?.multiply(it)
        }?.let { fiatBalance ->
            CurrencyValue(currencyManager.baseCurrency, fiatBalance)
        }
    }

    private fun getXRate(coin: Coin): BigDecimal? {
        val currency = currencyManager.baseCurrency
        return xRateManager.latestRate(coin.type, currency.code)?.let {
            if (it.isExpired()) {
                null
            } else {
                it.rate
            }
        }
    }

    private fun balance(coin: Coin): BigDecimal? {
        val wallet = walletManager.activeWallets.firstOrNull { it.coin == coin }
        return wallet?.let { adapterManager.getBalanceAdapterForWallet(it)?.balanceData?.available }
    }

}
