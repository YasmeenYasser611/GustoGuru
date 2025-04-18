package com.example.gustoguru;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.example.gustoguru.model.pojo.Meal;
import com.example.gustoguru.model.repository.MealRepository;
import com.example.gustoguru.model.local.AppDatabase;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "PLANNED_TEST";
    private MealRepository mealRepository;
    private final String TEST_DATE = "2023-12-25";

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
        testMeal.setIdMeal("planned_123");
        testMeal.setStrMeal("Christmas Dinner");
        testMeal.setStrCategory("Holiday");

        // Test adding to planned meals
//        testAddPlannedMeal(testMeal);

        // To test removal instead, uncomment this:
         testRemovePlannedMeal(testMeal);
    }

    private void testAddPlannedMeal(Meal meal) {
        Log.d(TAG, "Adding meal to planned meals for date: " + TEST_DATE);
        mealRepository.addPlannedMeal(meal, TEST_DATE);
        Log.d(TAG, "Check Database Inspector - should see meal in planned meals");
    }

    private void testRemovePlannedMeal(Meal meal) {
        Log.d(TAG, "Removing planned meal...");

        meal.setPlannedDate(null);
        mealRepository.addPlannedMeal(meal, null);

        Log.d(TAG, "Check Database Inspector - meal should no longer be planned");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AppDatabase.getInstance(this).close();
    }
}