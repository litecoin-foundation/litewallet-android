package com.breadwallet.presenter.spend

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.core.text.underline
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentTransaction
import com.breadwallet.R
import com.breadwallet.presenter.base.BaseFragment
import com.breadwallet.tools.util.addFragment
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.android.synthetic.main.view_login.*

/** Litewallet
 * Created by Mohamed Barry on 6/10/20
 * email: mosadialiou@gmail.com
 * Copyright © 2020 Litecoin Foundation. All rights reserved.
 */
class LoginFragment : BaseFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolBar.setNavigationOnClickListener { (parentFragment as DialogFragment?)?.dismiss() }
        loginBut.setOnClickListener { handleLogin() }
        registerBut.setOnClickListener { handleRegister() }
        forgetPwdBut.text = buildSpannedString {
            underline { append(forgetPwdBut.text) }
        }
    }

    private fun handleRegister() {
        parentFragment?.addFragment(RegisterFragment(), transition = FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
    }

    private fun handleLogin() {

    }
}