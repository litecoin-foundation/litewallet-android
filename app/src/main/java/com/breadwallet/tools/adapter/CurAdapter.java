package com.breadwallet.tools.adapter;

import android.app.Activity;
import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.breadwallet.R;
import com.breadwallet.presenter.customviews.BRButton;
import com.breadwallet.tools.util.BRCurrency;

import java.util.ArrayList;
import java.util.List;

import static android.widget.Adapter.IGNORE_ITEM_VIEW_TYPE;

public class CurAdapter extends RecyclerView.Adapter<CurAdapter.CustomViewHolder> {
    public static final String TAG = CurAdapter.class.getName();

    private final Context mContext;
    private final int layoutResourceId;
    private List<String> itemFeed;

    public CurAdapter(Context mContext, List<String> items) {
        itemFeed = items;
        if (itemFeed == null) itemFeed = new ArrayList<>();
        this.layoutResourceId = R.layout.spinner_item;
        this.mContext = mContext;
    }

    public String getItemAtPos(int pos) {
        return itemFeed.get(pos);
    }

    public List<String> getItems() {
        return itemFeed;
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // inflate the layout
        LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
        View convertView = inflater.inflate(layoutResourceId, parent, false);
        return new CustomViewHolder(convertView);
    }

    @Override
    public void onBindViewHolder(CustomViewHolder holder, int position) {
        setTexts(holder, position);
    }

    @Override
    public int getItemViewType(int position) {
        return IGNORE_ITEM_VIEW_TYPE;
    }

    @Override
    public int getItemCount() {
        return itemFeed.size();
    }

    private void setTexts(CustomViewHolder convertView, int position) {

        String item = itemFeed.get(position);
        convertView.button.setText(String.format("%s(%s)", item, BRCurrency.getSymbolByIso(mContext, item)));

    }

    class CustomViewHolder extends RecyclerView.ViewHolder {
        public BRButton button;

        public CustomViewHolder(View view) {
            super(view);
            button = (BRButton) view.findViewById(R.id.watch_list_layout);
        }
    }

}