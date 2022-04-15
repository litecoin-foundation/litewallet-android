package com.breadwallet.presenter.spend

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import com.breadwallet.R
import com.breadwallet.databinding.FragmentRegisterBinding
import com.breadwallet.entities.Country
import com.breadwallet.presenter.base.BaseFragment
import com.breadwallet.tools.util.CountryHelper
import com.breadwallet.tools.util.onError
import com.breadwallet.tools.util.text
import com.github.razir.progressbutton.bindProgressButton
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.showProgress

/** Litewallet
 * Created by Mohamed Barry on 6/3/20
 * email: mosadialiou@gmail.com
 * Copyright © 2020 Litecoin Foundation. All rights reserved.
 */
class RegisterFragment : BaseFragment<RegisterPresenter>(), RegisterView {

    lateinit var binding: FragmentRegisterBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolBar.setNavigationOnClickListener {
            goToLogin()
        }
        binding.registerViewContainer.countryField.editText?.keyListener = null
        binding.registerViewContainer.countryField.editText?.run {
            // FIXME: on some devices CountryHelper.countries.find { it.name == "" } returns a NULL country.
            //  Don't yet know the real cause but since we only handle USA, let's just hardcode it for now.
            //  So it's a temporary fix.
            val country = CountryHelper.countries.find { it.name == CountryHelper.usaCountry.name }
                ?: CountryHelper.usaCountry
            setText(country.name)
            tag = country
        }

        binding.registerViewContainer.submitBut.setOnClickListener { handleSubmit() }
        bindProgressButton(binding.registerViewContainer.submitBut)
    }

    private fun goToLogin() {
        (parentFragment as AuthBottomSheetDialogFragment?)?.onBackPressed()
    }

    private fun handleSubmit() {
        with(binding.registerViewContainer) {
            presenter.register(
                firstNameField.text(),
                lastNameField.text(),
                emailField.text(),
                passwordField.text(),
                confirmPasswordField.text(),
                addressField.text(),
                null,
                cityField.text(),
                stateField.text(),
                postalCodeField.text(),
                countryField.editText?.tag as Country,
                mobileNumberField.text()
            )
        }
    }

    override fun onWrongFirstName(errorResId: Int) {
        binding.registerViewContainer.firstNameField.onError(errorResId)
    }

    override fun onWrongLastName(errorResId: Int) {
        binding.registerViewContainer.lastNameField.onError(errorResId)
    }

    override fun onWrongAddress1(errorResId: Int) {
        binding.registerViewContainer.addressField.onError(errorResId)
    }

    override fun onWrongCity(errorResId: Int) {
        binding.registerViewContainer.cityField.onError(errorResId)
    }

    override fun onWrongState(errorResId: Int) {
        binding.registerViewContainer.stateField.onError(errorResId)
    }

    override fun onWrongPostalCode(errorResId: Int) {
        binding.registerViewContainer.postalCodeField.onError(errorResId)
    }

    override fun onWrongCountry(errorResId: Int) {
        binding.registerViewContainer.countryField.onError(errorResId)
    }

    override fun onWrongPhone(errorResId: Int) {
        binding.registerViewContainer.mobileNumberField.onError(errorResId)
    }

    override fun onWrongEmail(errorResId: Int) {
        binding.registerViewContainer.emailField.onError(errorResId)
    }

    override fun onWrongPassword(errorResId: Int) {
        binding.registerViewContainer.passwordField.onError(errorResId)
    }

    override fun onWrongConfirmPassword(errorResId: Int) {
        binding.registerViewContainer.confirmPasswordField.onError(errorResId)
    }

    override fun showProgress() {
        with(binding.registerViewContainer) {
            submitBut.showProgress { progressColor = Color.WHITE }
            submitBut.isEnabled = false
        }
    }

    override fun hideProgress() {
        with(binding.registerViewContainer) {
            submitBut.hideProgress(R.string.Button_submit)
            submitBut.isEnabled = true
        }
    }

    override fun onRegisteredSuccessful() {
        AlertDialog.Builder(requireContext())
            .setMessage(R.string.Register_Dialog_registeredSuccessMessage).setPositiveButton(
            android.R.string.ok
        ) { _, _ ->
            goToLogin()
        }.show()
    }

    override fun initPresenter() = RegisterPresenter(this)
}
