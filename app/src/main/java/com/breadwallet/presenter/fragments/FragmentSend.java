package com.breadwallet.presenter.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.OvershootInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.breadwallet.R;
import com.breadwallet.presenter.customviews.BRDialogView;
import com.breadwallet.presenter.customviews.BRSoftKeyboard;
import com.breadwallet.presenter.entities.PaymentItem;
import com.breadwallet.presenter.entities.RequestObject;
import com.breadwallet.tools.animation.BRAnimator;
import com.breadwallet.tools.animation.BreadDialog;
import com.breadwallet.tools.animation.SpringAnimator;
import com.breadwallet.tools.manager.BRClipboardManager;
import com.breadwallet.tools.security.BitcoinUrlHandler;
import com.breadwallet.tools.security.TransactionManager;
import com.breadwallet.tools.sqlite.CurrencyDataSource;
import com.breadwallet.tools.util.BRExchange;
import com.breadwallet.tools.util.BRCurrency;
import com.breadwallet.tools.util.Utils;
import com.breadwallet.wallet.BRWalletManager;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static com.breadwallet.tools.security.BitcoinUrlHandler.getRequestFromString;


/**
 * BreadWallet
 * <p>
 * Created by Mihail Gutan <mihail@breadwallet.com> on 6/29/15.
 * Copyright (c) 2016 breadwallet LLC
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

public class FragmentSend extends Fragment {
    private static final String TAG = FragmentSend.class.getName();
    public LinearLayout backgroundLayout;
    public ConstraintLayout signalLayout;
    public static final int ANIMATION_DURATION = 300;
    private BRSoftKeyboard keyboard;
    private EditText addressEdit;
    private Button scan;
    private Button paste;
    private Button send;
    private Spinner spinner;
    private EditText commentEdit;
    private StringBuilder amountBuilder;
    private TextView isoText;
    private EditText amountEdit;
    private TextView balanceText;
    private long curBalance;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_send, container, false);
        backgroundLayout = (LinearLayout) rootView.findViewById(R.id.background_layout);
        signalLayout = (ConstraintLayout) rootView.findViewById(R.id.signal_layout);
        keyboard = (BRSoftKeyboard) rootView.findViewById(R.id.keyboard);
        keyboard.setBRButtonBackgroundColor(R.color.white);
        keyboard.setBRKeyboardColor(R.color.white);
        isoText = (TextView) rootView.findViewById(R.id.iso_text);
        addressEdit = (EditText) rootView.findViewById(R.id.address_edit);
        scan = (Button) rootView.findViewById(R.id.share_text);
        paste = (Button) rootView.findViewById(R.id.paste_button);
        send = (Button) rootView.findViewById(R.id.send_button);
        commentEdit = (EditText) rootView.findViewById(R.id.comment_edit);
        spinner = (Spinner) rootView.findViewById(R.id.cur_spinner);
        amountEdit = (EditText) rootView.findViewById(R.id.amount_edit);
        balanceText = (TextView) rootView.findViewById(R.id.balance_text);
        setListeners();
        amountBuilder = new StringBuilder(0);

        return rootView;
    }

    private void setListeners() {
        long start = System.currentTimeMillis();
        paste.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!BRAnimator.isClickAllowed()) return;
                SpringAnimator.showAnimation(v);
                String bitcoinUrl = BRClipboardManager.getClipboard(getActivity());
                if (Utils.isNullOrEmpty(bitcoinUrl)) {
                    showClipboardError();
                    return;
                }
                String address = null;

                RequestObject obj = getRequestFromString(bitcoinUrl);

                if (obj == null || obj.address == null) {
                    showClipboardError();
                    return;
                }
                address = obj.address;
                BRWalletManager wm = BRWalletManager.getInstance();

                if (wm.isValidBitcoinPrivateKey(address) || wm.isValidBitcoinBIP38Key(address)) {
//                        wm.confirmSweep(getActivity(), address);
//                        addressEdit.setText("");
                    return;
                }

                if (BRWalletManager.validateAddress(address)) {
                    if (wm.addressContainedInWallet(address)) {

                        BreadDialog.showCustomDialog(getActivity(), "Address contained", getResources().getString(R.string.address_already_in_your_wallet), "close", null, new BRDialogView.BROnClickListener() {
                            @Override
                            public void onClick(BRDialogView brDialogView) {
                                brDialogView.dismiss();
                            }
                        }, null, null, 0);
                        BRClipboardManager.putClipboard(getActivity(), "");
                    } else if (wm.addressIsUsed(address)) {
                        final String finalAddress = address;
                        BreadDialog.showCustomDialog(getActivity(), "Address used", getResources().getString(R.string.address_already_used), "Ignore", "Cancel", new BRDialogView.BROnClickListener() {
                            @Override
                            public void onClick(BRDialogView brDialogView) {
                                brDialogView.dismiss();
                                addressEdit.setText(finalAddress);
                            }
                        }, new BRDialogView.BROnClickListener() {
                            @Override
                            public void onClick(BRDialogView brDialogView) {
                                brDialogView.dismiss();
                            }
                        }, null, 0);

                    } else {
                        addressEdit.setText(address);
                    }
                } else {
                    showClipboardError();
                }

            }
        });

        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!BRAnimator.isClickAllowed()) return;
                SpringAnimator.showAnimation(v);
                BRAnimator.openCamera(getActivity());

            }
        });
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //not allowed now
                if (!BRAnimator.isClickAllowed()) {
                    return;
                }

                SpringAnimator.showAnimation(v);
                boolean allFilled = true;
                String address = addressEdit.getText().toString();
                String amountStr = amountEdit.getText().toString();
                String iso = (String) spinner.getSelectedItem();

                //get amount in satoshis from any isos
                BigDecimal bigAmount = new BigDecimal(Utils.isNullOrEmpty(amountStr) ? "0" : amountStr);
                BigDecimal satoshiAmount = BRExchange.getSatoshisFromAmount(getActivity(), iso, bigAmount);
//                long amount = BRExchange.getAmountFromSatoshis(getActivity(), iso, satoshiAmount).longValue();

                if (address.isEmpty()) {
                    allFilled = false;
                    SpringAnimator.failShakeAnimation(getActivity(), addressEdit);
                }
                if (amountStr.isEmpty()) {
                    allFilled = false;
                    SpringAnimator.failShakeAnimation(getActivity(), amountEdit);
                }
                if (satoshiAmount.longValue() > BRWalletManager.getInstance().getBalance(getActivity())) {
                    SpringAnimator.failShakeAnimation(getActivity(), balanceText);
                }

                if (allFilled)
                    TransactionManager.getInstance().sendTransaction(getContext(), new PaymentItem(new String[]{address}, satoshiAmount.longValue(), null, false));
            }
        });

        backgroundLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!BRAnimator.isClickAllowed()) return;
                getActivity().onBackPressed();
            }
        });

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String item = parent.getItemAtPosition(position).toString();
                curBalance = BRWalletManager.getInstance().getBalance(getActivity());
                Log.e(TAG, "onItemSelected: " + item);
                isoText.setText(BRCurrency.getSymbolByIso(getActivity(),item));
                SpringAnimator.showAnimation(isoText);
                updateText();

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                isoText.setText(BRCurrency.getSymbolByIso(getActivity(),"BTC"));
                SpringAnimator.showAnimation(isoText);
            }
        });

        keyboard.addOnInsertListener(new BRSoftKeyboard.OnInsertListener()

        {
            @Override
            public void onClick(String key) {
                handleClick(key);
            }
        });


        final List<String> curList = new ArrayList<>();
        curList.add("BTC");
        spinner.setAdapter(new ArrayAdapter<>(getContext(), R.layout.bread_spinner_item, curList));
        Log.e(TAG, "spinner took: " + (System.currentTimeMillis() - start));
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (getActivity() == null) return;
                curList.addAll(CurrencyDataSource.getInstance(getActivity()).getAllISOs());
                if (getActivity() == null) return;
                final ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), R.layout.bread_spinner_item, curList);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        spinner.setAdapter(adapter);

                    }
                });

            }
        }).start();

    }

    private void showClipboardError() {
        BreadDialog.showCustomDialog(getActivity(), "Clipboard empty", getResources().getString(R.string.clipboard_invalid_data), "close", null, new BRDialogView.BROnClickListener() {
            @Override
            public void onClick(BRDialogView brDialogView) {
                brDialogView.dismiss();
            }
        }, null, null, 0);
        BRClipboardManager.putClipboard(getActivity(), "");
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final ViewTreeObserver observer = signalLayout.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                observer.removeGlobalOnLayoutListener(this);
                animateBackgroundDim(false);
                animateSignalSlide(false);
            }
        });

    }

    private void animateSignalSlide(final boolean reverse) {
        float translationY = signalLayout.getTranslationY();
        float signalHeight = signalLayout.getHeight();
        signalLayout.setTranslationY(reverse ? translationY : translationY + signalHeight);
        signalLayout.animate().translationY(reverse ? 2000 : translationY).setDuration(ANIMATION_DURATION).setInterpolator(new OvershootInterpolator(0.7f)).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (reverse && getActivity() != null) {
                    try {
                        getActivity().getFragmentManager().popBackStack();
                    } catch (Exception ex) {

                    }
                }

                if (!reverse) {
                    Bundle bundle = getArguments();
                    if (bundle != null && bundle.getString("url") != null)
                        setUrl(bundle.getString("url"));
                }
            }
        });

    }

    private void animateBackgroundDim(boolean reverse) {
        int transColor = reverse ? R.color.black_trans : android.R.color.transparent;
        int blackTransColor = reverse ? android.R.color.transparent : R.color.black_trans;

        ValueAnimator anim = new ValueAnimator();
        anim.setIntValues(transColor, blackTransColor);
        anim.setEvaluator(new ArgbEvaluator());
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                backgroundLayout.setBackgroundColor((Integer) valueAnimator.getAnimatedValue());
            }
        });

        anim.setDuration(ANIMATION_DURATION);
        anim.start();
    }

    @Override
    public void onStop() {
        super.onStop();
        animateBackgroundDim(true);
        animateSignalSlide(true);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }


    private void handleClick(String key) {
        if (key == null) {
            Log.e(TAG, "handleClick: key is null! ");
            return;
        }
        if (key.length() > 1) {
            Log.e(TAG, "handleClick: key is longer: " + key);
            return;
        }

        if (key.isEmpty()) {
            handleDeleteClick();
        } else if (Character.isDigit(key.charAt(0))) {
            handleDigitClick(Integer.parseInt(key));
        } else if (key.charAt(0) == '.') {
            handleSeparatorClick();
        }
    }

    private void handleDigitClick(Integer dig) {
        String currAmount = amountBuilder.toString();
        String iso = (String) spinner.getSelectedItem();
        if (new BigDecimal(currAmount.concat(String.valueOf(dig))).doubleValue()
                <= BRExchange.getMaxAmount(getActivity(), iso).doubleValue()) {
            //do not insert 0 if the balance is 0 now
            if (currAmount.equalsIgnoreCase("0")) amountBuilder = new StringBuilder("");
            if ((currAmount.contains(".") && (currAmount.length() - currAmount.indexOf(".") > BRCurrency.getMaxDecimalPlaces(iso))))
                return;
            amountBuilder.append(dig);
            updateText();
        }
    }

    private void handleSeparatorClick() {
        String currAmount = amountBuilder.toString();
        if (currAmount.contains(".") || BRCurrency.getMaxDecimalPlaces((String) spinner.getSelectedItem()) == 0)
            return;
        amountBuilder.append(".");
        updateText();
    }

    private void handleDeleteClick() {
        String currAmount = amountBuilder.toString();
        if (currAmount.length() > 0) {
            amountBuilder.deleteCharAt(currAmount.length() - 1);
            updateText();
        }

    }

    private void updateText() {
        if(getActivity() == null) return;
        String tmpAmount = amountBuilder.toString();
        amountEdit.setText(tmpAmount);
        String balanceString;
        String iso = (String) spinner.getSelectedItem();
        //Balance depending on ISO
        BigDecimal balanceForISO = BRExchange.getAmountFromSatoshis(getActivity(), iso, new BigDecimal(curBalance));
        //formattedBalance
        String formattedBalance = BRCurrency.getFormattedCurrencyString(getActivity(), iso, balanceForISO);
        if (new BigDecimal((tmpAmount.isEmpty() || tmpAmount.equalsIgnoreCase(".")) ? "0" : tmpAmount).doubleValue() > balanceForISO.doubleValue()) {
            balanceString = String.format("Insufficient funds. Try an amount below your current balance: %s", formattedBalance);
            balanceText.setTextColor(getContext().getColor(R.color.warning_color));
            amountEdit.setTextColor(getContext().getColor(R.color.warning_color));
            isoText.setTextColor(getContext().getColor(R.color.warning_color));
        } else {
            balanceString = String.format("Current Balance: %s", formattedBalance);
            balanceText.setTextColor(getContext().getColor(R.color.light_gray));
            amountEdit.setTextColor(getContext().getColor(R.color.almost_black));
            isoText.setTextColor(getContext().getColor(R.color.almost_black));
        }
        balanceText.setText(balanceString);

    }

    public void setUrl(String url) {
        RequestObject obj = BitcoinUrlHandler.getRequestFromString(url);
        if (obj == null) return;
        if (obj.address != null && addressEdit != null) {
            addressEdit.setText(obj.address.trim());
        }
        if (obj.message != null && commentEdit != null) {
            commentEdit.setText(obj.message);
        }
        if (obj.amount != null) {
            String iso = ((String) spinner.getSelectedItem());
            BigDecimal satoshiAmount = new BigDecimal(obj.amount).multiply(new BigDecimal(100000000));
            amountBuilder = new StringBuilder(BRExchange.getAmountFromSatoshis(getActivity(), iso, satoshiAmount).toPlainString());

            updateText();

        }
    }


}