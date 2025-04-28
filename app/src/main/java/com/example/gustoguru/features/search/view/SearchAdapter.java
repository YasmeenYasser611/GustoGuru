package com.example.gustoguru.features.search.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.gustoguru.R;
import com.example.gustoguru.model.pojo.FilteredMeal;

import java.util.ArrayList;
import java.util.List;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> {
    private List<FilteredMeal> meals;
    private final Context context;
    private final OnMealClickListener mealClickListener;


    public SearchAdapter(Context context, List<FilteredMeal> meals,
                         OnMealClickListener mealClickListener) {
        this.context = context;
        this.meals = new ArrayList<>(meals);
        this.mealClickListener = mealClickListener;

    }

    public void updateMeals(List<FilteredMeal> meals) {
        this.meals = new ArrayList<>(meals);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_favorite_meal, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FilteredMeal meal = meals.get(position);

        holder.tvMealName.setText(meal.getStrMeal());
        Glide.with(context)
                .load(meal.getStrMealThumb())
                .into(holder.ivMeal);

        holder.itemView.setOnClickListener(v -> mealClickListener.onClick(meal));



    }

    @Override
    public int getItemCount() {
        return meals.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivMeal;
        TextView tvMealName;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivMeal = itemView.findViewById(R.id.iv_meal);
            tvMealName = itemView.findViewById(R.id.tv_meal_name);

        }
    }

    public interface OnMealClickListener {
        void onClick(FilteredMeal meal);
    }

    public interface OnFavoriteClickListener {
        void onClick(FilteredMeal meal, int position);
    }
}