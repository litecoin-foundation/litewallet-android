package com.breadwallet.presenter.transfer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.breadwallet.R
import com.breadwallet.presenter.activities.BreadActivity
import com.breadwallet.presenter.base.BaseFragment
import com.breadwallet.tools.util.BRCurrency
import com.breadwallet.tools.util.BRExchange
import kotlinx.android.synthetic.main.fragment_transfer.*
import org.litecoin.partnerapi.model.Wallet
import java.math.BigDecimal

/** Litewallet
 * Created by Mohamed Barry on 6/14/20
 * email: mosadialiou@gmail.com
 * Copyright © 2020 Litecoin Foundation. All rights reserved.
 */
class TransferFragment : BaseFragment<TransferPresenter>(), TransferView {

    private var mWallet: Wallet? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_transfer, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        logoutBut.setOnClickListener {
            presenter.logout()
            (requireActivity() as BreadActivity?)?.recreate()
        }
        presenter.getWalletDetails()
    }

    override fun initPresenter() = TransferPresenter(this)

    override fun onWalletDetails(wallet: Wallet) {
        progressBar.visibility = View.GONE
        mWallet = wallet
        with(cardBalanceAmountLbl) {
            val amount = BRExchange.ltcToLitoshi(wallet.spendableBalance)
            text = getAvailableBalanceInLTC(LTC_ISO, amount)
            visibility = View.VISIBLE
        }
    }

    override fun hideProgress() {
        super.hideProgress()
        progressBar.visibility = View.GONE
    }

    override fun onTokenExpired() {
        super.onTokenExpired()
        presenter.logout()
        (activity as BreadActivity?)?.showAuthModal()
    }

    private fun getAvailableBalanceInLTC(iso: String, amount: BigDecimal): String {
        // amount in LTC units
        val ltcAmount = BRExchange.getBitcoinForSatoshis(requireContext(), amount)
        return BRCurrency.getFormattedCurrencyString(requireContext(), iso, ltcAmount)
    }

    companion object {
        const val LTC_ISO = "LTC"
    }
}
