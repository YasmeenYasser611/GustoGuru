package com.example.gustoguru.features.weekly_planner.view;

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

public class DailyMealsAdapter extends RecyclerView.Adapter<DailyMealsAdapter.MealViewHolder> {
    private List<Meal> meals;
    private final OnMealClickListener mealClickListener;
    private final OnRemoveClickListener removeClickListener;

    public DailyMealsAdapter(List<Meal> meals,
                             OnMealClickListener mealClickListener,
                             OnRemoveClickListener removeClickListener) {
        this.meals = new ArrayList<>(meals);
        this.mealClickListener = mealClickListener;
        this.removeClickListener = removeClickListener;
    }

    public void updateMeals(List<Meal> newMeals) {
        this.meals = new ArrayList<>(newMeals);
        notifyDataSetChanged();
    }

    public void removeAt(int position) {
        meals.remove(position);
        notifyItemRemoved(position);
    }

    public void insertAt(Meal meal, int position) {
        meals.add(position, meal);
        notifyItemInserted(position);
    }

    @NonNull
    @Override
    public MealViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_daily_meal, parent, false);
        return new MealViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MealViewHolder holder, int position) {
        Meal meal = meals.get(position);
        holder.bind(meal, mealClickListener, removeClickListener, position);
    }

    @Override
    public int getItemCount() {
        return meals.size();
    }

    static class MealViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivMeal;
        private final TextView tvMealName;
        private final TextView tvMealTime;
        private final ImageButton btnRemove;

        public MealViewHolder(@NonNull View itemView) {
            super(itemView);
            ivMeal = itemView.findViewById(R.id.iv_meal);
            tvMealName = itemView.findViewById(R.id.tv_meal_name);
            tvMealTime = itemView.findViewById(R.id.tv_meal_time);
            btnRemove = itemView.findViewById(R.id.btn_remove);
        }

        public void bind(Meal meal,
                         OnMealClickListener mealClickListener,
                         OnRemoveClickListener removeClickListener,
                         int position) {
            Glide.with(itemView.getContext())
                    .load(meal.getStrMealThumb())
                    .into(ivMeal);

            tvMealName.setText(meal.getStrMeal());

            // You can add time field to your Meal model
            tvMealTime.setText("Lunch");

            itemView.setOnClickListener(v -> mealClickListener.onClick(meal));
            btnRemove.setOnClickListener(v -> removeClickListener.onClick(meal, position));
        }
    }

    public interface OnMealClickListener {
        void onClick(Meal meal);
    }

    public interface OnRemoveClickListener {
        void onClick(Meal meal, int position);
    }
}