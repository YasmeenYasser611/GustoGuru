package com.example.gustoguru.features.favorites.view;

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
import com.example.gustoguru.model.pojo.Meal;

import java.util.List;

public class MealAdapter extends RecyclerView.Adapter<MealAdapter.ViewHolder> {
    private List<Meal> meals;
    private final Context context;
    private final OnMealClickListener mealClickListener;
    private final OnFavoriteClickListener favoriteClickListener;

    public MealAdapter(Context context, List<Meal> meals,
                       OnMealClickListener mealClickListener,
                       OnFavoriteClickListener favoriteClickListener) {
        this.context = context;
        this.meals = meals;
        this.mealClickListener = mealClickListener;
        this.favoriteClickListener = favoriteClickListener;
    }

    public void updateMeals(List<Meal> meals) {
        this.meals = meals;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_favorite_meal, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Meal meal = meals.get(position);

        holder.tvMealName.setText(meal.getStrMeal());
        Glide.with(context)
                .load(meal.getStrMealThumb())
                .into(holder.ivMeal);

        holder.itemView.setOnClickListener(v -> mealClickListener.onClick(meal));
        holder.btnFavorite.setOnClickListener(v -> {
            favoriteClickListener.onClick(meal);
            notifyItemChanged(position); // Update the icon immediately
        });

        // Set favorite icon state
        int favoriteIcon = meal.isFavorite() ?
                R.drawable.ic_favorite : R.drawable.ic_favorite_border;
        holder.btnFavorite.setImageResource(favoriteIcon);
    }

    @Override
    public int getItemCount() {
        return meals.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder
    {
        ImageView ivMeal;
        TextView tvMealName;
        ImageButton btnFavorite;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivMeal = itemView.findViewById(R.id.iv_meal);
            tvMealName = itemView.findViewById(R.id.tv_meal_name);
            btnFavorite = itemView.findViewById(R.id.btn_favorite);
        }
    }

    public interface OnMealClickListener {
        void onClick(Meal meal);
    }

    public interface OnFavoriteClickListener {
        void onClick(Meal meal);
    }
}