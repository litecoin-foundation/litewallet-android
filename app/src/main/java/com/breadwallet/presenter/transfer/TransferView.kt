package com.breadwallet.presenter.transfer

import com.breadwallet.presenter.base.BaseView
import org.litecoin.partnerapi.model.Wallet

/** Litewallet
 * Created by Mohamed Barry on 6/30/20
 * email: mosadialiou@gmail.com
 * Copyright © 2020 Litecoin Foundation. All rights reserved.
 */
interface TransferView : BaseView {
    fun onWalletDetails(wallet: Wallet)
}
