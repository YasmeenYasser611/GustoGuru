package com.example.gustoguru.features.meal.view;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
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
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
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

import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MealDetailActivity extends AppCompatActivity implements MealDetailView {
    private static final int REQUEST_CALENDAR_PERMISSION = 1001;
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
        initializeViews();
        setupRecyclerView();
        initializePresenter();
        loadMealDetails();
    }

    private void initializeViews() {
        progressBar = findViewById(R.id.progressBar);
        ivMeal = findViewById(R.id.ivMeal);
        btnFavorite = findViewById(R.id.btnFavorite);
        tvMealName = findViewById(R.id.tvMealName);
        tvCategory = findViewById(R.id.tvCategory);
        tvArea = findViewById(R.id.tvArea);
        tvInstructions = findViewById(R.id.tvInstructions);
        youtubePlayerView = findViewById(R.id.youtubePlayerView);

        ImageButton btnAddToCalendar = findViewById(R.id.btnAddToCalendar);
        btnAddToCalendar.setOnClickListener(v -> showDatePickerForNextWeek());
        btnFavorite.setOnClickListener(v -> presenter.toggleFavorite());

        rvIngredients = findViewById(R.id.rvIngredients);
        rvIngredients.setHasFixedSize(true);
    }

    private void setupRecyclerView() {
        ingredientsAdapter = new IngredientsAdapter(this);
        rvIngredients.setLayoutManager(new LinearLayoutManager(this));
        rvIngredients.setAdapter(ingredientsAdapter);
    }

    private void initializePresenter() {
        presenter = new MealDetailPresenter(
                this,
                MealRepository.getInstance(
                        AppDatabase.getInstance(this).favoriteMealDao(),
                        AppDatabase.getInstance(this).plannedMealDao(),
                        MealClient.getInstance(),
                        FirebaseClient.getInstance()
                ),
                getApplicationContext()
        );
    }

    private void loadMealDetails() {
        String mealId = getIntent().getStringExtra("MEAL_ID");
        if (mealId == null || mealId.isEmpty()) {
            showError("Invalid meal ID");
            finish();
            return;
        }
        presenter.getMealDetails(mealId);
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
            tvMealName.setText(meal.getStrMeal());
            tvCategory.setText(meal.getStrCategory());
            tvArea.setText(meal.getStrArea());
            tvInstructions.setText(meal.getStrInstructions());

            Glide.with(this)
                    .load(meal.getStrMealThumb())
                    .into(ivMeal);

            updateFavoriteButton(meal.isFavorite());
        });
    }

    @Override
    public void showIngredients(Map<String, String> ingredientMeasureMap) {
        ingredientsAdapter.setMeal(ingredientMeasureMap);
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
        updateFavoriteButton(isFavorite);
    }

    @Override
    public void showError(String message) {
        runOnUiThread(() -> Toast.makeText(this, message, Toast.LENGTH_SHORT).show());
    }



    private void updateFavoriteButton(boolean isFavorite) {
        int iconRes = isFavorite ? R.drawable.ic_favorite : R.drawable.ic_favorite_border;
        btnFavorite.setImageResource(iconRes);
    }

    private String extractYouTubeId(String url) {
        String pattern = "(?<=watch\\?v=|/videos/|embed\\/|youtu.be\\/|\\/v\\/|\\/e\\/|watch\\?v%3D|watch\\?feature=player_embedded&v=|%2Fvideos%2F|embed%\\?video_id=)([^#\\&\\?\\n]*)";
        Matcher matcher = Pattern.compile(pattern).matcher(url);
        return matcher.find() ? matcher.group() : null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (youtubePlayerView != null) {
            youtubePlayerView.release();
        }
    }

    @Override
    public void showPlannerSuccess(String date) {
        runOnUiThread(() -> Toast.makeText(this,
                getString(R.string.meal_added_to_planner, date),
                Toast.LENGTH_SHORT).show());
    }

    @Override
    public void showCalendarSuccess() {
        runOnUiThread(() -> Toast.makeText(this,
                R.string.added_to_device_calendar,
                Toast.LENGTH_SHORT).show());
    }

    @Override
    public void showDatePicker(Calendar minDate, Calendar maxDate)
    {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, day) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(year, month, day);
                    presenter.handleDateSelected(selectedDate);
                },
                minDate.get(Calendar.YEAR),
                minDate.get(Calendar.MONTH),
                minDate.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.getDatePicker().setMinDate(minDate.getTimeInMillis());
        datePickerDialog.getDatePicker().setMaxDate(maxDate.getTimeInMillis());
        datePickerDialog.show();
    }

    @Override
    public void requestCalendarPermission(int requestCode) {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_CALENDAR},
                requestCode);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CALENDAR_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showDatePickerForNextWeek();
            } else {
                showError("Calendar permission denied");
            }
        }
    }

    private void showDatePickerForNextWeek() {
        Calendar today = Calendar.getInstance();
        Calendar maxDate = Calendar.getInstance();
        maxDate.add(Calendar.DAY_OF_YEAR, 6);
        showDatePicker(today, maxDate);
    }
}