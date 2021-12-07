package com.breadwallet.tools.adapter;

import android.app.Fragment;
import android.app.FragmentManager;
import androidx.legacy.app.FragmentPagerAdapter;

import com.breadwallet.presenter.entities.TxItem;
import com.breadwallet.presenter.fragments.FragmentTransactionItem;

import java.util.List;

public class TransactionPagerAdapter extends FragmentPagerAdapter {
    private static final String TAG = TransactionPagerAdapter.class.getName();
    private List<TxItem> items;

    public TransactionPagerAdapter(FragmentManager fm, List<TxItem> items) {
        super(fm);
        this.items = items;

    }

    public TransactionPagerAdapter(FragmentManager fm){
        super(fm);
    }

    @Override
    public Fragment getItem(int pos) {
        return FragmentTransactionItem.newInstance(items.get(pos));
    }

    @Override
    public int getCount() {
        return items == null ? 0 : items.size();
    }

}
