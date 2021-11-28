package io.vextabit.wallet.entities

import androidx.room.Entity
import io.vextabit.coinkit.models.CoinType

@Entity(primaryKeys = ["coinType", "key"])
data class BlockchainSetting(
        val coinType: CoinType,
        val key: String,
        val value: String)
