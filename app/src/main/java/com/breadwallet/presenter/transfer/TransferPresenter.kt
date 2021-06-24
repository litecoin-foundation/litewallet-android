package com.breadwallet.presenter.transfer

import com.breadwallet.BreadApp
import com.breadwallet.presenter.base.BasePresenter
import com.breadwallet.tools.manager.BRSharedPrefs
import org.litecoin.partnerapi.callback.WalletCallback
import org.litecoin.partnerapi.model.Wallet
import org.litecoin.partnerapi.network.WalletClient
import javax.inject.Inject

/** Litewallet
 * Created by Mohamed Barry on 6/30/20
 * email: mosadialiou@gmail.com
 * Copyright © 2020 Litecoin Foundation. All rights reserved.
 */
class TransferPresenter(view: TransferView) : BasePresenter<TransferView>(view) {

    @Inject
    lateinit var walletClient: WalletClient

    override fun subscribe() {
    }

    override fun unsubscribe() {
    }

    fun logout() {
        BRSharedPrefs.logoutFromLitecoinCard(BreadApp.getBreadContext())
    }

    fun getWalletDetails() {
        view?.showProgress()
        val userId = BRSharedPrefs.getLitecoinCardId(BreadApp.getBreadContext())
        walletClient.details(userId, object : WalletCallback {
            override fun onDetailsFetched(wallet: Wallet) {
                view?.hideProgress()
                (view as TransferView?)?.onWalletDetails(wallet)
            }

            override fun onUnknownSystemError() {
                view?.hideProgress()
                view?.showError("Oops something wrong happened. Please try again later")
            }

            override fun onTokenExpired() {
                view?.hideProgress()
                view?.onTokenExpired()
            }

            override fun onFailure(error: Int) {
                view?.hideProgress()
                view?.showError(error)
            }
        })
    }
}
