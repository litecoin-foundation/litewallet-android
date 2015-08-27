
package com.breadwallet.presenter.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.breadwallet.R;
import com.breadwallet.presenter.BreadWalletApp;
import com.breadwallet.presenter.activities.MainActivity;
import com.breadwallet.tools.adapter.CurrencyListAdapter;
import com.breadwallet.tools.animation.SpringAnimator;
import com.breadwallet.tools.others.CurrencyManager;


/**
 * BreadWallet
 * <p/>
 * Created by Mihail on 7/14/15.
 * Copyright (c) 2015 Mihail Gutan <mihail@breadwallet.com>
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

public class FragmentCurrency extends Fragment {
    public static final String TAG = "FragmentCurrency";
    public static final String CURRENT_CURRENCY = "currentCurrency";
    public static final String POSITION = "position";
    public static final String RATE = "rate";

    public ListView currencyList;
    public MainActivity app;
    public Button currencyRefresh;
    public TextView noInternetConnection;
    public CurrencyListAdapter adapter;
    public TextView currencyItemText;
    public ProgressBar currencyProgressBar;
    public SharedPreferences settings;
    private String ISO;
    private float rate;
    int lastItemsPosition = 0;
    private SharedPreferences.Editor editor;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             final ViewGroup container, Bundle savedInstanceState) {
        // The last two arguments ensure LayoutParams are inflated
        // properly.
        final View rootView = inflater.inflate(
                R.layout.fragment_currency, container, false);

        currencyList = (ListView) rootView.findViewById(R.id.currency_list_view);
        currencyProgressBar = (ProgressBar) rootView.findViewById(R.id.currency_progress_barr);
        currencyRefresh = (Button) rootView.findViewById(R.id.currencyRefresh);
        noInternetConnection = (TextView) rootView.findViewById(R.id.noInternetConnectionText);
        app = MainActivity.app;
        settings = getActivity().getSharedPreferences(MainActivity.PREFS_NAME, 0);
        currencyList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        editor = settings.edit();
        currencyRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tryAndSetAdapter();
                SpringAnimator.showAnimation(v);
            }
        });
        currencyList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                adapter.notifyDataSetChanged();
                currencyItemText = (TextView) view.findViewById(R.id.currency_item_text);
                final String selectedCurrency = currencyItemText.getText().toString();
                ISO = selectedCurrency.substring(0, 3);
                lastItemsPosition = position;
                rate = adapter.getItem(position).rate;
                editor.putString(CURRENT_CURRENCY, ISO);
                editor.putInt(POSITION, lastItemsPosition);
                editor.putFloat(RATE, rate);
                editor.commit();
                String finalExchangeRate = CurrencyManager.getMiddleTextExchangeString(1.0, rate, ISO);
                ((BreadWalletApp) getActivity().getApplication()).setTopMidleView(
                        BreadWalletApp.SETTINGS_TEXT, finalExchangeRate);
            }

        });

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences settings = getActivity().getSharedPreferences(MainActivity.PREFS_NAME, 0);
        final String tmp = settings.getString(FragmentCurrency.CURRENT_CURRENCY, "USD");
        final float tmpRate = settings.getFloat(RATE, 0);
        String readyText = CurrencyManager.getMiddleTextExchangeString(1.0, tmpRate,
                tmp);
        ((BreadWalletApp) getActivity().getApplication()).setTopMidleView(BreadWalletApp.SETTINGS_TEXT,
                readyText);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                tryAndSetAdapter();
            }
        }, 500);

    }

    @Override
    public void onPause() {
        super.onPause();

    }

    public void tryAndSetAdapter() {
        adapter = CurrencyManager.getCurrencyAdapterIfReady();
        if (adapter.getCount() > 0) {
            currencyList.setAdapter(adapter);
            currencyRefresh.setVisibility(View.GONE);
            noInternetConnection.setVisibility(View.GONE);
            currencyProgressBar.setVisibility(View.GONE);
        } else {
            ((BreadWalletApp) app.getApplicationContext()).showCustomToast(getActivity(),
                    getResources().getString(R.string.no_internet_connection), 500, Toast.LENGTH_SHORT);
            currencyRefresh.setVisibility(View.VISIBLE);
            noInternetConnection.setVisibility(View.VISIBLE);
            currencyProgressBar.setVisibility(View.GONE);
        }
    }

}
