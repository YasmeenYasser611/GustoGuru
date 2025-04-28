package com.example.gustoguru.features.search.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.gustoguru.R;
import com.example.gustoguru.model.pojo.FilteredMeal;

import java.util.ArrayList;
import java.util.List;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> {
    private List<FilteredMeal> meals;
    private final Context context;
    private final OnMealClickListener mealClickListener;
    private RequestManager glide;

    public SearchAdapter(Context context, List<FilteredMeal> meals,
                         OnMealClickListener mealClickListener) {
        this.context = context;
        this.meals = new ArrayList<>(meals);
        this.mealClickListener = mealClickListener;
        this.glide = Glide.with(context);
    }

    public void updateMeals(List<FilteredMeal> meals) {
        this.meals = new ArrayList<>(meals);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_search_meal, parent, false); // Use new layout
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FilteredMeal meal = meals.get(position);

        holder.tvMealName.setText(meal.getStrMeal());
        holder.progressBar.setVisibility(View.VISIBLE);

        glide.load(meal.getStrMealThumb())
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                                Target<Drawable> target, boolean isFirstResource) {
                        holder.progressBar.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model,
                                                   Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        holder.progressBar.setVisibility(View.GONE);
                        return false;
                    }
                })
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
        ProgressBar progressBar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivMeal = itemView.findViewById(R.id.iv_meal);
            tvMealName = itemView.findViewById(R.id.tv_meal_name);
            progressBar = itemView.findViewById(R.id.progress_bar);
        }
    }

    public interface OnMealClickListener {
        void onClick(FilteredMeal meal);
    }
}