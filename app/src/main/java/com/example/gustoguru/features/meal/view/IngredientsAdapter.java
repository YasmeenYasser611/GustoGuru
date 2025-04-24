package com.example.gustoguru.features.meal.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.gustoguru.R;
import com.example.gustoguru.features.home.view.CategoryAdapter;
import com.example.gustoguru.model.pojo.Category;
import com.example.gustoguru.model.pojo.Ingredient;
import com.example.gustoguru.model.pojo.Meal;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
public class IngredientsAdapter extends RecyclerView.Adapter<IngredientsAdapter.ViewHolder> {
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