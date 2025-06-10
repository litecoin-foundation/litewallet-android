package com.breadwallet.presenter.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.breadwallet.databinding.FragmentHistoryBinding
import com.breadwallet.presenter.activities.BreadActivity
import com.breadwallet.presenter.base.BaseFragment
import com.breadwallet.tools.manager.BRSharedPrefs
import com.breadwallet.tools.manager.BRSharedPrefs.OnIsoChangedListener
import com.breadwallet.tools.manager.TxManager
import com.breadwallet.tools.sqlite.TransactionDataSource.OnTxAddedListener
import com.breadwallet.tools.threads.BRExecutor
import com.breadwallet.wallet.BRPeerManager
import com.breadwallet.wallet.BRPeerManager.OnTxStatusUpdate
import com.breadwallet.wallet.BRWalletManager
import com.breadwallet.wallet.BRWalletManager.OnBalanceChanged
import timber.log.Timber

/**
 * Litewallet Created by Mohamed Barry on 6/1/20 email: mosadialiou@gmail.com Copyright © 2020
 * Litecoin Foundation. All rights reserved.
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
        setupDeprecationWarning()
    }

    private fun setupDeprecationWarning() {
        // Get Nexus Wallet button click
        binding.getNexusWalletButton.setOnClickListener {
            try {
                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW)
                intent.data = android.net.Uri.parse("market://details?id=com.litecoin.nexus")
                startActivity(intent)
            } catch (e: Exception) {
                // Fallback to web browser if Play Store is not available
                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW)
                intent.data = android.net.Uri.parse("https://play.google.com/store/apps/details?id=com.litecoin.nexus")
                startActivity(intent)
            }
        }

        // Learn more button click
        binding.learnMoreButton.setOnClickListener {
            try {
                com.breadwallet.presenter.activities.WebViewActivity.start(
                    requireActivity(),
                    "https://support.nexuswallet.com/hc/nexus-help-center/articles/1749345713-start-using-nexus-wallet-a-simple-guide-for-litewallet-users",
                    "Nexus Wallet"
                )
            } catch (e: Exception) {
                timber.log.Timber.e("Failed to open Nexus Help Center: %s", e.message)
            }
        }

        // Close button click
        binding.deprecationCloseButton.setOnClickListener {
            binding.deprecationWarningContainer.visibility = android.view.View.GONE
        }
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
        params.putString("lwa_error_message", errorString)
        Timber.d("History Fragment: RegisterError : %s", errorString)
    }
    override fun onResume() {
        super.onResume()
        addObservers()

        if (this.activity == null) {
            registerAnalyticsError("null_in_history_fragment_on_resume")
        } else {
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
            } else {
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
            } else {
                TxManager.getInstance().updateTxList(this.activity)
            }
        }
    }
    private fun updateUI() {
        BRExecutor.getInstance().forLightWeightBackgroundTasks().execute {
            if (this.activity == null) {
                registerAnalyticsError("null_in_history_fragment_update_ui")
            } else {
                Thread.currentThread().name =
                        Thread.currentThread().name + "HistoryFragment:updateUI"
                TxManager.getInstance().updateTxList(this.activity)
            }
        }
    }

    override fun initPresenter() = HistoryPresenter(this)
}
