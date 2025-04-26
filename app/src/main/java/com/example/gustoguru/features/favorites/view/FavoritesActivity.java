package com.example.gustoguru.features.favorites.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gustoguru.R;
import com.example.gustoguru.features.favorites.presenter.FavoritesPresenter;
import com.example.gustoguru.features.meal.view.MealDetailActivity;
import com.example.gustoguru.model.local.AppDatabase;
import com.example.gustoguru.model.pojo.Meal;
import com.example.gustoguru.model.remote.firebase.FirebaseClient;
import com.example.gustoguru.model.remote.retrofit.client.MealClient;
import com.example.gustoguru.model.repository.MealRepository;

import java.util.ArrayList;
import java.util.List;

public class FavoritesActivity extends AppCompatActivity implements FavoritesView {
    private FavoritesPresenter presenter;
    private RecyclerView recyclerView;
    private MealAdapter adapter;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        // Initialize views
        progressBar = findViewById(R.id.progressBar);
        recyclerView = findViewById(R.id.rv_favorites);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Reuse your existing MealAdapter or create a new one
        adapter = new MealAdapter(this, new ArrayList<>(), this::onMealClick, this::onFavoriteClick);
        recyclerView.setAdapter(adapter);

        // Initialize presenter
        presenter = new FavoritesPresenter(
                this,
                MealRepository.getInstance(
                        AppDatabase.getInstance(this).favoriteMealDao(),
                        AppDatabase.getInstance(this).plannedMealDao(),
                        MealClient.getInstance(),
                        FirebaseClient.getInstance()
                )
        );

        // Load favorites
        presenter.loadFavorites();
    }

    private void onMealClick(Meal meal) {
        Intent intent = new Intent(this, MealDetailActivity.class);
        intent.putExtra("MEAL_ID", meal.getIdMeal());
        startActivity(intent);
    }

    private void onFavoriteClick(Meal meal) {
        presenter.toggleFavorite(meal);
    }

    @Override
    public void showFavorites(List<Meal> meals) {
        adapter.updateMeals(meals);
    }

    @Override
    public void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideLoading() {
        progressBar.setVisibility(View.GONE);
    }
}