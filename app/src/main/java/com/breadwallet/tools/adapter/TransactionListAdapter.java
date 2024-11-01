package com.breadwallet.tools.adapter;
import android.app.Activity;
import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.recyclerview.widget.RecyclerView;

import com.breadwallet.R;
import com.breadwallet.presenter.entities.PartnerNames;
import com.breadwallet.presenter.entities.TxItem;
import com.breadwallet.tools.manager.BRSharedPrefs;
import com.breadwallet.tools.manager.PromptManager;
import com.breadwallet.tools.manager.TxManager;
import com.breadwallet.tools.threads.BRExecutor;
import com.breadwallet.tools.util.BRCurrency;
import com.breadwallet.tools.util.BRDateUtil;
import com.breadwallet.tools.util.BRExchange;
import com.breadwallet.tools.util.Utils;
import com.breadwallet.wallet.BRPeerManager;
import com.platform.tools.KVStoreManager;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import timber.log.Timber;

public class TransactionListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final Context mContext;
    private final int txResId;
    private final int syncingResId;
    private final int promptResId;
    private List<TxItem> backUpFeed;
    private List<TxItem> itemFeed;
    private final int txType = 0;
    private final int promptType = 1;
    private final int syncingType = 2;
    private boolean updatingReverseTxHash;
    private boolean updatingData;

    public TransactionListAdapter(Context mContext, List<TxItem> items) {
        this.txResId = R.layout.tx_item;
        this.syncingResId = R.layout.syncing_item;
        this.promptResId = R.layout.prompt_item;
        this.mContext = mContext;
        items = new ArrayList<>();
        init(items);
    }

    public void setItems(List<TxItem> items) {
        init(items);
    }

    private void init(List<TxItem> items) {
        if (items == null) items = new ArrayList<>();
        if (itemFeed == null) itemFeed = new ArrayList<>();
        if (backUpFeed == null) backUpFeed = new ArrayList<>();
        this.itemFeed = items;
        this.backUpFeed = items;
        updateTxHashes();
    }

    public void updateData() {
        if (updatingData) return;
        BRExecutor.getInstance().forLightWeightBackgroundTasks().execute(new Runnable() {
            @Override
            public void run() {
                long s = System.currentTimeMillis();
                List<TxItem> newItems = new ArrayList<>(itemFeed);
                TxItem item;
                for (int i = 0; i < newItems.size(); i++) {
                    item = newItems.get(i);
                    item.metaData = KVStoreManager.getInstance().getTxMetaData(mContext, item.getTxHash());
                    item.txReversed = Utils.reverseHex(Utils.bytesToHex(item.getTxHash()));
                }
                backUpFeed = newItems;
                Timber.d("timber: updateData: newItems: %d, took: %s", newItems.size(), System.currentTimeMillis() - s);
                updatingData = false;
            }
        });
    }

    private void updateTxHashes() {
        if (updatingReverseTxHash) return;
        updatingReverseTxHash = true;
    }

    public List<TxItem> getItems() {
        return itemFeed;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
        if (viewType == txType)
            return new TxHolder(inflater.inflate(txResId, parent, false));
        else if (viewType == promptType)
            return new PromptHolder(inflater.inflate(promptResId, parent, false));
        else if (viewType == syncingType)
            return new SyncingProgressViewHolder(inflater.inflate(syncingResId, parent, false));
        return null;
    }
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case txType:
                setTexts((TxHolder) holder, position);
                break;
            case promptType:
                setPrompt((PromptHolder) holder);
                break;
            case syncingType:
                setSyncing((SyncingProgressViewHolder) holder);
                break;
        }
    }
    @Override
    public int getItemViewType(int position) {
        if (position == 0 && TxManager.getInstance().currentPrompt == PromptManager.PromptItem.SYNCING) {
            return syncingType;
        } else if (position == 0 && TxManager.getInstance().currentPrompt != null) {
            return promptType;
        } else {
            return txType;
        }
    }

    @Override
    public int getItemCount() {
        return TxManager.getInstance().currentPrompt == null ? itemFeed.size() : itemFeed.size() + 1;
    }

    private void setTexts(final TxHolder convertView, int position) {
        TxItem item = itemFeed.get(TxManager.getInstance().currentPrompt == null ? position : position - 1);
        item.metaData = KVStoreManager.getInstance().getTxMetaData(mContext, item.getTxHash());
        String commentString = (item.metaData == null || item.metaData.comment == null) ? "" : item.metaData.comment;
        String sendAddress = "ERROR-ADDRESS";
        convertView.comment.setText(commentString);
        if (commentString.isEmpty()) {
            convertView.constraintLayout.removeView(convertView.comment);
            ConstraintSet set = new ConstraintSet();
            set.clone(convertView.constraintLayout);
            int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, mContext.getResources().getDisplayMetrics());
            set.connect(R.id.status, ConstraintSet.TOP, convertView.toFrom.getId(), ConstraintSet.BOTTOM, px);
            // Apply the changes
            set.applyTo(convertView.constraintLayout);
        } else {
            if (convertView.constraintLayout.indexOfChild(convertView.comment) == -1)
                convertView.constraintLayout.addView(convertView.comment);
            ConstraintSet set = new ConstraintSet();
            set.clone(convertView.constraintLayout);
            int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, mContext.getResources().getDisplayMetrics());
            set.connect(R.id.status, ConstraintSet.TOP, convertView.comment.getId(), ConstraintSet.BOTTOM, px);
            // Apply the changes
            set.applyTo(convertView.constraintLayout);
            convertView.comment.requestLayout();
        }

        boolean received = item.getSent() == 0;
        convertView.arrowIcon.setImageResource(received ? R.drawable.arrow_down_bold_circle : R.drawable.arrow_up_bold_circle);
        convertView.mainLayout.setBackgroundResource(getResourceByPos(position));
        convertView.sentReceived.setText(received ? mContext.getString(R.string.TransactionDetails_received, "") : mContext.getString(R.string.TransactionDetails_sent, ""));
        convertView.toFrom.setText(received ? String.format(mContext.getString(R.string.TransactionDetails_from), "") : String.format(mContext.getString(R.string.TransactionDetails_to), ""));

        Set<String> outputAddressSet = new HashSet<String>(Arrays.asList(item.getTo()));

        final String opsString = Utils.fetchPartnerKey(mContext, PartnerNames.OPSALL);
        List<String> opsList = new ArrayList<String>(Arrays.asList(opsString.split(",")));
        Set<String> opsSet = new HashSet<>();
        opsSet.addAll(opsList);
        List<String> outputAddresses = outputAddressSet.stream().filter(element -> !opsSet.contains(element)).collect(Collectors.toList());
        List<String> opsAddressList = outputAddressSet.stream().filter(element -> opsSet.contains(element)).collect(Collectors.toList());
        List<String> filteredAddress = outputAddresses.stream().filter(Objects::nonNull).collect(Collectors.toList());
        //Filter method
        if (filteredAddress.stream().findFirst().isPresent()) {
            sendAddress =  filteredAddress.stream().findFirst().get();
        } else {
            sendAddress = "ERROR-ADDRESS";
        }
        convertView.account.setText(sendAddress);

        int blockHeight = item.getBlockHeight();
        int confirms = blockHeight == Integer.MAX_VALUE ? 0 : BRSharedPrefs.getLastBlockHeight(mContext) - blockHeight + 1;

        int level = 0;
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
        boolean availableForSpend = false;
        String sentReceived = received ? "Receiving" : "Sending";
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
        if (availableForSpend && received) {
            convertView.status_2.setText(mContext.getString(R.string.Transaction_available));
        } else {
            convertView.constraintLayout.removeView(convertView.status_2);
            ConstraintSet set = new ConstraintSet();
            set.clone(convertView.constraintLayout);
            int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, mContext.getResources().getDisplayMetrics());

            set.connect(R.id.status, ConstraintSet.BOTTOM, convertView.constraintLayout.getId(), ConstraintSet.BOTTOM, px);
            // Apply the changes
            set.applyTo(convertView.constraintLayout);
        }
        if (level == 6) {
            convertView.status.setText(mContext.getString(R.string.Transaction_complete));
        } else {
            convertView.status.setText(String.format("%s - %s", sentReceived, percentage));
        }

        if (!item.isValid())
            convertView.status.setText(mContext.getString(R.string.Transaction_invalid));

        long[] outAmounts = item.getOutAmounts();
        long opsAmount = Long.MAX_VALUE;
        if (outAmounts.length == 3) {
            for (int i = 0; i < outAmounts.length; i++) {

                long value = outAmounts[i];

                if (value < opsAmount && value != 0L) {
                    opsAmount = value;
                    Timber.d("timber: outAmounts size %d opsAmount value: %d", outAmounts.length, value);
                }
            }
        }
        else {
            opsAmount = 0L;
        }

        long sentLitoshisAmount = received ? item.getReceived() : (item.getSent() - item.getReceived() -  opsAmount);
        boolean isBTCPreferred = BRSharedPrefs.getPreferredLTC(mContext);
        String iso = isBTCPreferred ? "LTC" : BRSharedPrefs.getIsoSymbol(mContext);
        convertView.amount.setText(BRCurrency.getFormattedCurrencyString(mContext, iso, BRExchange.getAmountFromLitoshis(mContext, iso, new BigDecimal(sentLitoshisAmount))));

        //if it's 0 we use the current time.
        long timeStamp = item.getTimeStamp() == 0 ? System.currentTimeMillis() : item.getTimeStamp() * 1000;
        CharSequence timeSpan = BRDateUtil.getCustomSpan(new Date(timeStamp));
        convertView.timestamp.setText(timeSpan);
    }

    private void setPrompt(final PromptHolder prompt) {
        Timber.d("timber: setPrompt: %s", TxManager.getInstance().promptInfo.title);
        if (TxManager.getInstance().promptInfo == null) {
            throw new RuntimeException("can't happen, showing prompt with null PromptInfo");
        }

        prompt.mainLayout.setOnClickListener(TxManager.getInstance().promptInfo.listener);
        prompt.mainLayout.setBackgroundResource(R.drawable.tx_rounded);
        prompt.title.setText(TxManager.getInstance().promptInfo.title);
        prompt.description.setText(TxManager.getInstance().promptInfo.description);
    }

    private void setSyncing(final SyncingProgressViewHolder syncing) {
        TxManager.getInstance().syncingProgressViewHolder = syncing;
        syncing.mainLayout.setBackgroundResource(R.drawable.tx_rounded);
    }

    private int getResourceByPos(int pos) {
        if (TxManager.getInstance().currentPrompt != null) pos--;
        if (itemFeed != null && itemFeed.size() == 1) {
            return R.drawable.tx_rounded;
        } else if (pos == 0) {
            return R.drawable.tx_rounded_up;
        } else if (itemFeed != null && pos == itemFeed.size() - 1) {
            return R.drawable.tx_rounded_down;
        } else {
            return R.drawable.tx_not_rounded;
        }
    }


    public void filterBy(String query, boolean[] switches) {
        filter(query, switches);
    }

    public void resetFilter() {
        itemFeed = backUpFeed;
        notifyDataSetChanged();
    }

    private void filter(final String query, final boolean[] switches) {
        long start = System.currentTimeMillis();
        String lowerQuery = query.toLowerCase().trim();
        if (Utils.isNullOrEmpty(lowerQuery) && !switches[0] && !switches[1] && !switches[2] && !switches[3])
            return;
        int switchesON = 0;
        for (boolean i : switches) if (i) switchesON++;

        final List<TxItem> filteredList = new ArrayList<>();
        TxItem item;
        for (int i = 0; i < backUpFeed.size(); i++) {
            item = backUpFeed.get(i);
            boolean matchesHash = item.getTxHashHexReversed() != null && item.getTxHashHexReversed().contains(lowerQuery);
            boolean matchesAddress = item.getFrom()[0].contains(lowerQuery) || item.getTo()[0].contains(lowerQuery);
            boolean matchesMemo = item.metaData != null && item.metaData.comment != null && item.metaData.comment.toLowerCase().contains(lowerQuery);
            if (matchesHash || matchesAddress || matchesMemo) {
                if (switchesON == 0) {
                    filteredList.add(item);
                } else {
                    boolean willAdd = true;
                    //filter by sent and this is received
                    if (switches[0] && (item.getSent() - item.getReceived() <= 0)) {
                        willAdd = false;
                    }
                    //filter by received and this is sent
                    if (switches[1] && (item.getSent() - item.getReceived() > 0)) {
                        willAdd = false;
                    }

                    int confirms = item.getBlockHeight() == Integer.MAX_VALUE ? 0 : BRSharedPrefs.getLastBlockHeight(mContext) - item.getBlockHeight() + 1;
                    //complete
                    if (switches[2] && confirms >= 6) {
                        willAdd = false;
                    }

                    //pending
                    if (switches[3] && confirms < 6) {
                        willAdd = false;
                    }

                    if (willAdd) filteredList.add(item);
                }

            }

        }
        itemFeed = filteredList;
        notifyDataSetChanged();

        Timber.d("timber: filter: %s took: %s", query, System.currentTimeMillis() - start);
    }

    private class TxHolder extends RecyclerView.ViewHolder {
        public RelativeLayout mainLayout;
        public ConstraintLayout constraintLayout;
        public TextView sentReceived;
        public TextView amount;
        public TextView toFrom;
        public TextView account;
        public TextView status;
        public TextView status_2;
        public TextView timestamp;
        public TextView comment;
        public ImageView arrowIcon;

        public TxHolder(View view) {
            super(view);
            mainLayout = (RelativeLayout) view.findViewById(R.id.main_layout);
            constraintLayout = (ConstraintLayout) view.findViewById(R.id.constraintLayout);
            sentReceived = (TextView) view.findViewById(R.id.sent_received);
            amount = (TextView) view.findViewById(R.id.amount);
            toFrom = (TextView) view.findViewById(R.id.to_from);
            account = (TextView) view.findViewById(R.id.account);
            status = (TextView) view.findViewById(R.id.status);
            status_2 = (TextView) view.findViewById(R.id.status_2);
            timestamp = (TextView) view.findViewById(R.id.timestamp);
            comment = (TextView) view.findViewById(R.id.comment);
            arrowIcon = (ImageView) view.findViewById(R.id.arrow_icon);
        }
    }

    public class PromptHolder extends RecyclerView.ViewHolder {
        public RelativeLayout mainLayout;
        public ConstraintLayout constraintLayout;
        public TextView title;
        public TextView description;
        public ImageButton close;

        public PromptHolder(View view) {
            super(view);
            mainLayout = (RelativeLayout) view.findViewById(R.id.main_layout);
            constraintLayout = (ConstraintLayout) view.findViewById(R.id.prompt_layout);
            title = view.findViewById(R.id.info_title);
            description = view.findViewById(R.id.info_description);
            close = (ImageButton) view.findViewById(R.id.info_close_button);
        }
    }

    public class SyncingProgressViewHolder extends RecyclerView.ViewHolder {
        public RelativeLayout mainLayout;
        public ConstraintLayout constraintLayout;
        public TextView date;
        public TextView label;
        public ProgressBar progress;
        public TextView blockheightView;

        public SyncingProgressViewHolder(View view) {
            super(view);
            mainLayout = (RelativeLayout) view.findViewById(R.id.main_layout);
            constraintLayout = (ConstraintLayout) view.findViewById(R.id.syncing_layout);
            date = view.findViewById(R.id.sync_date);
            progress = (ProgressBar) view.findViewById(R.id.sync_progress);
            label = view.findViewById(R.id.syncing_label);
        }
    }

}