package com.example.gustoguru.features.home.view.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.example.gustoguru.R;
import com.example.gustoguru.model.network.NetworkUtil;
import com.example.gustoguru.model.pojo.Ingredient;

import java.util.List;

import javax.annotation.Nullable;

public class IngredientAdapter extends RecyclerView.Adapter<IngredientAdapter.ViewHolder> {
    private Context context;
    private List<Ingredient> ingredients;
    private OnIngredientClickListener listener;



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
        String thumbnailUrl = "https://www.themealdb.com/images/ingredients/" +
                ingredient.getStrIngredient() + "-Small.png";

        boolean isOnline = NetworkUtil.isNetworkAvailable(context);

        if (!isOnline) {
            // OFFLINE MODE - Clear any existing animation first
            holder.imageViewThumb.clearAnimation();
            holder.textViewIngredient.setVisibility(View.GONE);

            // Try to load from cache only
            Glide.with(context)
                    .asBitmap()
                    .load(thumbnailUrl)
                    .apply(new RequestOptions()
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .onlyRetrieveFromCache(true))
                    .listener(new RequestListener<Bitmap>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                                    Target<Bitmap> target, boolean isFirstResource) {
                            // Show offline state when no cached version exists
                            holder.imageViewThumb.setImageResource(R.drawable.ic_offline);
                            startOfflineAnimation(holder.imageViewThumb);
                            return true; // Return true to prevent further error handling
                        }

                        @Override
                        public boolean onResourceReady(Bitmap resource, Object model,
                                                       Target<Bitmap> target, DataSource dataSource,
                                                       boolean isFirstResource) {
                            // Show cached version
                            holder.textViewIngredient.setVisibility(View.VISIBLE);
                            return false;
                        }
                    })
                    .into(holder.imageViewThumb);
        } else {
            // ONLINE MODE
            holder.textViewIngredient.setVisibility(View.VISIBLE);
            holder.textViewIngredient.setText(ingredient.getStrIngredient());

            Glide.with(context)
                    .load(thumbnailUrl)
                    .apply(new RequestOptions()
                            .placeholder(R.drawable.placeholder_meal)
                            .error(R.drawable.ic_close_emoji))
                    .into(holder.imageViewThumb);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                if (!isOnline) {
                    if (holder.textViewIngredient.getVisibility() == View.VISIBLE) {
                        listener.onIngredientClick(ingredient);
                    } else {
                        startOfflineAnimation(holder.imageViewThumb);
                        Toast.makeText(context, "No cached data available offline",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    listener.onIngredientClick(ingredient);
                }
            }
        });
    }

    private void startOfflineAnimation(View view) {
        view.clearAnimation();

        // Shake animation
        Animation shake = AnimationUtils.loadAnimation(context, R.anim.shake);
        view.startAnimation(shake);

        // Pulse effect with color tint
        view.animate()
                .alpha(0.6f)
                .setDuration(300)
                .withEndAction(() -> {
                    view.animate()
                            .alpha(1f)
                            .setDuration(300)
                            .start();
                })
                .start();

        // Apply temporary tint
        ImageViewCompat.setImageTintList(
                (ImageView) view,
                ColorStateList.valueOf(ContextCompat.getColor(context, R.color.offline_highlight))
        );

        // Remove tint after animation
        new Handler().postDelayed(() -> {
            ImageViewCompat.setImageTintList((ImageView) view, null);
        }, 1000);
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
    public interface OnIngredientClickListener {
        void onIngredientClick(Ingredient ingredient);
    }
}