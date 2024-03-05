package com.breadwallet.tools.adapter;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.breadwallet.R;
import com.breadwallet.entities.CountryLang;

import java.util.List;

public class CountryLanguageAdapter extends RecyclerView.Adapter<CountryLanguageAdapter.ViewHolder> {

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView txtLang;

        public ViewHolder(View itemView) {
            super(itemView);

            txtLang = itemView.findViewById(R.id.text_language_intro);
        }
    }

    private List<CountryLang> mCountryLang;

    public CountryLanguageAdapter(List<CountryLang> countryAudios) {
        mCountryLang = countryAudios;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater layoutInflater = LayoutInflater.from(context);

        View languageView = layoutInflater.inflate(R.layout.language_list, parent, false);

        ViewHolder viewHolder = new ViewHolder(languageView);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CountryLang countryLang = mCountryLang.get(position);

        TextView textLanguage = holder.txtLang;
        textLanguage.setText(countryLang.getLangQuestion());

    }

    @Override
    public int getItemCount() {
        return mCountryLang.size();
    }
}