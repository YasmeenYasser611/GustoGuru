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
import android.widget.LinearLayout;
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
import com.example.gustoguru.model.pojo.Category;

import java.util.List;

import javax.annotation.Nullable;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {
    private Context context;
    private List<Category> categories;
    private OnCategoryClickListener listener;

    public interface OnCategoryClickListener {
        void onCategoryClick(Category category);
    }

    public CategoryAdapter(Context context, List<Category> categories, OnCategoryClickListener listener) {
        this.context = context;
        this.categories = categories;
        this.listener = listener;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.category_chip, parent, false);
        return new ViewHolder(view);
    }


    @NonNull
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Category category = categories.get(position);
        boolean isOnline = NetworkUtil.isNetworkAvailable(context);

        if (!isOnline) {
            // OFFLINE MODE
            holder.textViewCategory.setVisibility(View.GONE);
            holder.textViewDescription.setVisibility(View.GONE);
            holder.imageViewThumb.clearAnimation();

            // Try to load cached image only
            Glide.with(context)
                    .asBitmap()
                    .load(category.getStrCategoryThumb())
                    .apply(new RequestOptions()
                            .placeholder(R.drawable.placeholder_meal)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .onlyRetrieveFromCache(true))
                    .listener(new RequestListener<Bitmap>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                                    Target<Bitmap> target, boolean isFirstResource) {
                            // Show offline state when no cached version exists
                            holder.imageViewThumb.setImageResource(R.drawable.ic_offline);
                            startOfflineAnimation(holder.imageViewThumb);
                            return true; // Prevent default error handling
                        }

                        @Override
                        public boolean onResourceReady(Bitmap resource, Object model,
                                                       Target<Bitmap> target, DataSource dataSource,
                                                       boolean isFirstResource) {
                            // Show cached version with text
                            holder.textViewCategory.setVisibility(View.VISIBLE);
                            holder.textViewDescription.setVisibility(View.VISIBLE);
                            holder.textViewCategory.setText(category.getStrCategory());
                            holder.textViewDescription.setText(category.getStrCategoryDescription());
                            return false;
                        }
                    })
                    .into(holder.imageViewThumb);
        } else {
            // ONLINE MODE - normal behavior
            holder.textViewCategory.setVisibility(View.VISIBLE);
            holder.textViewDescription.setVisibility(View.VISIBLE);
            holder.textViewCategory.setText(category.getStrCategory());
            holder.textViewDescription.setText(category.getStrCategoryDescription());

            Glide.with(context)
                    .load(category.getStrCategoryThumb())
                    .apply(new RequestOptions()
                            .placeholder(R.drawable.placeholder_meal)
                            .error(R.drawable.ic_close_emoji))
                    .into(holder.imageViewThumb);
        }

        holder.layout.setOnClickListener(v -> {
            if (listener != null) {
                if (!isOnline) {
                    if (holder.textViewCategory.getVisibility() == View.VISIBLE) {
                        listener.onCategoryClick(category);
                    } else {
                        startOfflineAnimation(holder.imageViewThumb);
                        Toast.makeText(context, "No cached category available offline",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    listener.onCategoryClick(category);
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
        return categories != null ? categories.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewCategory, textViewDescription;
        ImageView imageViewThumb;
        LinearLayout layout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewCategory = itemView.findViewById(R.id.textViewCategory);
            textViewDescription = itemView.findViewById(R.id.textViewDescription);
            imageViewThumb = itemView.findViewById(R.id.imageViewThumb);
            layout = itemView.findViewById(R.id.layout);
        }
    }
}