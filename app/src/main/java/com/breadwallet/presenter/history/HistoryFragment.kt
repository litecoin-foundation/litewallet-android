package com.breadwallet.presenter.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.breadwallet.databinding.FragmentHistoryBinding
import com.breadwallet.presenter.activities.BreadActivity
import com.breadwallet.presenter.base.BaseFragment
import com.breadwallet.tools.manager.AnalyticsManager
import com.breadwallet.tools.manager.BRSharedPrefs
import com.breadwallet.tools.manager.BRSharedPrefs.OnIsoChangedListener
import com.breadwallet.tools.manager.TxManager
import com.breadwallet.tools.sqlite.TransactionDataSource.OnTxAddedListener
import com.breadwallet.tools.threads.BRExecutor
import com.breadwallet.tools.util.BRConstants
import com.breadwallet.wallet.BRPeerManager
import com.breadwallet.wallet.BRPeerManager.OnTxStatusUpdate
import com.breadwallet.wallet.BRWalletManager
import com.breadwallet.wallet.BRWalletManager.OnBalanceChanged
import timber.log.Timber

/** Litewallet
 * Created by Mohamed Barry on 6/1/20
 * email: mosadialiou@gmail.com
 * Copyright © 2020 Litecoin Foundation. All rights reserved.
 */
class HistoryFragment :
    BaseFragment<HistoryPresenter>(),
    OnBalanceChanged,
    OnTxStatusUpdate,
    OnIsoChangedListener,
    OnTxAddedListener,
    HistoryView {
    lateinit var binding: FragmentHistoryBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        TxManager.getInstance().init(requireActivity() as BreadActivity, binding.recyclerView)
    }

    private fun addObservers() {
        BRWalletManager.getInstance().addBalanceChangedListener(this)
        BRPeerManager.getInstance().addStatusUpdateListener(this)
        BRSharedPrefs.addIsoChangedListener(this)
    }

    private fun removeObservers() {
        BRWalletManager.getInstance().removeListener(this)
        BRPeerManager.getInstance().removeListener(this)
        BRSharedPrefs.removeListener(this)
    }
    private fun registerAnalyticsError(errorString: String) {
        val params = Bundle()
        params.putString("lwa_error_message", errorString);
        AnalyticsManager.logCustomEventWithParams(BRConstants._20200112_ERR, params)
        Timber.d("History Fragment: RegisterError : %s", errorString)
    }
    override fun onResume() {
        super.onResume()
        addObservers()

        if (this.activity == null) {
            registerAnalyticsError("null_in_history_fragment_on_resume")
        }
        else {
            TxManager.getInstance().onResume(this.activity)
        }
    }

    override fun onPause() {
        super.onPause()
        removeObservers()
    }

    override fun onBalanceChanged(balance: Long) {
        updateUI()
    }

    override fun onStatusUpdate() {
        BRExecutor.getInstance().forBackgroundTasks().execute {
            if (this.activity == null) {
                registerAnalyticsError("null_in_history_fragment_on_status_update")
            }
            else {
                TxManager.getInstance().updateTxList(this.activity)
            }
        }
    }

    override fun onIsoChanged(iso: String) {
        updateUI()
    }

    override fun onTxAdded() {
        BRExecutor.getInstance().forBackgroundTasks().execute {
            if (this.activity == null) {
                registerAnalyticsError("null_in_history_fragment_on_tx_added")
            }
            else {
                TxManager.getInstance().updateTxList(this.activity)
            }
        }
    }
    private fun updateUI() {
        BRExecutor.getInstance().forLightWeightBackgroundTasks().execute {
            if (this.activity == null) {
                registerAnalyticsError("null_in_history_fragment_update_ui")
            }
            else {
                Thread.currentThread().name = Thread.currentThread().name + "HistoryFragment:updateUI"
                TxManager.getInstance().updateTxList(this.activity)
            }
        }
    }

    override fun initPresenter() = HistoryPresenter(this)
}
