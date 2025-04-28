package com.example.gustoguru.features.search.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gustoguru.R;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.List;

public class SuggestionAdapter extends RecyclerView.Adapter<SuggestionAdapter.ViewHolder> {
    private List<String> suggestions = new ArrayList<>();
    private final OnSuggestionClickListener listener;

    public SuggestionAdapter(OnSuggestionClickListener listener) {
        this.listener = listener;
    }

    public void updateSuggestions(List<String> newSuggestions) {
        this.suggestions = new ArrayList<>(newSuggestions);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_suggestion_chip, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.chip.setText(suggestions.get(position));
        holder.chip.setOnClickListener(v ->
                listener.onSuggestionClick(suggestions.get(position)));
    }

    @Override
    public int getItemCount() {
        return suggestions.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        Chip chip;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            chip = itemView.findViewById(R.id.chip);
        }
    }

    public interface OnSuggestionClickListener {
        void onSuggestionClick(String suggestion);
    }
}
