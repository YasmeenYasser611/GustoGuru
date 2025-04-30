package com.example.gustoguru.features.home.view;



import static android.app.ProgressDialog.show;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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
import com.example.gustoguru.features.profile.view.ProfileActivity;
import com.example.gustoguru.features.search.view.SearchActivity;
import com.example.gustoguru.features.sessionmanager.SessionManager;
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
    private TextView tvGreeting;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        sessionManager = new SessionManager(this);


        initializeViews();
        setupAdapters();
        setupRecyclerViews();
        initializePresenter();
        setupPersonalizedGreeting();
        loadData();
        setupBottomNavigation();
    }

    private void setupPersonalizedGreeting() {
        String userName = sessionManager.getUserName();
        String greetingText;

        if (sessionManager.isLoggedIn())
        {
            if (!userName.isEmpty())
            {

                greetingText = String.format("Hey chef %s 👩‍🍳", userName);
            } else {
                greetingText = "Hey Chef! 👩‍🍳";
            }
        }
        else
        {
            greetingText = "Hey Guest Chef! 👨‍🍳";
        }

        tvGreeting.setText(greetingText);
    }



    @Override
    protected void onResume() {
        super.onResume();
        setupPersonalizedGreeting(); // Refresh when returning to activity
    }
    private void initializeViews() {
        tvGreeting = findViewById(R.id.tvGreeting);
        ivMealOfTheDay = findViewById(R.id.ivMealOfTheDay);
        tvMealOfTheDayName = findViewById(R.id.tvMealOfTheDayName);
        categoriesContainer = findViewById(R.id.categoriesContainer);
        mealsByCategoryContainer = findViewById(R.id.mealsByCategoryContainer);
        tvMealsByCategoryTitle = findViewById(R.id.tvMealsByCategoryTitle);
    }

    private void setupAdapters() {
        categoryAdapter = new CategoryAdapter(this, new ArrayList<>(), this);
        mealsAdapter = new MealAdapter(this, new ArrayList<>(),
                meal -> navigateToMealDetail(meal.getIdMeal()),
                null);
    }

    private void setupRecyclerViews() {
        categoriesContainer.setHasFixedSize(true);
        categoriesContainer.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        categoriesContainer.setAdapter(categoryAdapter);

        mealsByCategoryContainer.setHasFixedSize(true);
        mealsByCategoryContainer.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        mealsByCategoryContainer.setAdapter(mealsAdapter);
    }

    private void initializePresenter() {
        presenter = new HomePresenter(this,
                MealRepository.getInstance(
                        AppDatabase.getInstance(this).favoriteMealDao(),
                        AppDatabase.getInstance(this).plannedMealDao(),
                        MealClient.getInstance(),
                        FirebaseClient.getInstance()
                ),
                this
        );
    }

    private void loadData() {
        presenter.getRandomMeal();
        presenter.getAllCategories();
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setOnNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_fav) {
                navigateToFavorites();
                return true;
            } else if (id == R.id.nav_planner) {
                navigateToPlannedMeals();
                return true;
            } else if (id == R.id.nav_profile) {
                checkLoginAndNavigateToProfile();
                return true;
            } else if (id == R.id.nav_search) {
                navigateToSearch();
                return true;
            } else if (id == R.id.nav_home) {
                navigateToHome();
                return true;
            }
            return false;
        });
    }

    private void checkLoginAndNavigateToProfile() {
        SessionManager sessionManager = new SessionManager(this);
        if (sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, ProfileActivity.class));
        } else {
            startActivity(new Intent(this, LoginActivity.class));
        }
    }
    private void showProfileScreen(SessionManager sessionManager) {
        // Inflate profile layout
        View profileView = getLayoutInflater().inflate(R.layout.profile_screen, null);

        // Set user details
        TextView userEmail = profileView.findViewById(R.id.user_email);
        userEmail.setText(sessionManager.getUserEmail());

        // Setup logout button
        Button logoutButton = profileView.findViewById(R.id.logout_button);
        logoutButton.setOnClickListener(v -> {
            sessionManager.logoutUser();
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
            recreate(); // Refresh the activity to update UI
        });

        // Show in a dialog (or you can replace a fragment)
        new AlertDialog.Builder(this)
                .setView(profileView)
                .setCancelable(true)
                .show();
    }

    @Override
    public void onCategoryClick(Category category) {
        tvMealsByCategoryTitle.setVisibility(View.VISIBLE);
        mealsByCategoryContainer.setVisibility(View.VISIBLE);
        tvMealsByCategoryTitle.setText("Meals in " + category.getStrCategory());
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

        findViewById(R.id.mealOfTheDayCard).setOnClickListener(v ->
                navigateToMealDetail(meal.getIdMeal()));
    }

    @Override
    public void showSearchResults(List<FilteredMeal> meals) {
        if (meals == null || meals.isEmpty()) {
            showError("No meals found in this category");
            mealsAdapter.updateMeals(new ArrayList<>());
        } else {
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


    private void navigateToMealDetail(String mealId) {
        Intent intent = new Intent(this, MealDetailActivity.class);
        intent.putExtra("MEAL_ID", mealId);
        startActivity(intent);
    }

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.onDestroy();
    }
}

