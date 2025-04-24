package com.example.gustoguru.features.meal.view;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.gustoguru.R;
import com.example.gustoguru.features.meal.presenter.MealDetailPresenter;
import com.example.gustoguru.model.local.AppDatabase;
import com.example.gustoguru.model.pojo.Meal;
import com.example.gustoguru.model.remote.firebase.FirebaseClient;
import com.example.gustoguru.model.remote.retrofit.callback.MealCallback;
import com.example.gustoguru.model.remote.retrofit.client.MealClient;
import com.example.gustoguru.model.repository.MealRepository;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MealDetailActivity extends AppCompatActivity implements MealDetailView {

    private RecyclerView recyclerView;
    private IngredientsAdapter ingredientsAdapter;
    private MealDetailPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meal_detail);

        // Initialize adapter
        ingredientsAdapter = new IngredientsAdapter(this);

        // Initialize presenter with repository
        presenter = new MealDetailPresenter(
                this,
                MealRepository.getInstance(
                        AppDatabase.getInstance(this).favoriteMealDao(),
                        AppDatabase.getInstance(this).plannedMealDao(),
                        MealClient.getInstance(),
                        FirebaseClient.getInstance()
                )
        );

        // Setup RecyclerView
        recyclerView = findViewById(R.id.rvIngredients);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(ingredientsAdapter);

        // Get meal ID from intent and load data
        String mealId = getIntent().getStringExtra("MEAL_ID");
        if (mealId != null && !mealId.isEmpty()) {
            presenter.getMealDetails(mealId);
        } else {
            showError("No meal ID provided");
            finish();
        }
    }

    // Implement MealDetailView methods (only the ones we need for ingredients)
    @Override
    public void showMealDetails(Meal meal) {
        // We'll use this to update other views if needed
    }

    @Override
    public void showIngredients(Map<String, String> ingredientMeasureMap) {
        // Update adapter with the ingredients
        ingredientsAdapter.setMeal(ingredientMeasureMap);
    }

    @Override
    public void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    // Unused methods from interface (could be abstract class instead)
    @Override public void showLoading() {}
    @Override public void hideLoading() {}
    @Override public void showInstructions(String instructions) {}
    @Override public void showYoutubeVideo(String videoUrl) {}
    @Override public void showFavoriteStatus(boolean isFavorite) {}
    @Override public void navigateBack() {}
}