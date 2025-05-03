package com.example.gustoguru.features.home.view;

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
import com.example.gustoguru.model.pojo.Area;

import java.util.List;

import javax.annotation.Nullable;

public class AreaAdapter extends RecyclerView.Adapter<AreaAdapter.ViewHolder> {
    private Context context;
    private List<Area> areas;
    private OnAreaClickListener listener;

    public interface OnAreaClickListener {
        void onAreaClick(Area area);
    }

    public AreaAdapter(Context context, List<Area> areas, OnAreaClickListener listener) {
        this.context = context;
        this.areas = areas;
        this.listener = listener;
    }

    public void setAreas(List<Area> areas) {
        this.areas = areas;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_area, parent, false);
        return new ViewHolder(view);
    }
    @NonNull
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Area area = areas.get(position);
        String flagUrl = CountryFlagUtils.getFlagUrl(area.getStrArea());
        boolean isOnline = NetworkUtil.isNetworkAvailable(context);

        if (!isOnline) {
            // OFFLINE MODE
            holder.textViewArea.setVisibility(View.GONE); // Hide area name initially
            holder.imageViewFlag.clearAnimation();

            // Try to load cached flag only
            Glide.with(context)
                    .asBitmap()
                    .load(flagUrl)
                    .apply(new RequestOptions()
                            .override(120, 80)
                            .centerCrop()
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .onlyRetrieveFromCache(true))
                    .listener(new RequestListener<Bitmap>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                                    Target<Bitmap> target, boolean isFirstResource) {
                            // Show offline state when no cached version exists
                            holder.imageViewFlag.setImageResource(R.drawable.ic_offline);
                            startOfflineAnimation(holder.imageViewFlag);
                            return true; // Prevent default error handling
                        }

                        @Override
                        public boolean onResourceReady(Bitmap resource, Object model,
                                                       Target<Bitmap> target, DataSource dataSource,
                                                       boolean isFirstResource) {
                            // Show cached version with name
                            holder.textViewArea.setVisibility(View.VISIBLE);
                            holder.textViewArea.setText(area.getStrArea());
                            return false;
                        }
                    })
                    .into(holder.imageViewFlag);
        } else {
            // ONLINE MODE - normal behavior
            holder.textViewArea.setVisibility(View.VISIBLE);
            holder.textViewArea.setText(area.getStrArea());

            Glide.with(context)
                    .load(flagUrl)
                    .apply(new RequestOptions()
                            .override(120, 80)
                            .centerCrop()
                            .placeholder(R.drawable.placeholder_meal)
                            .error(R.drawable.ic_close_emoji))
                    .into(holder.imageViewFlag);
        }

        holder.layout.setOnClickListener(v -> {
            if (listener != null) {
                if (!isOnline) {
                    if (holder.textViewArea.getVisibility() == View.VISIBLE) {
                        listener.onAreaClick(area);
                    } else {
                        startOfflineAnimation(holder.imageViewFlag);
                        Toast.makeText(context, "No cached flag available offline",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    listener.onAreaClick(area);
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
        return areas != null ? areas.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewArea;
        ImageView imageViewFlag;
        LinearLayout layout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewArea = itemView.findViewById(R.id.tvAreaName);
            imageViewFlag = itemView.findViewById(R.id.ivAreaFlag);
            layout = itemView.findViewById(R.id.areaLayout);
        }
    }
}
