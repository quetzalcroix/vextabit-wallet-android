package io.vextabit.wallet.modules.sendevm

import android.os.Parcelable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.vextabit.wallet.R
import io.vextabit.wallet.core.App
import io.vextabit.wallet.core.ISendEthereumAdapter
import io.vextabit.wallet.core.ethereum.EvmCoinService
import io.vextabit.wallet.core.fiat.AmountTypeSwitchServiceSendEvm
import io.vextabit.wallet.core.fiat.FiatServiceSendEvm
import io.vextabit.wallet.core.providers.Translator
import io.vextabit.wallet.entities.Wallet
import io.vextabit.wallet.modules.swap.settings.AddressResolutionService
import io.vextabit.wallet.modules.swap.settings.RecipientAddressViewModel
import io.vextabit.coinkit.models.Coin
import io.horizontalsystems.ethereumkit.models.TransactionData
import kotlinx.android.parcel.Parcelize
import java.math.BigDecimal
import java.math.BigInteger


data class SendEvmData(
        val transactionData: TransactionData,
        val additionalInfo: AdditionalInfo? = null
) {
    sealed class AdditionalInfo : Parcelable {
        @Parcelize
        class Send(val info: SendInfo) : AdditionalInfo()

        @Parcelize
        class Uniswap(val info: UniswapInfo) : AdditionalInfo()

        @Parcelize
        class OneInchSwap(val info: OneInchSwapInfo): AdditionalInfo()

        val sendInfo: SendInfo?
            get() = (this as? Send)?.info

        val uniswapInfo: UniswapInfo?
            get() = (this as? Uniswap)?.info

        val oneInchSwapInfo: OneInchSwapInfo?
            get() = (this as? OneInchSwap)?.info
    }

    @Parcelize
    data class SendInfo(
            val domain: String?
    ) : Parcelable

    @Parcelize
    data class UniswapInfo(
            val estimatedOut: BigDecimal,
            val estimatedIn: BigDecimal,
            val slippage: String? = null,
            val deadline: String? = null,
            val recipientDomain: String? = null,
            val price: String? = null,
            val priceImpact: String? = null,
            val gasPrice: String? = null
    ) : Parcelable

    @Parcelize
    data class OneInchSwapInfo(
        val coinTo: Coin,
        val estimatedAmountTo: BigDecimal,
        val slippage: String? = null,
        val recipientDomain: String? = null
    ): Parcelable
}

object SendEvmModule {

    const val walletKey = "walletKey"
    const val transactionDataKey = "transactionData"
    const val additionalInfoKey = "additionalInfo"

    @Parcelize
    data class TransactionDataParcelable(
            val toAddress: String,
            val value: BigInteger,
            val input: ByteArray
    ) : Parcelable {
        constructor(transactionData: TransactionData) : this(transactionData.to.hex, transactionData.value, transactionData.input)
    }


    class Factory(private val wallet: Wallet) : ViewModelProvider.Factory {
        private val adapter by lazy { io.vextabit.wallet.core.App.adapterManager.getAdapterForWallet(wallet) as ISendEthereumAdapter }
        private val service by lazy { SendEvmService(wallet.coin, adapter) }

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return when (modelClass) {
                SendEvmViewModel::class.java -> {
                    SendEvmViewModel(service, listOf(service)) as T
                }
                AmountInputViewModel::class.java -> {
                    val switchService = AmountTypeSwitchServiceSendEvm()
                    val fiatService = FiatServiceSendEvm(switchService, io.vextabit.wallet.core.App.currencyManager, io.vextabit.wallet.core.App.xRateManager)
                    switchService.add(fiatService.toggleAvailableObservable)

                    AmountInputViewModel(service, fiatService, switchService, clearables = listOf(service, fiatService, switchService)) as T
                }
                SendAvailableBalanceViewModel::class.java -> {
                    val coinService = EvmCoinService(wallet.coin, io.vextabit.wallet.core.App.currencyManager, io.vextabit.wallet.core.App.xRateManager)
                    SendAvailableBalanceViewModel(service, coinService, listOf(service, coinService)) as T
                }
                RecipientAddressViewModel::class.java -> {
                    val addressParser = io.vextabit.wallet.core.App.addressParserFactory.parser(wallet.coin)
                    val resolutionService = AddressResolutionService(wallet.coin.code, true)
                    val placeholder = Translator.getString(R.string.SwapSettings_RecipientPlaceholder)
                    RecipientAddressViewModel(service, resolutionService, addressParser, placeholder, listOf(service, resolutionService)) as T
                }
                else -> throw IllegalArgumentException()
            }
        }
    }

}
