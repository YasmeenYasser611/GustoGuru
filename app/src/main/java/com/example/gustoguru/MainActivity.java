package com.example.gustoguru;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;

import com.example.gustoguru.model.pojo.Meal;
import com.example.gustoguru.model.repository.MealRepository;
import com.example.gustoguru.model.local.AppDatabase;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "FAV_TEST";
    private MealRepository mealRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize repository
        mealRepository = MealRepository.getInstance(
                AppDatabase.getInstance(this).favoriteMealDao(),
                AppDatabase.getInstance(this).plannedMealDao(),
                null
        );

        // Create test meal
        Meal testMeal = new Meal();
        testMeal.setIdMeal("test_123");
        testMeal.setStrMeal("YasTest Favorite Meal");
        testMeal.setStrCategory("Test Category");
        testMeal.setFavorite(true);

        // Add to favorites
//        mealRepository.addFavorite(testMeal);

        mealRepository.removeFavorite(testMeal);

    }





}