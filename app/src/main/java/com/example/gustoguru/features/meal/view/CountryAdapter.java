package com.example.gustoguru.features.meal.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.gustoguru.R;

import java.util.List;

public class CountryAdapter extends RecyclerView.Adapter<CountryAdapter.ViewHolder> {
    private final List<CountryItem> countries;
    private final Context context;

    public CountryAdapter(Context context, List<CountryItem> countries) {
        this.context = context;
        this.countries = countries;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_country, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CountryItem country = countries.get(position);
        holder.tvCountryName.setText(country.getName());

        Glide.with(context)
                .load(country.getFlagUrl())
                .placeholder(R.drawable.placeholder_meal)
                .error(R.drawable.ic_close_emoji)
                .into(holder.ivCountryFlag);
    }

    @Override
    public int getItemCount() {
        return countries.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCountryName;
        ImageView ivCountryFlag;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCountryName = itemView.findViewById(R.id.tvCountryName);
            ivCountryFlag = itemView.findViewById(R.id.ivCountryFlag);
        }
    }

    public static class CountryItem {
        private final String name;
        private final String flagUrl;

        public CountryItem(String name, String flagUrl) {
            this.name = name;
            this.flagUrl = flagUrl;
        }

        public String getName() {
            return name;
        }

        public String getFlagUrl() {
            return flagUrl;
        }
    }
}