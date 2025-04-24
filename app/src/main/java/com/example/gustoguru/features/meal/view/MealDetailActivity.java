package com.example.gustoguru.features.meal.view;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
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
    private MealDetailPresenter presenter;
    private IngredientsAdapter ingredientsAdapter;

    // Views
    private ProgressBar progressBar;
    private ImageView ivMeal;
    private ImageButton btnFavorite;
    private TextView tvMealName, tvCategory, tvArea, tvInstructions;
    private YouTubePlayerView youtubePlayerView;
    private RecyclerView rvIngredients;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meal_detail);

        // Initialize views
        progressBar = findViewById(R.id.progressBar);
        ivMeal = findViewById(R.id.ivMeal);
        btnFavorite = findViewById(R.id.btnFavorite);
        tvMealName = findViewById(R.id.tvMealName);
        tvCategory = findViewById(R.id.tvCategory);
        tvArea = findViewById(R.id.tvArea);
        tvInstructions = findViewById(R.id.tvInstructions);
        youtubePlayerView = findViewById(R.id.youtubePlayerView);
        rvIngredients = findViewById(R.id.rvIngredients);
        rvIngredients.setHasFixedSize(true);

//        rvIngredients.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        // Setup RecyclerView
        ingredientsAdapter = new IngredientsAdapter(this);
        rvIngredients.setLayoutManager(new LinearLayoutManager(this));
        rvIngredients.setAdapter(ingredientsAdapter);

        // Initialize presenter
        presenter = new MealDetailPresenter(
                this,
                MealRepository.getInstance(
                        AppDatabase.getInstance(this).favoriteMealDao(),
                        AppDatabase.getInstance(this).plannedMealDao(),
                        MealClient.getInstance(),
                        FirebaseClient.getInstance()
                )
        );

        // Get meal ID from intent
        String mealId = getIntent().getStringExtra("MEAL_ID");
        if (mealId == null || mealId.isEmpty()) {
            showError("Invalid meal ID");
            finish();
            return;
        }


        // Load meal details
        presenter.getMealDetails(mealId);

        // Setup favorite button click listener
        btnFavorite.setOnClickListener(v -> presenter.toggleFavorite());
    }

    // Implement MealDetailView methods
    @Override
    public void showLoading() {
        runOnUiThread(() -> progressBar.setVisibility(View.VISIBLE));
    }

    @Override
    public void hideLoading() {
        runOnUiThread(() -> progressBar.setVisibility(View.GONE));
    }

    @Override
    public void showMealDetails(Meal meal) {
        runOnUiThread(() -> {
            // Set basic meal info
            tvMealName.setText(meal.getStrMeal());
            tvCategory.setText(meal.getStrCategory());
            tvArea.setText(meal.getStrArea());
            tvInstructions.setText(meal.getStrInstructions());

            // Load meal image
            Glide.with(this)
                    .load(meal.getStrMealThumb())
                    .into(ivMeal);

            // Update favorite button
            updateFavoriteButton(meal.isFavorite());
        });
    }


    @Override
    public void showIngredients(Map<String, String> ingredientMeasureMap) {
        Log.d("MealDetail", "Ingredients Map Size: " + ingredientMeasureMap.size());
        for (Map.Entry<String, String> entry : ingredientMeasureMap.entrySet()) {
            Log.d("MealDetail", "Ingredient: " + entry.getKey() + " | Measure: " + entry.getValue());
        }
        ingredientsAdapter.setMeal(ingredientMeasureMap);
    }

    @Override
    public void showInstructions(String instructions) {

    }

    @Override
    public void showYoutubeVideo(String videoUrl) {
        runOnUiThread(() -> {
            if (videoUrl == null || videoUrl.isEmpty()) {
                youtubePlayerView.setVisibility(View.GONE);
                return;
            }

            String videoId = extractYouTubeId(videoUrl);
            if (videoId == null) {
                youtubePlayerView.setVisibility(View.GONE);
                return;
            }

            youtubePlayerView.setVisibility(View.VISIBLE);
            getLifecycle().addObserver(youtubePlayerView);

            youtubePlayerView.addYouTubePlayerListener(new AbstractYouTubePlayerListener() {
                @Override
                public void onReady(@NonNull YouTubePlayer youTubePlayer) {
                    youTubePlayer.cueVideo(videoId, 0);
                }
            });
        });
    }

    @Override
    public void showFavoriteStatus(boolean isFavorite) {
        runOnUiThread(() -> updateFavoriteButton(isFavorite));
    }

    @Override
    public void showError(String message) {
        runOnUiThread(() -> Toast.makeText(this, message, Toast.LENGTH_SHORT).show());
    }

    @Override
    public void navigateBack() {
        finish();
    }

    private void updateFavoriteButton(boolean isFavorite) {
        int iconRes = isFavorite ? R.drawable.ic_favorite : R.drawable.ic_favorite_border;
        btnFavorite.setImageResource(iconRes);
    }

    private String extractYouTubeId(String url) {
        String pattern = "(?<=watch\\?v=|/videos/|embed\\/|youtu.be\\/|\\/v\\/|\\/e\\/|watch\\?v%3D|watch\\?feature=player_embedded&v=|%2Fvideos%2F|embed%\\?video_id=)([^#\\&\\?\\n]*)";
        Pattern compiledPattern = Pattern.compile(pattern);
        Matcher matcher = compiledPattern.matcher(url);
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (youtubePlayerView != null) {
            youtubePlayerView.release();
        }
    }
}