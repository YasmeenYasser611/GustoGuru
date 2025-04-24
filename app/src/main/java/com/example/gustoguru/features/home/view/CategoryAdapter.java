package com.example.gustoguru.features.home.view;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.gustoguru.R;
import com.example.gustoguru.model.pojo.Category;

import java.util.ArrayList;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {
    private Context context;
    private List<Category> categories;


    public CategoryAdapter(Context context, List<Category> categories )
    {
        this.context= context;
        this.categories = categories;
    }


    public void setCategories(List<Category> categories) {
        this.categories.clear();
        this.categories.addAll(categories);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.category_chip, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position)
        {
            Category category = categories.get(position);

            holder.textViewCategory.setText(category.getStrCategory());
            holder.textViewDescription.setText(category.getStrCategoryDescription());

//            Glide.with(context)
//                    .load(category.getStrCategoryThumb())
//                    .apply(new RequestOptions()
//                            .override(200, 200)
//                            .placeholder(R.drawable.ic_launcher_background)
//                            .error(R.drawable.ic_launcher_foreground))
//                    .into(holder.imageViewThumb);

            Glide.with(context)
                    .load(category.getStrCategoryThumb())
                    .override(120, 80)  // Matches ImageView dimensions
                    .centerCrop()
                    .into(holder.imageViewThumb);

        holder.layout.setOnClickListener(v -> {
            Toast.makeText(context, category.getStrCategory(), Toast.LENGTH_SHORT).show();
        });


        }


    @Override
    public int getItemCount()
    {
        return categories != null ? categories.size() : 0;
    }




    public static class ViewHolder extends RecyclerView.ViewHolder
    {
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