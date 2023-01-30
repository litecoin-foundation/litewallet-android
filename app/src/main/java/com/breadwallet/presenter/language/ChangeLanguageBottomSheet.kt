package com.breadwallet.presenter.language

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.breadwallet.R
import com.breadwallet.databinding.ChangeLanguageBottomSheetBinding
import com.breadwallet.entities.Language
import com.breadwallet.presenter.activities.intro.IntroActivity
import com.breadwallet.ui.RoundedBottomSheetDialogFragment
import com.breadwallet.tools.util.LocaleHelper
import com.breadwallet.tools.util.Utils
import com.breadwallet.tools.util.getString
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog


/** Litewallet
 * Created by Mohamed Barry on 7/19/21
 * email: mosadialiou@gmail.com
 * Copyright © 2021 Litecoin Foundation. All rights reserved.
 */
class ChangeLanguageBottomSheet : RoundedBottomSheetDialogFragment() {

    lateinit var binding: ChangeLanguageBottomSheetBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ChangeLanguageBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener { dismiss() }

        val currentLanguage = LocaleHelper.instance.currentLocale
        binding.toolbar.title = currentLanguage.desc

        val adapter = LanguageAdapter(Language.values()).apply {
            selectedPosition = currentLanguage.ordinal
            onLanguageChecked = {
                binding.toolbar.title = it.desc
                binding.okButton.text = getString(LocaleHelper.getLocale(it), R.string.Button_ok)
            }
        }
        binding.recyclerView.adapter = adapter

        binding.recyclerView.post {
            binding.recyclerView.scrollToPosition(adapter.selectedPosition)
        }

        binding.okButton.setOnClickListener {
            dismiss()
            if (LocaleHelper.instance.setLocaleIfNeeded(adapter.selectedLanguage())) {
                val intent = Intent(requireContext(), IntroActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext(), theme)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(false)
        val behavior = dialog.behavior
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        behavior.setPeekHeight(0, true)
        behavior.setExpandedOffset(Utils.getPixelsFromDps(context, 16))
        behavior.isFitToContents = false
        behavior.isHideable = true

        behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_DRAGGING) {
                    behavior.state = BottomSheetBehavior.STATE_EXPANDED
                }
            }
        })

        dialog.setOnShowListener {
            val bottomSheet: FrameLayout? = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet)
            val lp = bottomSheet?.layoutParams
            lp?.height = ViewGroup.LayoutParams.MATCH_PARENT
            bottomSheet?.layoutParams = lp
        }
        return dialog
    }
}