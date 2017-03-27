package com.breadwallet.tools.adapter;

import android.app.Activity;
import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.breadwallet.R;
import com.breadwallet.presenter.entities.CurrencyEntity;
import com.breadwallet.presenter.entities.TransactionListItem;
import com.breadwallet.tools.manager.SharedPreferencesManager;
import com.breadwallet.tools.sqlite.CurrencyDataSource;
import com.breadwallet.tools.util.BRCurrency;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static android.text.format.DateUtils.MINUTE_IN_MILLIS;


/**
 * BreadWallet
 * <p>
 * Created by Mihail Gutan <mihail@breadwallet.com> on 7/27/15.
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

public class TransactionListAdapter extends ArrayAdapter<TransactionListItem> {
    public static final String TAG = TransactionListAdapter.class.getName();

    private final Context mContext;
    private final int layoutResourceId;
    private List<TransactionListItem> itemFeed;

    public TransactionListAdapter(Context mContext, List<TransactionListItem> items) {
        super(mContext, R.layout.tx_list_item);
        itemFeed = items;
        if (itemFeed == null) itemFeed = new ArrayList<>();
        this.layoutResourceId = R.layout.tx_list_item;
        this.mContext = mContext;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            // inflate the layout
            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            convertView = inflater.inflate(layoutResourceId, parent, false);

            if (itemFeed.size() == 1) {
                convertView.setBackground(getContext().getDrawable(R.drawable.tx_last_item_rounded_full));
            } else {
                if (position == itemFeed.size() - 1) {
                    convertView.setBackground(getContext().getDrawable(R.drawable.tx_last_item_rounded_bottom));
                } else if(position == 0){
                    convertView.setBackground(getContext().getDrawable(R.drawable.tx_last_item_rounded_top));
                } else {
                    convertView.setBackground(getContext().getDrawable(R.drawable.tx_last_item_not_rounded));
                }
            }
        }
        setTexts(convertView, position);

        return convertView;

    }

    @Override
    public int getCount() {
        return itemFeed.size();
    }

    @Override
    public int getItemViewType(int position) {
        return IGNORE_ITEM_VIEW_TYPE;
    }

    private void setTexts(View convertView, int position) {
        TextView sentReceived = (TextView) convertView.findViewById(R.id.sent_received);
        TextView amount = (TextView) convertView.findViewById(R.id.amount);
        TextView toFrom = (TextView) convertView.findViewById(R.id.to_from);
//        TextView account = (TextView) convertView.findViewById(R.id.account);
        TextView confirmation = (TextView) convertView.findViewById(R.id.confirmation);
        TextView timestamp = (TextView) convertView.findViewById(R.id.timestamp);
//        TextView comment = (TextView) convertView.findViewById(R.id.comment);

        TransactionListItem item = itemFeed.get(position);

        boolean received = item.getSent() == 0;
        sentReceived.setText(received ? "Received" : "Sent");
        toFrom.setText(received ? "from" : "to");
        int blockHeight = item.getBlockHeight();
        int confirms = blockHeight == Integer.MAX_VALUE ? 0 : SharedPreferencesManager.getLastBlockHeight(mContext) - blockHeight + 1;
        confirmation.setText((confirms >= 6) ? "Completed" : "Waiting to be confirmed");

        boolean priceInBtc = SharedPreferencesManager.getPriceSetToBitcoin(mContext);
        long satoshisAmount = received ? item.getReceived() : (item.getSent() - item.getReceived()) * -1;
        String iso = SharedPreferencesManager.getIso(mContext);
        CurrencyEntity ent = CurrencyDataSource.getInstance(mContext).getCurrencyByIso(iso);

        if (priceInBtc || ent == null) {
            amount.setText(BRCurrency.getFormattedCurrencyString(mContext, "BTC", new BigDecimal(satoshisAmount)));
        } else {

            BigDecimal exchangeRate = new BigDecimal(ent.rate);
            String exchangeString = BRCurrency.getExchangeForAmount(exchangeRate, iso, new BigDecimal(satoshisAmount), mContext);
            amount.setText(exchangeString);
        }
        //if it's 0 we use the current time.
        long timeStamp = item.getTimeStamp() == 0 ? System.currentTimeMillis() : item.getTimeStamp() * 1000;
        CharSequence timeSpan = DateUtils.getRelativeTimeSpanString(timeStamp, System.currentTimeMillis(), MINUTE_IN_MILLIS);

        timestamp.setText(timeSpan);

    }

}