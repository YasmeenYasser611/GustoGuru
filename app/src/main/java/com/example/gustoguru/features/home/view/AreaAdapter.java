package com.example.gustoguru.features.home.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.gustoguru.R;
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

        // Load flag using CountryFlagUtils
        String flagUrl = CountryFlagUtils.getFlagUrl(area.getStrArea());
        Glide.with(context)
                .load(flagUrl)
                .override(120, 80) // Consistent with CategoryAdapter
                .centerCrop()
                .placeholder(R.drawable.placeholder_meal)
                .error(R.drawable.ic_close_emoji)
                .into(holder.imageViewFlag);

        holder.layout.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAreaClick(area);
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
