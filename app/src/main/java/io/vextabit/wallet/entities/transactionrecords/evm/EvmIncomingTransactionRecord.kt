package io.vextabit.wallet.entities.transactionrecords.evm

import io.vextabit.wallet.entities.CoinValue
import io.vextabit.coinkit.models.Coin
import io.horizontalsystems.ethereumkit.models.FullTransaction
import java.math.BigDecimal

class EvmIncomingTransactionRecord(
    fullTransaction: FullTransaction,
    baseCoin: Coin,
    amount: BigDecimal,
    val from: String,
    token: Coin,
    override val foreignTransaction: Boolean = false
) : EvmTransactionRecord(fullTransaction, baseCoin) {

    val value: CoinValue = CoinValue(token, amount)

    override val mainValue = value
}
