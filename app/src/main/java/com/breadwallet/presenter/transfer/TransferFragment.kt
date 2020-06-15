package com.breadwallet.presenter.transfer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.breadwallet.R
import com.breadwallet.presenter.base.BaseFragment


/** Litewallet
 * Created by Mohamed Barry on 6/14/20
 * email: mosadialiou@gmail.com
 * Copyright © 2020 Litecoin Foundation. All rights reserved.
 */
class TransferFragment : BaseFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_transfer, container, false)
    }
}