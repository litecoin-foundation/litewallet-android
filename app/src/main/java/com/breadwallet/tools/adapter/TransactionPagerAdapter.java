package com.breadwallet.tools.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.breadwallet.presenter.entities.TxItem;
import com.breadwallet.presenter.fragments.FragmentTransactionItem;

import java.util.List;

public class TransactionPagerAdapter extends FragmentStateAdapter {

    private static final String TAG = TransactionPagerAdapter.class.getName();
    private final List<TxItem> items;

    public TransactionPagerAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle, List<TxItem> items) {
        super(fragmentManager, lifecycle);
        this.items = items;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return FragmentTransactionItem.newInstance(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }
}
