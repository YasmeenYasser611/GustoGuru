package com.example.gustoguru.features.home.view;



import static android.app.ProgressDialog.show;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.gustoguru.R;
import com.example.gustoguru.features.authentication.login.view.LoginActivity;
import com.example.gustoguru.features.favorites.view.FavoritesActivity;
import com.example.gustoguru.features.home.presenter.HomePresenter;
import com.example.gustoguru.features.meal.view.MealAdapter;
import com.example.gustoguru.features.meal.view.MealDetailActivity;
import com.example.gustoguru.features.search.view.SearchActivity;
import com.example.gustoguru.features.weekly_planner.view.PlannedActivity;
import com.example.gustoguru.model.pojo.Category;
import com.example.gustoguru.model.pojo.FilteredMeal;
import com.example.gustoguru.model.pojo.Meal;
import com.example.gustoguru.model.repository.MealRepository;
import com.example.gustoguru.model.remote.firebase.FirebaseClient;
import com.example.gustoguru.model.remote.retrofit.client.MealClient;
import com.example.gustoguru.model.local.AppDatabase;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity implements HomeView, CategoryAdapter.OnCategoryClickListener {
    private HomePresenter presenter;
    private RecyclerView categoriesContainer;
    private CategoryAdapter categoryAdapter;
    private TextView tvMealOfTheDayName;
    private ImageView ivMealOfTheDay;
    private RecyclerView mealsByCategoryContainer;
    private TextView tvMealsByCategoryTitle;
    private MealAdapter mealsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize views
        ivMealOfTheDay = findViewById(R.id.ivMealOfTheDay);
        tvMealOfTheDayName = findViewById(R.id.tvMealOfTheDayName);
        categoriesContainer = findViewById(R.id.categoriesContainer);
        mealsByCategoryContainer = findViewById(R.id.mealsByCategoryContainer);
        tvMealsByCategoryTitle = findViewById(R.id.tvMealsByCategoryTitle);

        // Initialize adapters
        categoryAdapter = new CategoryAdapter(this, new ArrayList<>(), this);
        mealsAdapter = new MealAdapter(this, new ArrayList<>(),
                meal -> {
                    Intent intent = new Intent(this, MealDetailActivity.class);
                    intent.putExtra("MEAL_ID", meal.getIdMeal());
                    startActivity(intent);
                },
                null
        );

        // Setup RecyclerViews
        categoriesContainer.setHasFixedSize(true);
        categoriesContainer.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        categoriesContainer.setAdapter(categoryAdapter);

        mealsByCategoryContainer.setHasFixedSize(true);
        mealsByCategoryContainer.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        mealsByCategoryContainer.setAdapter(mealsAdapter);

        // Initialize presenter
        presenter = new HomePresenter(this,
                MealRepository.getInstance(
                        AppDatabase.getInstance(this).favoriteMealDao(),
                        AppDatabase.getInstance(this).plannedMealDao(),
                        MealClient.getInstance(),
                        FirebaseClient.getInstance()
                ),
                this
        );

        // Load data
        presenter.getRandomMeal();
        presenter.getAllCategories();

        // Bottom navigation setup
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setOnNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_fav) {
                navigateToFavorites();
                return true;
            }
            else if (item.getItemId() == R.id.nav_planner) {
                navigateToPlannedMeals();
                return true;
            }
            else if (item.getItemId() == R.id.nav_profile) {
                navigateToLogin();
                return true;
            }
            else if (item.getItemId() == R.id.nav_search) {
                navigateToSearch();
                return true;
            }
            else if (item.getItemId() == R.id.nav_home) {
                navigateToHome();
                return true;
            }
            return false;
        });
    }

    @Override
    public void onCategoryClick(Category category) {
        // Show the meals section
        tvMealsByCategoryTitle.setVisibility(View.VISIBLE);
        mealsByCategoryContainer.setVisibility(View.VISIBLE);

        // Update the title with category name
        tvMealsByCategoryTitle.setText("Meals in " + category.getStrCategory());

        // Fetch meals for this category
        presenter.searchByCategory(category.getStrCategory());
    }

    @Override
    public void showCategories(List<Category> categories) {
        categoryAdapter.setCategories(categories);
    }

    @Override
    public void showMealOfTheDay(Meal meal) {
        tvMealOfTheDayName.setText(meal.getStrMeal());
        Glide.with(this)
                .load(meal.getStrMealThumb())
                .centerCrop()
                .into(ivMealOfTheDay);

        findViewById(R.id.mealOfTheDayCard).setOnClickListener(v -> {
            Intent intent = new Intent(this, MealDetailActivity.class);
            intent.putExtra("MEAL_ID", meal.getIdMeal());
            startActivity(intent);
        });
    }


    @Override
    public void showSearchResults(List<FilteredMeal> meals) {
        if (meals == null || meals.isEmpty()) {
            showError("No meals found in this category");
            mealsAdapter.updateMeals(new ArrayList<>());
        } else {
            // Convert FilteredMeal to Meal objects
            List<Meal> mealList = new ArrayList<>();
            for (FilteredMeal filteredMeal : meals) {
                Meal meal = new Meal();
                meal.setIdMeal(filteredMeal.getIdMeal());
                meal.setStrMeal(filteredMeal.getStrMeal());
                meal.setStrMealThumb(filteredMeal.getStrMealThumb());
                mealList.add(meal);
            }
            mealsAdapter.updateMeals(mealList);
        }
    }

    @Override
    public void showError(String message) {
        Toast.makeText(this, "Error: " + message, Toast.LENGTH_SHORT).show();
    }


    public void showLoading() {
        // Implement if needed
    }


    public void hideLoading() {
        // Implement if needed
    }

    // Navigation methods
    private void navigateToSearch() {
        startActivity(new Intent(this, SearchActivity.class));
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void navigateToHome() {
        startActivity(new Intent(this, HomeActivity.class));
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void navigateToPlannedMeals() {
        startActivity(new Intent(this, PlannedActivity.class));
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void navigateToFavorites() {
        startActivity(new Intent(this, FavoritesActivity.class));
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    private void navigateToLogin() {
        startActivity(new Intent(this, LoginActivity.class));
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}

