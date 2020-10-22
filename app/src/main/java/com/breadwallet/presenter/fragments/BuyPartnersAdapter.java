package com.breadwallet.presenter.fragments;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.breadwallet.R;
import com.breadwallet.presenter.entities.Partner;
import com.breadwallet.tools.animation.BRAnimator;

import java.util.List;

/**
 * Movo
 * Created by sadia on 2020-January-27
 * email: mosadialiou@gmail.com
 */
class BuyPartnersAdapter extends RecyclerView.Adapter<BuyPartnersAdapter.PartnerViewHolder> {

    private final LayoutInflater inflater;
    private List<Partner> partners;

    BuyPartnersAdapter(Context context, @NonNull List<Partner> partners) {
        inflater = LayoutInflater.from(context);
        this.partners = partners;
    }

    @NonNull
    @Override
    public PartnerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PartnerViewHolder(inflater.inflate(R.layout.buy_partner_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final PartnerViewHolder holder, int position) {
        Partner partner = partners.get(position);
        holder.logo.setImageResource(partner.getLogo());
        holder.title.setText(partner.getTitle());
        holder.detail.setText(partner.getDetails());

        holder.buyPartnerWrapper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currencyResId = getCurrencyResId(holder.fiatOptions.getCheckedRadioButtonId());
                String currency = v.getContext().getString(currencyResId);
                BRAnimator.showBuyFragment((FragmentActivity) v.getContext(), currency);
            }
        });
    }

    @StringRes
    private int getCurrencyResId(int checkedOption) {
        int currency = R.string.usd_currency_code;
        if (checkedOption == R.id.cad_fiat) {
            currency = R.string.cad_currency_code;
        } else if (checkedOption == R.id.eur_fiat) {
            currency = R.string.eur_currency_code;
        } else if (checkedOption == R.id.jpy_fiat) {
            currency = R.string.jpy_currency_code;
        }
        return currency;
    }

    @Override
    public int getItemCount() {
        return partners.size();
    }

    class PartnerViewHolder extends RecyclerView.ViewHolder {

        final ImageView logo;
        final TextView title;
        final TextView detail;
        final RadioGroup fiatOptions;
        final View buyPartnerWrapper;

        PartnerViewHolder(@NonNull View itemView) {
            super(itemView);

            logo = itemView.findViewById(R.id.logo);
            title = itemView.findViewById(R.id.titleLbl);
            detail = itemView.findViewById(R.id.detailLbl);
            fiatOptions = itemView.findViewById(R.id.fiat_option);
            buyPartnerWrapper = itemView.findViewById(R.id.buyPartnerWrapper);
        }
    }
}
