package com.breadwallet.presenter.fragments;

/**
 * BreadWallet
 * <p/>
 * Created by Mihail Gutan on 7/24/15.
 * Copyright (c) 2016 breadwallet llc <mihail@breadwallet.com>
 * <p/>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.inputmethodservice.Keyboard;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.breadwallet.R;
import com.breadwallet.presenter.BreadWalletApp;
import com.breadwallet.presenter.activities.IntroActivity;
import com.breadwallet.presenter.activities.MainActivity;
import com.breadwallet.presenter.entities.PaymentRequestEntity;
import com.breadwallet.tools.BRConstants;
import com.breadwallet.tools.CurrencyManager;
import com.breadwallet.tools.TypesConverter;
import com.breadwallet.tools.adapter.CustomPagerAdapter;
import com.breadwallet.tools.adapter.MiddleViewAdapter;
import com.breadwallet.tools.animation.FragmentAnimator;
import com.breadwallet.tools.animation.SpringAnimator;
import com.breadwallet.tools.security.KeyStoreManager;
import com.breadwallet.tools.security.PassCodeManager;
import com.breadwallet.tools.security.PostAuthenticationProcessor;
import com.breadwallet.wallet.BRWalletManager;

import java.security.KeyStore;
import java.util.Locale;

public class PasswordDialogFragment extends DialogFragment {

    private static final String TAG = PasswordDialogFragment.class.getName();
    private EditText passcodeEditText;
    private EditText phraseEditText;
    private Button cancel;
    private Button reset;
    private DialogFragment dialogFragment;
    private static final int AUTH_MODE_CHECK_PASS = 0;
    private static final int AUTH_MODE_NEW_PASS = 1;
    private static final int AUTH_MODE_CONFIRM_PASS = 2;
    private int currentMode = AUTH_MODE_CHECK_PASS;
    private String tempPassToChange;
    private boolean firstTime = false; // if false then change;
    private boolean verifyOnly = false;
    private TextView title;
    private TextView digit_1;
    private TextView digit_2;
    private TextView digit_3;
    private TextView digit_4;
    private PaymentRequestEntity request;
    private TextView info;
    private TextWatcher textWatcher;
    private String prevPass;

    private int mode = -1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_TITLE, 0);

    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_password_dialog, container);
        dialogFragment = this;
        passcodeEditText = (EditText) view.findViewById(R.id.edit_passcode);
        phraseEditText = (EditText) view.findViewById(R.id.edit_phrase);
        cancel = (Button) view.findViewById(R.id.button_password_cancel);
        reset = (Button) view.findViewById(R.id.button_password_reset);
        title = (TextView) view.findViewById(R.id.passcode_dialog_title);
        info = (TextView) view.findViewById(R.id.password_info_text);

        digit_1 = (TextView) view.findViewById(R.id.passcode_digit1);
        digit_2 = (TextView) view.findViewById(R.id.passcode_digit2);
        digit_3 = (TextView) view.findViewById(R.id.passcode_digit3);
        digit_4 = (TextView) view.findViewById(R.id.passcode_digit4);
        prevPass = "";

        clearDigits();

        final InputMethodManager keyboard = (InputMethodManager) getActivity().
                getSystemService(Context.INPUT_METHOD_SERVICE);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().cancel();
                passcodeEditText.setText("");
                keyboard.hideSoftInputFromWindow(cancel.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
            }
        });

        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (phraseEditText.getVisibility() == View.GONE) {
                    phraseEditText.setVisibility(View.VISIBLE);
                    title.setText(R.string.recovery_title);
                    info.setVisibility(View.GONE);
                } else {
                    if (!phraseEditText.getText().toString().isEmpty() && KeyStoreManager.phraseIsValid(phraseEditText.getText().toString(), getActivity())) {
                        KeyStoreManager.putFailCount(0, getActivity());
                        KeyStoreManager.putFailTimeStamp(0, getActivity());
                        getDialog().dismiss();
                    } else {
                        final String tmpTitle = title.getText().toString();
                        title.setText(R.string.phrase_no_match);
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                title.setText(tmpTitle);
                            }
                        }, 3000);
                    }
                }

            }
        });

        int failCount = KeyStoreManager.getFailCount(getActivity());
        SharedPreferences prefs = getActivity().getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE);

        long secureTime = prefs.getLong(BRConstants.SECURE_TIME_PREFS, 0);
        long failTimestamp = KeyStoreManager.getFailTimeStampt(getActivity());
        if (secureTime == 0) secureTime = System.currentTimeMillis() / 1000;

        updateInfoText();

        if (secureTime < failTimestamp + Math.pow(6, failCount - 3) * 60.0) {
            setWalletDisabled();
            return view;
        }

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                keyboard.showSoftInput(passcodeEditText, 0);
            }
        });

        title.setText(getResources().getString(R.string.enter_old_passcode));
        getDialog().getWindow().setBackgroundDrawableResource(R.drawable.rounded_dialog);

        if (firstTime) {
            cancel.setClickable(false);
            cancel.setVisibility(View.GONE);
            title.setText(R.string.chose_a_new_passcode);
            currentMode = AUTH_MODE_NEW_PASS;
        }
        if (verifyOnly) {
            title.setText(R.string.enter_passcode);
        }

        textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                addRemoveDigit(s.length());
                if (s.length() == 4) validatePassCode(s);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        };
        passcodeEditText.addTextChangedListener(textWatcher);

        getDialog().setCanceledOnTouchOutside(false);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        passcodeEditText.setText("");
        final Activity app = getActivity();
        if (app != null) {
            passcodeEditText.post(
                    new Runnable() {
                        public void run() {
                            InputMethodManager inputMethodManager = (InputMethodManager) app.getSystemService(Context.INPUT_METHOD_SERVICE);
                            inputMethodManager.toggleSoftInputFromWindow(passcodeEditText.getApplicationWindowToken(), InputMethodManager.SHOW_FORCED, 0);
                            passcodeEditText.requestFocus();
                        }
                    });
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        InputMethodManager keyboard = (InputMethodManager) getActivity().
                getSystemService(Context.INPUT_METHOD_SERVICE);
        EditText editText = CustomPagerAdapter.adapter.
                mainFragment.addressEditText;
        if (CustomPagerAdapter.adapter != null && editText != null)
            keyboard.hideSoftInputFromWindow(editText.getWindowToken(), 0);
//        BreadWalletApp.canceled = true;
    }

    public void setFirstTimeTrue() {
        firstTime = true;
        verifyOnly = false;
    }

    public void setVerifyOnlyTrue() {
        verifyOnly = true;
        firstTime = false;
    }

    private boolean validatePassCode(CharSequence s) {
        PassCodeManager passCodeManager = PassCodeManager.getInstance();

        //Set and confirm the new passcode
        if (firstTime) {
            switch (currentMode) {
                case AUTH_MODE_NEW_PASS:
                    tempPassToChange = s.toString();
                    currentMode = AUTH_MODE_CONFIRM_PASS;
                    title.setText(getResources().getString(R.string.verify_passcode));
                    passcodeEditText.setText("");
                    break;
                case AUTH_MODE_CONFIRM_PASS:
                    String passToCheck = s.toString();
                    if (passToCheck.equals(tempPassToChange)) {
                        passCodeManager.setPassCode(tempPassToChange, getActivity());
                        tempPassToChange = "";
                        try {
                            getDialog().dismiss();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        String tmp = CurrencyManager.getInstance(getActivity()).getCurrentBalanceText();
                        MiddleViewAdapter.resetMiddleView(getActivity(), tmp);
                        ((BreadWalletApp) getActivity().getApplicationContext()).setUnlocked(true);
                        InputMethodManager keyboard = (InputMethodManager) MainActivity.app.
                                getSystemService(Context.INPUT_METHOD_SERVICE);
                        keyboard.hideSoftInputFromWindow(cancel.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
                        currentMode = AUTH_MODE_CHECK_PASS;
                    } else {
                        SpringAnimator.showAnimation(dialogFragment.getView());
                        passcodeEditText.setText("");
                        currentMode = AUTH_MODE_NEW_PASS;
                        title.setText(getResources().getString(R.string.chose_a_new_passcode));
                    }
                    break;
            }
            // verify the passcode
        } else if (verifyOnly) {
            //assume the passcode is wrong all the time
            if (!prevPass.equals(s.toString())) {
                KeyStoreManager.putFailCount(KeyStoreManager.getFailCount(getActivity()) + 1, getActivity());
            }

            prevPass = s.toString();
            if (KeyStoreManager.getFailCount(getActivity()) >= 3) setWalletDisabled();
            if (passCodeManager.checkAuth(s.toString(), getActivity())) {
                //reset the passcode after successful attempt
                KeyStoreManager.putFailCount(0, getActivity());
//                BreadWalletApp.canceled = true;
                getDialog().cancel();
                long totalSpent = BRWalletManager.getInstance(getActivity()).getTotalSent();
                long spendLimit = totalSpent + PassCodeManager.getInstance().getLimit(getActivity()) + (request == null ? 0 : request.amount);
                KeyStoreManager.putSpendLimit(spendLimit, getActivity());
                Log.e(TAG, "Setting the new limit: " + spendLimit + ", totalSpent was: " + totalSpent);

                ((BreadWalletApp) getActivity().getApplicationContext()).setUnlocked(true);
                FragmentSettingsAll.refreshUI(getActivity());
                MiddleViewAdapter.resetMiddleView(getActivity(), null);
                ((BreadWalletApp) getActivity().getApplicationContext()).allowKeyStoreAccessForSeconds();
                getDialog().dismiss();
                passcodeEditText.setText("");
                Log.e(TAG, "mode: " + mode + " request: " + request);
                if (mode == BRConstants.AUTH_FOR_PHRASE) {
                    FragmentAnimator.animateSlideToLeft((MainActivity) getActivity(), new FragmentRecoveryPhrase(), new FragmentSettings());
                } else if (mode == BRConstants.AUTH_FOR_LIMIT) {
                    FragmentAnimator.animateSlideToLeft((MainActivity) getActivity(), new FragmentSpendLimit(), new FragmentSettings());
                } else if (mode == BRConstants.AUTH_FOR_PAY && request != null) {
                    BRWalletManager walletManager = BRWalletManager.getInstance(getActivity());
                    String seed = KeyStoreManager.getKeyStorePhrase(getActivity(), BRConstants.PAY_REQUEST_CODE);
                    if (seed != null && !seed.isEmpty()) {
                        boolean success;
                        if (request.serializedTx != null) {
                            success = walletManager.publishSerializedTransaction(request.serializedTx, seed);
                        } else {
                            success = walletManager.pay(request.addresses[0], (request.amount), seed);
                        }
                        if (!success) {
                            ((BreadWalletApp) getActivity().getApplication()).showCustomToast(getActivity(),
                                    "Failed to send", MainActivity.screenParametersPoint.y / 2, Toast.LENGTH_LONG, 0);
                            return false;
                        }
                    } else {
                        return false;
                    }
                    seed = null;
                    final MediaPlayer mp = MediaPlayer.create(getActivity(), R.raw.coinflip);
                    mp.start();
                    FragmentAnimator.hideScanResultFragment();
                }
            } else {
                SpringAnimator.showAnimation(dialogFragment.getView());
                passcodeEditText.setText("");
            }

        } else {
            switch (currentMode) {
                case AUTH_MODE_CHECK_PASS:
                    if (passCodeManager.checkAuth(s.toString(), getActivity())) {
                        currentMode = AUTH_MODE_NEW_PASS;
                        title.setText(getResources().getString(R.string.chose_a_new_passcode));
                        passcodeEditText.setText("");
                    } else {
                        SpringAnimator.showAnimation(dialogFragment.getView());
                        passcodeEditText.setText("");
                    }
                    break;
                case AUTH_MODE_NEW_PASS:
                    if (s.length() > 3 && s.length() < 12) {
                        tempPassToChange = s.toString();
                        currentMode = AUTH_MODE_CONFIRM_PASS;
                        title.setText(getResources().getString(R.string.verify_passcode));
                        passcodeEditText.setText("");
                    } else {
                        SpringAnimator.showAnimation(dialogFragment.getView());
                        passcodeEditText.setText("");
                    }
                    break;
                case AUTH_MODE_CONFIRM_PASS:
                    String passToCheck = s.toString();
                    if (passToCheck.equals(tempPassToChange)) {
                        passCodeManager.setPassCode(tempPassToChange, getActivity());
                        tempPassToChange = "";
                        getDialog().cancel();
                        String tmp = CurrencyManager.getInstance(getActivity()).getCurrentBalanceText();
                        MiddleViewAdapter.resetMiddleView(getActivity(), tmp);
                        ((BreadWalletApp) getActivity().getApplicationContext()).setUnlocked(true);
                        InputMethodManager keyboard = (InputMethodManager) MainActivity.app.
                                getSystemService(Context.INPUT_METHOD_SERVICE);
                        keyboard.hideSoftInputFromWindow(cancel.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
                        currentMode = AUTH_MODE_CHECK_PASS;
                    } else {
                        SpringAnimator.showAnimation(dialogFragment.getView());
                        passcodeEditText.setText("");
                        currentMode = AUTH_MODE_NEW_PASS;
                        title.setText(getResources().getString(R.string.chose_a_new_passcode));
                    }
                    break;
            }
        }
        return false;
    }

    private void updateInfoText() {
        int failCount = KeyStoreManager.getFailCount(getActivity());
        int attemptsRemaining = 8 - failCount;

        if (attemptsRemaining <= 0) {
            BRWalletManager m = BRWalletManager.getInstance(getActivity());
            m.wipeKeyStore();
            m.wipeWalletButKeystore(getActivity());
            startIntroActivity();
            FragmentAnimator.resetFragmentAnimator();
        }
        if (failCount >= 3) {
            info.setVisibility(View.VISIBLE);
            info.setText(String.format(Locale.getDefault(), "%d attempts remaining", attemptsRemaining < 0 ? 0 : attemptsRemaining));
        }
    }

    private void setWalletDisabled() {
        int failCount = KeyStoreManager.getFailCount(getActivity());
        SharedPreferences prefs = getActivity().getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE);

        long secureTime = prefs.getLong(BRConstants.SECURE_TIME_PREFS, 0);
        long failTimestamp = KeyStoreManager.getFailTimeStampt(getActivity());
        double waitTime = (failTimestamp + Math.pow(6, failCount - 3) * 60.0 - secureTime) / 60.0;
        title.setText(R.string.wallet_disabled);
        passcodeEditText.setVisibility(View.GONE);
        info.setVisibility(View.VISIBLE);
        info.setText(String.format(getString(R.string.try_again), (int) waitTime));
        digit_1.setVisibility(View.GONE);
        digit_2.setVisibility(View.GONE);
        digit_3.setVisibility(View.GONE);
        digit_4.setVisibility(View.GONE);
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        passcodeEditText.removeTextChangedListener(textWatcher);
        reset.setVisibility(View.VISIBLE);

    }

    public void setPaymentRequestEntity(PaymentRequestEntity requestEntity) {
        request = requestEntity;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    private void addRemoveDigit(int digits) {
        if (digit_1 == null) return;
        digit_1.setText(digits >= 1 ? "\u2022" : "-");
        digit_2.setText(digits >= 2 ? "\u2022" : "-");
        digit_3.setText(digits >= 3 ? "\u2022" : "-");
        digit_4.setText(digits >= 4 ? "\u2022" : "-");

    }

    private void clearDigits() {
        if (digit_1 == null) return;
        digit_1.setText("-");
        digit_2.setText("-");
        digit_3.setText("-");
        digit_4.setText("-");
    }

    private void startIntroActivity() {

        Intent intent;
        intent = new Intent(getActivity(), IntroActivity.class);
        startActivity(intent);
        if (!getActivity().isDestroyed()) {
            getActivity().finish();
        }
    }
}