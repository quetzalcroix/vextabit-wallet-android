package io.vextabit.wallet.entities.transactionrecords.binancechain

import io.vextabit.wallet.entities.CoinValue
import io.horizontalsystems.binancechainkit.models.TransactionInfo
import io.vextabit.coinkit.models.Coin

class BinanceChainOutgoingTransactionRecord(
    transaction: TransactionInfo,
    feeCoin: Coin,
    coin: Coin,
    val sentToSelf: Boolean
) : BinanceChainTransactionRecord(transaction, feeCoin) {
    val value = CoinValue(coin, transaction.amount.toBigDecimal())
    val to = transaction.to

    override val mainValue: CoinValue = value

}
