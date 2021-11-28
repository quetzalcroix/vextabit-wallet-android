package io.vextabit.wallet.modules.settings.security.privacy

import io.vextabit.wallet.core.App
import io.vextabit.wallet.core.managers.TorStatus
import io.vextabit.wallet.entities.*
import io.vextabit.coinkit.models.Coin
import io.horizontalsystems.views.ListPosition

object PrivacySettingsModule {

    interface IPrivacySettingsView {

        fun showPrivacySettingsInfo()
        fun showNotificationsNotEnabledAlert()
        fun showRestartAlert(checked: Boolean)
        fun toggleTorEnabled(torEnabled: Boolean)
        fun setRestoreWalletSettingsViewItems(items: List<PrivacySettingsViewItem>)
        fun showSyncModeSelectorDialog(syncModeOptions: List<SyncMode>, selected: SyncMode, coin: Coin)
        fun setTransactionsOrdering(transactionsOrdering: TransactionDataSortingType)
        fun showTransactionsSortingOptions(items: List<TransactionDataSortingType>, selectedItem: TransactionDataSortingType)
        fun setTorConnectionStatus(connectionStatus: TorStatus)
    }

    interface IPrivacySettingsViewDelegate {
        fun viewDidLoad()
        fun didSwitchTorEnabled(checked: Boolean)
        fun updateTorState(checked: Boolean)
        fun setTorEnabled(checked: Boolean)
        fun onItemTap(settingType: PrivacySettingsType, position: Int)
        fun onSelectSetting(position: Int)
        fun onTransactionOrderSettingTap()
        fun onSelectTransactionSorting(transactionDataSortingType: TransactionDataSortingType)
        fun onShowPrivacySettingsInfoClick()
        fun clear()
    }

    interface IPrivacySettingsInteractor {
        val wallets: List<Wallet>
        val activeAccount: Account?
        var transactionsSortingType: TransactionDataSortingType
        var isTorEnabled: Boolean
        val isTorNotificationEnabled: Boolean
        fun stopTor()
        fun enableTor()
        fun disableTor()
        fun subscribeToTorStatus()

        fun syncSettings(): List<Triple<InitialSyncSetting, Coin, Boolean>>
        fun saveSyncModeSetting(syncModeSetting: InitialSyncSetting)

        fun clear()
    }

    interface IPrivacySettingsInteractorDelegate {
        fun didStopTor()
        fun onTorConnectionStatusUpdated(connectionStatus: TorStatus)
    }

    interface IPrivacySettingsRouter {
        fun restartApp()
    }

    fun init(view: PrivacySettingsViewModel, router: IPrivacySettingsRouter) {
        val interactor = PrivacySettingsInteractor(
                io.vextabit.wallet.core.App.pinComponent,
                io.vextabit.wallet.core.App.torKitManager,
                io.vextabit.wallet.core.App.initialSyncModeSettingsManager,
                io.vextabit.wallet.core.App.walletManager,
                io.vextabit.wallet.core.App.accountManager,
                io.vextabit.wallet.core.App.localStorage)

        val presenter = PrivacySettingsPresenter(interactor, router)
        interactor.delegate = presenter
        view.delegate = presenter
        presenter.view = view
    }
}

sealed class PrivacySettingsType {
    open val selectedTitle: String = ""

    class RestoreModeSettingType(var selected: SyncMode) : PrivacySettingsType() {
        override val selectedTitle: String
            get() = selected.title
    }
}

data class PrivacySettingsViewItem(
        val title: String,
        val coin: Coin,
        val settingType: PrivacySettingsType,
        var enabled: Boolean = true,
        val listPosition: ListPosition
)
