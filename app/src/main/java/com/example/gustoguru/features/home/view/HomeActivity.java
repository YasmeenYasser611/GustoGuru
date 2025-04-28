package com.example.gustoguru.features.home.view;



import static android.app.ProgressDialog.show;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.gustoguru.R;
import com.example.gustoguru.features.favorites.view.FavoritesActivity;
import com.example.gustoguru.features.home.presenter.HomePresenter;
import com.example.gustoguru.features.meal.view.MealDetailActivity;
import com.example.gustoguru.features.weekly_planner.view.PlannedActivity;
import com.example.gustoguru.model.pojo.Category;
import com.example.gustoguru.model.pojo.Meal;
import com.example.gustoguru.model.repository.MealRepository;
import com.example.gustoguru.model.remote.firebase.FirebaseClient;
import com.example.gustoguru.model.remote.retrofit.client.MealClient;
import com.example.gustoguru.model.local.AppDatabase;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity implements HomeView {
    private HomePresenter presenter;


    private RecyclerView categoriesContainer;

    private  CategoryAdapter categoryAdapter;

    private TextView tvMealOfTheDayName ;

    private ImageView ivMealOfTheDay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        ivMealOfTheDay = findViewById(R.id.ivMealOfTheDay);
        tvMealOfTheDayName= findViewById(R.id.tvMealOfTheDayName);


        categoryAdapter= new CategoryAdapter(HomeActivity.this, new ArrayList<>());


        presenter = new HomePresenter(this , MealRepository.getInstance(AppDatabase.getInstance(this).favoriteMealDao(), AppDatabase.getInstance(this).plannedMealDao(), MealClient.getInstance(), FirebaseClient.getInstance()));

        categoriesContainer = findViewById(R.id.categoriesContainer);
        categoriesContainer.setHasFixedSize(true);
        categoriesContainer.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        presenter.getRandomMeal();
        presenter.getAllCategories();

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setOnNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_fav) {
                navigateToFavorites();
                return true;
            } else if (item.getItemId() == R.id.nav_planner) {
                navigateToPlannedMeals();
                return true;
            }
            // Handle other menu items if needed
            return false;
        });


    }

    private void navigateToPlannedMeals() {
        Intent intent = new Intent(this, PlannedActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void navigateToFavorites() {
        Intent intent = new Intent(this, FavoritesActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }


    @Override
    public void showCategories(List<Category> categories)
    {
        categoryAdapter.setCategories(categories);
        categoriesContainer.setAdapter(categoryAdapter);
        categoryAdapter.notifyDataSetChanged();

    }

    @Override
    public void showError(String message) {
        Toast.makeText(HomeActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();

    }

    @Override
    public void showMealOfTheDay(Meal meal) {
        // Set meal name
        tvMealOfTheDayName.setText(meal.getStrMeal());

        // Load meal image with Glide
        Glide.with(this)
                .load(meal.getStrMealThumb())
                .centerCrop()
                .into(ivMealOfTheDay);

        // Optional: Make the card clickable
        findViewById(R.id.mealOfTheDayCard).setOnClickListener(v -> {
            Intent intent = new Intent(this, MealDetailActivity.class);
            intent.putExtra("MEAL_ID", meal.getIdMeal());
            startActivity(intent);

        });
    }
}

