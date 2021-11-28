package io.vextabit.wallet.entities

import io.vextabit.coinkit.models.CoinType

data class InitialSyncSetting(val coinType: CoinType,
                              var syncMode: SyncMode)
