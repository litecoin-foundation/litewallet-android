package com.breadwallet.presenter.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.breadwallet.R;
import com.breadwallet.presenter.customviews.BRDialogView;
import com.breadwallet.presenter.entities.PartnerNames;
import com.breadwallet.presenter.entities.TransactionItem;
import com.breadwallet.tools.manager.AnalyticsManager;
import com.breadwallet.tools.manager.BRSharedPrefs;
import com.breadwallet.tools.manager.FeeManager;
import com.breadwallet.tools.security.BRSender;
import com.breadwallet.tools.util.BRConstants;
import com.breadwallet.tools.util.BRCurrency;
import com.breadwallet.tools.util.BRExchange;
import com.breadwallet.tools.util.Utils;
import com.breadwallet.wallet.BRWalletManager;

import java.math.BigDecimal;

/**
 * Litewallet
 * Created by Mohamed Barry on 3/2/20
 * email: mosadialiou@gmail.com
 * Copyright © 2020 Litecoin Foundation. All rights reserved.
 */
public class DynamicDonationFragment extends Fragment {

    static final long BALANCE_STEP = 1_000_000;

    private long currentBalance;

    private TextView addressVal;
    private TextView amountVal;
    private TextView feeVal;
    private TextView totalVal;

    private TextView donationToTheLitewalletTeam;

    private TextView amountSliderVal;

    private SeekBar seekBar;
    private String selectedIso;
    private boolean isLTCSwap = true;
    private String chosenAddress = BRConstants.DONATION_ADDRESS;
    private long mDonationAmount;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dynamic_donation, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        selectedIso = BRSharedPrefs.getIsoSymbol(getContext());
        isLTCSwap = BRSharedPrefs.getPreferredLTC(getContext());

        addressVal = view.findViewById(R.id.addressVal);
        addressVal.setText(chosenAddress);

        donationToTheLitewalletTeam = view.findViewById(R.id.donationAddressesPhrase);
        donationToTheLitewalletTeam.setText(getString(R.string.Donate_toThe_LWTeam));

        TextView processingTimeLbl = view.findViewById(R.id.processingTimeLbl);
        processingTimeLbl.setText(getString(R.string.Confirmation_processingAndDonationTime, "2.5-5"));

        amountVal = view.findViewById(R.id.amountVal);
        feeVal = view.findViewById(R.id.feeVal);
        totalVal = view.findViewById(R.id.totalVal);

        Button cancelBut = view.findViewById(R.id.cancelBut);
        cancelBut.setOnClickListener(v -> {
            AnalyticsManager.logCustomEvent(BRConstants._20200225_DCD);
            getActivity().onBackPressed();
        });

        Button donateBut = view.findViewById(R.id.donateBut);
        donateBut.setOnClickListener(v -> {
            BRDialogView dialog = new BRDialogView();
            dialog.setTitle(getString(R.string.Donate_Dialog_title));

            dialog.setMessage(getString(R.string.Donate_Dialog_message));
            dialog.setNegButton(getString(R.string.Donate_Dialog_Negative_text));
            dialog.setPosButton(getString(R.string.Donate_Dialog_Positive_text));
            dialog.setPosListener(brDialogView -> {
                dialog.dismiss();
                sendDonation();
            });
            dialog.setNegListener(brDialogView -> dialog.dismiss());
            dialog.show(((Activity) getActivity()).getFragmentManager(), dialog.getClass().getName());

        });

        amountSliderVal = view.findViewById(R.id.amountSliderVal);

        seekBar = view.findViewById(R.id.seekBar);

        ImageButton upAmountBut = view.findViewById(R.id.upAmountBut);
        ImageButton downAmountBut = view.findViewById(R.id.downAmountBut);

        upAmountBut.setOnClickListener(v -> {
            seekBar.incrementProgressBy(diff());

            long newAmount = newAmount(seekBar.getProgress());
            updateDonationValues(newAmount);
        });

        downAmountBut.setOnClickListener(v -> {
            seekBar.incrementProgressBy(-diff());

            long newAmount = newAmount(seekBar.getProgress());
            updateDonationValues(newAmount);
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateDonationValues(newAmount(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //NO-OP
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //NO-OP
            }
        });

        setFeeToRegular();

        currentBalance = BRSharedPrefs.getCatchedBalance(getContext());

        updateDonationValues(BRConstants.DONATION_AMOUNT);
    }

    private void sendDonation() {
        String memo = getString(R.string.Donate_toThe_LWTeam) + chosenAddress;
        TransactionItem request = new TransactionItem(chosenAddress,
                null,
                null,
                mDonationAmount,
                0,
                null,
                false,
                memo);

        Bundle params = new Bundle();
        params.putString("donation_address", chosenAddress);
        params.putLong("donation_amount", mDonationAmount);
        params.putString("address_scheme", "v2");
        AnalyticsManager.logCustomEventWithParams(BRConstants._20200223_DD, params);
        BRSender.getInstance().sendTransaction(getContext(), request);
    }

    private void setFeeToRegular() {
        FeeManager feeManager = FeeManager.getInstance();

        //TODO: This should be inserted into the FeeManager after v0.4.0
        AnalyticsManager.logCustomEvent(BRConstants._20200301_DUDFPK);

        feeManager.resetFeeType();
        BRWalletManager.getInstance().setFeePerKb(feeManager.currentFees.regular);
    }

    private int diff() {
        float step = (currentBalance - BRConstants.DONATION_AMOUNT) * 1f / BALANCE_STEP;
        int diff = (int) (seekBar.getMax() * 1f / step);
        return Math.max(diff, 1);
    }

    private long newAmount(int progress) {
        long maxFee = BRWalletManager.getInstance().feeForTransactionAmount(currentBalance);
        long adjustedAmount = (long) ((progress * 1f / seekBar.getMax()) * (currentBalance - BRConstants.DONATION_AMOUNT - maxFee));
        return adjustedAmount + BRConstants.DONATION_AMOUNT;
    }

    private void updateDonationValues(long donationAmount) {
        mDonationAmount = donationAmount;
        final BigDecimal donation = new BigDecimal(donationAmount);

        long feeAmount = BRWalletManager.getInstance().feeForTransactionAmount(donationAmount);
        final BigDecimal fee = new BigDecimal(feeAmount);

        final BigDecimal total = new BigDecimal(donationAmount + feeAmount);

        amountVal.setText(formatResultAmount(formatLtcAmount(donation), formatIsoAmount(donation)));
        feeVal.setText(formatResultAmount(formatLtcAmount(fee), formatIsoAmount(fee)));
        totalVal.setText(formatResultAmount(formatLtcAmount(total), formatIsoAmount(total)));

        amountSliderVal.setText(totalVal.getText());
    }

    private String formatLtcAmount(BigDecimal amount) {
        BigDecimal ltcAmount = BRExchange.getLitecoinForLitoshis(getContext(), amount);
        return BRCurrency.getFormattedCurrencyString(getContext(), "LTC", ltcAmount);
    }

    private String formatIsoAmount(BigDecimal amount) {
        BigDecimal fiatAmount = BRExchange.getAmountFromLitoshis(getContext(), selectedIso, amount);
        return BRCurrency.getFormattedCurrencyString(getContext(), selectedIso, fiatAmount);
    }

    private String formatResultAmount(String ltcAmount, String isoAmount) {
        String format = "%s (%s)";
        if (isLTCSwap) {
            return String.format(format, ltcAmount, isoAmount);
        } else {
            return String.format(format, isoAmount, ltcAmount);
        }
    }
}
