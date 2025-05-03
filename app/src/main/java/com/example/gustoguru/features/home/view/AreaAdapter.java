package com.example.gustoguru.features.home.view;

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
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.gustoguru.R;
import com.example.gustoguru.model.network.NetworkUtil;
import com.example.gustoguru.model.pojo.Area;

import java.util.List;

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

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Area area = areas.get(position);
        holder.textViewArea.setText(area.getStrArea());

        String flagUrl = CountryFlagUtils.getFlagUrl(area.getStrArea());

        RequestOptions requestOptions = new RequestOptions()
                .override(120, 80)
                .centerCrop()
                .placeholder(R.drawable.placeholder_meal)
                .error(R.drawable.ic_close_emoji);

        if (!NetworkUtil.isNetworkAvailable(context)) {
            requestOptions = requestOptions.diskCacheStrategy(DiskCacheStrategy.ALL);
        }

        Glide.with(context)
                .load(flagUrl)
                .apply(requestOptions)
                .into(holder.imageViewFlag);

        holder.layout.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAreaClick(area);
                if (!NetworkUtil.isNetworkAvailable(context)) {
                    Toast.makeText(context, "Showing offline data", Toast.LENGTH_SHORT).show();
                }
            }
        });
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
