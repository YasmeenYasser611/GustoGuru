package com.example.gustoguru.features.home.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.gustoguru.R;
import com.example.gustoguru.model.network.NetworkUtil;
import com.example.gustoguru.model.pojo.Ingredient;

import java.util.List;

public class IngredientAdapter extends RecyclerView.Adapter<IngredientAdapter.ViewHolder> {
    private Context context;
    private List<Ingredient> ingredients;
    private OnIngredientClickListener listener;

    public interface OnIngredientClickListener {
        void onIngredientClick(Ingredient ingredient);
    }

    public IngredientAdapter(Context context, List<Ingredient> ingredients, OnIngredientClickListener listener) {
        this.context = context;
        this.ingredients = ingredients;
        this.listener = listener;
    }

    public void setIngredients(List<Ingredient> ingredients) {
        this.ingredients = ingredients;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ingredientview, parent, false);
        return new ViewHolder(view);
    }

    @NonNull
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Ingredient ingredient = ingredients.get(position);
        holder.textViewIngredient.setText(ingredient.getStrIngredient());

        String thumbnailUrl = "https://www.themealdb.com/images/ingredients/" +
                ingredient.getStrIngredient() + "-Small.png";

        RequestOptions requestOptions = new RequestOptions()
                .placeholder(R.drawable.placeholder_meal)
                .error(R.drawable.ic_close_emoji);

        if (!NetworkUtil.isNetworkAvailable(context)) {
            requestOptions = requestOptions.diskCacheStrategy(DiskCacheStrategy.ALL);
        }

        Glide.with(context)
                .load(thumbnailUrl)
                .apply(requestOptions)
                .into(holder.imageViewThumb);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onIngredientClick(ingredient);
                if (!NetworkUtil.isNetworkAvailable(context)) {
                    Toast.makeText(context, "Showing offline data", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return ingredients != null ? ingredients.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewThumb;
        TextView textViewIngredient;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewThumb = itemView.findViewById(R.id.ivIngredientThumb);
            textViewIngredient = itemView.findViewById(R.id.tvIngredientName);
        }
    }
}