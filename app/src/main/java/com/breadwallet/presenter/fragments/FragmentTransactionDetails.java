package com.breadwallet.presenter.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.breadwallet.R;
import com.breadwallet.presenter.entities.TxItem;
import com.breadwallet.tools.adapter.TransactionPagerAdapter;
import com.breadwallet.tools.animation.BRAnimator;

import java.util.List;

import timber.log.Timber;

public class FragmentTransactionDetails extends DialogFragment {

    public TextView mTitle;
    public LinearLayout backgroundLayout;
    private ViewPager2 txViewPager;
    private TransactionPagerAdapter txPagerAdapter;
    private List<TxItem> items;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_TITLE, android.R.style.Theme_DeviceDefault_Light_NoActionBar);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // The last two arguments ensure LayoutParams are inflated
        // properly.
        View rootView = inflater.inflate(R.layout.fragment_transaction_details, container, false);
        mTitle = (TextView) rootView.findViewById(R.id.title);
        backgroundLayout = (LinearLayout) rootView.findViewById(R.id.background_layout);
        txViewPager = (ViewPager2) rootView.findViewById(R.id.tx_list_pager);
        txPagerAdapter = new TransactionPagerAdapter(getChildFragmentManager(), getLifecycle(), items);
        txViewPager.setAdapter(txPagerAdapter);
        txViewPager.setOffscreenPageLimit(5);

        int pos = getArguments().getInt("pos", 0);
        txViewPager.setCurrentItem(pos, false);

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final ViewTreeObserver observer = txViewPager.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (observer.isAlive()) {
                    observer.removeOnGlobalLayoutListener(this);
                }
                BRAnimator.animateBackgroundDim(backgroundLayout, false);
                BRAnimator.animateSignalSlide(txViewPager, false, null);
            }
        });
    }

    public void close() {
        final FragmentActivity app = getActivity();
        BRAnimator.animateBackgroundDim(backgroundLayout, true);
        BRAnimator.animateSignalSlide(txViewPager, true, () -> {
            if (app != null && !app.isFinishing())
                app.getSupportFragmentManager().popBackStack();
            else
                Timber.d("timber: onAnimationEnd: app is null");
        });
    }

    public void setItems(List<TxItem> items) {
        this.items = items;
    }
}