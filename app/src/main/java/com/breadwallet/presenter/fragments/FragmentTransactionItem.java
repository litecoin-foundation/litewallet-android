package com.breadwallet.presenter.fragments;


import static androidx.core.content.ContextCompat.getSystemService;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.breadwallet.R;
import com.breadwallet.presenter.entities.PartnerNames;
import com.breadwallet.presenter.entities.TxItem;
import com.breadwallet.tools.animation.BRAnimator;
import com.breadwallet.tools.animation.SlideDetector;
import com.breadwallet.tools.manager.BRSharedPrefs;
import com.breadwallet.tools.manager.TxManager;
import com.breadwallet.tools.threads.BRExecutor;
import com.breadwallet.tools.util.BRConstants;
import com.breadwallet.tools.util.BRCurrency;
import com.breadwallet.tools.util.BRExchange;
import com.breadwallet.tools.util.Utils;
import com.breadwallet.wallet.BRPeerManager;
import com.platform.entities.TxMetaData;
import com.platform.tools.KVStoreManager;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import timber.log.Timber;

public class FragmentTransactionItem extends Fragment {

    private static final String ARG_ITEM = "arg_item";

    public TextView mTitle;
    private TextView mLargeDescriptionText;
    private TextView mSubHeader;
    private TextView mConfirmationText;
    private TextView mAvailableSpend;
    private EditText mCommentText;
    private TextView mAddressText;
    private TextView mDateText;
    private TextView mToFromBottom;
    private TextView mTxHash;
    private TxItem item;
    private LinearLayout signalLayout;
    private ImageButton close;
    private String oldComment;
    private TextView mTxHashLink;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.transaction_details_item, container, false);
        signalLayout = (LinearLayout) rootView.findViewById(R.id.signal_layout);
        mTitle = (TextView) rootView.findViewById(R.id.title);
        mLargeDescriptionText = (TextView) rootView.findViewById(R.id.large_description_text);
        mSubHeader = (TextView) rootView.findViewById(R.id.sub_header);
        mCommentText = (EditText) rootView.findViewById(R.id.comment_text);
        mAddressText = (TextView) rootView.findViewById(R.id.address_text);
        mDateText = (TextView) rootView.findViewById(R.id.date_text);
        mToFromBottom = (TextView) rootView.findViewById(R.id.to_from);
        mConfirmationText = (TextView) rootView.findViewById(R.id.confirmation_text);
        mAvailableSpend = (TextView) rootView.findViewById(R.id.available_spend);
        mTxHash = (TextView) rootView.findViewById(R.id.tx_hash);
        mTxHashLink = (TextView) rootView.findViewById(R.id.tx_hash_link);
        close = (ImageButton) rootView.findViewById(R.id.close_button);

        //TODO: all views are using the layout of this button. Views should be refactored without it
        // Hiding until layouts are built.
        ImageButton faq = (ImageButton) rootView.findViewById(R.id.faq_button);

        signalLayout.setOnTouchListener(new SlideDetector(signalLayout, this::close));

        rootView.setOnClickListener(v -> {
            if (!BRAnimator.isClickAllowed()) return;
            close();
        });
        close.setOnClickListener(v -> close());

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getArguments() == null) {
            requireActivity().getSupportFragmentManager().popBackStack();
            return;
        }

        item = (TxItem) getArguments().getSerializable(ARG_ITEM);

        fillTexts();
    }

    private void fillTexts() {
        if (item == null) {
            requireActivity().getSupportFragmentManager().popBackStack();
            return;
        }

        //get the current iso
        String iso = BRSharedPrefs.getPreferredLTC(getActivity()) ? "LTC" : BRSharedPrefs.getIsoSymbol(getContext());

        long opsAmount = getOpsAmount();

        //get the tx amount
        BigDecimal txAmount = new BigDecimal(item.getReceived() - item.getSent()).abs();
        //see if it was sent
        boolean sent = item.getReceived() - item.getSent() < 0;

        //calculated and formatted amount for isoSymbol
        String amountWithFee = BRCurrency.getFormattedCurrencyString(getActivity(), iso, BRExchange.getAmountFromLitoshis(getActivity(), iso, txAmount.subtract(new BigDecimal(opsAmount))));
        String amount = BRCurrency.getFormattedCurrencyString(getActivity(), iso, BRExchange.getAmountFromLitoshis(getActivity(), iso, item.getFee() == -1 ? txAmount.subtract(new BigDecimal(opsAmount)) : txAmount.subtract(new BigDecimal(item.getFee())).subtract(new BigDecimal(opsAmount))));
        //large sent (Sent $24.32 ....)
        Spannable largeDescriptionString = sent ? new SpannableString(String.format(getString(R.string.TransactionDetails_sent), amountWithFee)) : new SpannableString(String.format(getString(R.string.TransactionDetails_received), amount));
        String startingBalance = BRCurrency.getFormattedCurrencyString(getActivity(), iso, BRExchange.getAmountFromLitoshis(getActivity(), iso, new BigDecimal(sent ? item.getBalanceAfterTx() + txAmount.longValue() : item.getBalanceAfterTx() - txAmount.longValue())));
        String endingBalance = BRCurrency.getFormattedCurrencyString(getActivity(), iso, BRExchange.getAmountFromLitoshis(getActivity(), iso, new BigDecimal(item.getBalanceAfterTx())));
        String commentString = item.metaData == null || item.metaData.comment == null ? "" : item.metaData.comment;
        String sb = String.format(getString(R.string.Transaction_starting), startingBalance);
        String eb = String.format(getString(R.string.Transaction_ending), endingBalance);

        //Target sent address
        String sendAddress;
        Set<String> outputAddressSet = new HashSet<String>(Arrays.asList(item.getTo()));
        final String opsString = Utils.fetchPartnerKey(getActivity(), PartnerNames.OPSALL);
        List<String> opsList = new ArrayList<String>(Arrays.asList(opsString.split(",")));
        Set<String> opsSet = new HashSet<>();
        opsSet.addAll(opsList);
        List<String> outputAddresses = outputAddressSet.stream().filter(element -> !opsSet.contains(element)).collect(Collectors.toList());
        List<String> filteredAddress = outputAddresses.stream().filter(Objects::nonNull).collect(Collectors.toList());

        //Filter method
        if (filteredAddress.stream().findFirst().isPresent()) {
            sendAddress = filteredAddress.stream().findFirst().get();
        } else {
            sendAddress = "ERROR-ADDRESS";
        }

        String toFrom = sent ? String.format(getString(R.string.TransactionDetails_to), sendAddress) : String.format(getString(R.string.TransactionDetails_from), sendAddress);

        mTxHash.setText(item.getTxHashHexReversed());
        mTxHash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mTxtHashCopy = mTxHash.getText().toString();

                // Get the ClipboardManager
                ClipboardManager clipboard = (ClipboardManager) requireActivity().getSystemService(Context.CLIPBOARD_SERVICE);

                // Create a ClipData object with the text
                ClipData clip = ClipData.newPlainText("Copied Text", mTxtHashCopy);

                // Set the ClipData to the clipboard
                clipboard.setPrimaryClip(clip);
            }
        });

        mTxHashLink.setOnClickListener(view -> {
            close();
            String txUrl = BRConstants.BLOCK_EXPLORER_BASE_URL + item.getTxHashHexReversed();
            Timber.d("timber: txUrl = %s", txUrl);
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(txUrl));
            startActivity(browserIntent);
            getActivity().overridePendingTransition(R.anim.enter_from_bottom, R.anim.empty_300);
        });

        int level = getLevel(item);

        boolean availableForSpend = false;
        String percentage = "";
        switch (level) {
            case 0:
                percentage = "0%";
                break;
            case 1:
                percentage = "20%";
                break;
            case 2:
                percentage = "40%";
                availableForSpend = true;
                break;
            case 3:
                percentage = "60%";
                availableForSpend = true;
                break;
            case 4:
                percentage = "80%";
                availableForSpend = true;
                break;
            case 5:
                percentage = "100%";
                availableForSpend = true;
                break;
        }

        boolean removeView = sent || !availableForSpend;
        Timber.d("timber: fillTexts: removeView : %s", removeView);
        if (!removeView) {
            mAvailableSpend.setText(getString(R.string.Transaction_available));
        } else {
            mAvailableSpend.setText("");
            signalLayout.removeView(mAvailableSpend);
        }

        if (level == 6) {
            mConfirmationText.setText(getString(R.string.Transaction_complete));
        } else {
            mConfirmationText.setText(String.format("%s", percentage));
        }

        if (!item.isValid())
            mConfirmationText.setText(getString(R.string.Transaction_invalid));

        mToFromBottom.setText(sent ? getString(R.string.TransactionDirection_to) : getString(R.string.TransactionDirection_address));
        mDateText.setText(getFormattedDate(item.getTimeStamp()));
        mLargeDescriptionText.setText(TextUtils.concat(largeDescriptionString));
        mSubHeader.setText(toFrom);
        mCommentText.setText(commentString);
        mAddressText.setText(sendAddress);
    }

    private long getOpsAmount() {
        long opsAmount = 0;

        if (item == null || item.getOutAmounts() == null || item.getOutAmounts().length != 3) {
            return opsAmount;
        }

        long[] outAmounts = item != null ? item.getOutAmounts() : new long[0];
        for (long value : outAmounts) {
            if (value < opsAmount) {
                opsAmount = value;
            }
        }

        return opsAmount;
    }

    private int getLevel(TxItem item) {
        int blockHeight = item.getBlockHeight();
        int confirms = blockHeight == Integer.MAX_VALUE ? 0 : BRSharedPrefs.getLastBlockHeight(getContext()) - blockHeight + 1;
        int level;
        if (confirms <= 0) {
            int relayCount = BRPeerManager.getRelayCount(item.getTxHash());
            if (relayCount <= 0)
                level = 0;
            else if (relayCount == 1)
                level = 1;
            else
                level = 2;
        } else {
            if (confirms == 1)
                level = 3;
            else if (confirms == 2)
                level = 4;
            else if (confirms == 3)
                level = 5;
            else
                level = 6;
        }
        return level;
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
        oldComment = mCommentText.getText().toString();
    }

    @Override
    public void onPause() {
        String comment = mCommentText.getText().toString();
        final FragmentActivity app = getActivity();
        if (!comment.equals(oldComment)) {
            final TxMetaData md = new TxMetaData();
            md.comment = comment;
            BRExecutor.getInstance().forLightWeightBackgroundTasks().execute(new Runnable() {
                @Override
                public void run() {
                    KVStoreManager.getInstance().putTxMetaData(app, md, item.getTxHash());
                    TxManager.getInstance().updateTxList(app);
                }
            });
        }
        oldComment = null;
        Utils.hideKeyboard(app);
        super.onPause();
    }

    public static FragmentTransactionItem newInstance(TxItem item) {
        FragmentTransactionItem f = new FragmentTransactionItem();

        Bundle args = new Bundle();
        args.putSerializable(ARG_ITEM, item);
        f.setArguments(args);

        return f;
    }

    private String getFormattedDate(long timeStamp) {

        Date currentLocalTime = new Date(timeStamp == 0 ? System.currentTimeMillis() : timeStamp * 1000);

        SimpleDateFormat date1 = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
        SimpleDateFormat date2 = new SimpleDateFormat("HH:mm a", Locale.getDefault());

        String str1 = date1.format(currentLocalTime);
        String str2 = date2.format(currentLocalTime);

        return str1 + " " + String.format(getString(R.string.TransactionDetails_from), str2);
    }

    private String getShortAddress(String sendAddress) {
        String p1 = sendAddress.substring(0, 5);
        String p2 = sendAddress.substring(sendAddress.length() - 5, sendAddress.length());
        return p1 + "..." + p2;
    }

    private void close() {
        Fragment parentFragment = getParentFragment();
        if (parentFragment instanceof FragmentTransactionDetails) {
            ((FragmentTransactionDetails) parentFragment).close();
        }
    }
}