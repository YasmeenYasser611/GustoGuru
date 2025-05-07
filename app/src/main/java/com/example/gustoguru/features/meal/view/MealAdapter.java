package com.example.gustoguru.features.meal.view;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MealAdapter extends RecyclerView.Adapter<MealAdapter.ViewHolder> {
    private List<Meal> meals;
    private final Context context;
    private final OnMealClickListener mealClickListener;
    private final OnFavoriteClickListener favoriteClickListener;

    public MealAdapter(Context context, List<Meal> meals,
                       OnMealClickListener mealClickListener,
                       OnFavoriteClickListener favoriteClickListener) {
        this.context = context;
        this.meals = new ArrayList<>(meals);
        this.mealClickListener = mealClickListener;
        this.favoriteClickListener = favoriteClickListener;
    }

    public void updateMeals(List<Meal> meals) {
        this.meals = new ArrayList<>(meals);
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

        // Show planned date if available
        if (meal.getPlannedDate() != null && !meal.getPlannedDate().isEmpty())
        {
            holder.tvPlannedDate.setText(meal.getPlannedDate());
            holder.tvPlannedDate.setVisibility(View.VISIBLE);
        }
        else
        {
            holder.tvPlannedDate.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> mealClickListener.onClick(meal));

        // Only show favorite button if we have a click listener for it
        if (favoriteClickListener != null) {
            int favoriteIcon = meal.isFavorite() ?
                    R.drawable.ic_favorite : R.drawable.ic_favorite_border;
            holder.btnFavorite.setImageResource(favoriteIcon);
            holder.btnFavorite.setOnClickListener(v -> {
                int currentPosition = holder.getAdapterPosition();
                if (currentPosition != RecyclerView.NO_POSITION) {
                    favoriteClickListener.onClick(meals.get(currentPosition), currentPosition);
                }
            });
            holder.btnFavorite.setVisibility(View.VISIBLE);
        } else {
            holder.btnFavorite.setVisibility(View.GONE);
        }
    }

    public void removeAt(int position) {
        meals.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, meals.size());
    }

    public void insertAt(Meal meal, int position) {
        meals.add(position, meal);
        notifyItemInserted(position);
    }

    @Override
    public int getItemCount() {
        return meals.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivMeal;
        TextView tvMealName;
        TextView tvPlannedDate; // Add this
        ImageButton btnFavorite;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivMeal = itemView.findViewById(R.id.iv_meal);
            tvMealName = itemView.findViewById(R.id.tv_meal_name);
            tvPlannedDate = itemView.findViewById(R.id.tv_planned_date); // Add this
            btnFavorite = itemView.findViewById(R.id.btn_favorite);
        }
    }


    public static class IngredientsAdapter extends RecyclerView.Adapter<IngredientsAdapter.ViewHolder> {
        private Context context;
        private List<Map.Entry<String, String>> ingredientEntries;

        public IngredientsAdapter(Context context) {
            this.context = context;
            this.ingredientEntries = new ArrayList<>();
        }

        public void setMeal(Map<String, String> ingredientMeasureMap) {
            this.ingredientEntries.clear();

            if (ingredientMeasureMap != null) {
                // Convert map entries to a list
                this.ingredientEntries.addAll(ingredientMeasureMap.entrySet());
            }

            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_ingredient, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Map.Entry<String, String> entry = ingredientEntries.get(position);
            String ingredient = entry.getKey();
            String measure = entry.getValue();

            holder.textViewIngredient.setText(ingredient);
            holder.textViewMeasure.setText(measure);

            // Load ingredient image
            Glide.with(context)
                    .load("https://www.themealdb.com/images/ingredients/" + ingredient + "-Small.png")
                    .placeholder(R.drawable.placeholder_meal)
                    .error(R.drawable.placeholder_meal)
                    .into(holder.imageViewThumb);
        }

        @Override
        public int getItemCount() {
            return ingredientEntries.size();
        }

        public static class ViewHolder extends RecyclerView.ViewHolder {
            TextView textViewIngredient, textViewMeasure;
            ImageView imageViewThumb;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                textViewIngredient = itemView.findViewById(R.id.textViewIngredient);
                textViewMeasure = itemView.findViewById(R.id.textViewMeasure);
                imageViewThumb = itemView.findViewById(R.id.imageViewThumb);
            }
        }
    }

    public interface OnMealClickListener {
        void onClick(Meal meal);
    }

    public interface OnFavoriteClickListener {
        void onClick(Meal meal, int position);
    }

}