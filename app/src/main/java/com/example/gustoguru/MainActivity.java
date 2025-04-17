package com.example.gustoguru;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.example.gustoguru.model.pojo.Area;
import com.example.gustoguru.model.pojo.Category;
import com.example.gustoguru.model.pojo.FilteredMeal;
import com.example.gustoguru.model.pojo.Ingredient;
import com.example.gustoguru.model.pojo.Meal;
import com.example.gustoguru.model.remote.callback.AreaCallback;
import com.example.gustoguru.model.remote.callback.CategoryCallback;
import com.example.gustoguru.model.remote.callback.FilteredMealCallback;
import com.example.gustoguru.model.remote.callback.IngredientCallback;
import com.example.gustoguru.model.remote.callback.MealCallback;
import com.example.gustoguru.model.remote.client.MealClient;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "API_TEST";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        runAllTests();
    }

    private void runAllTests() {
        Log.d(TAG, "===== STARTING API TEST SUITE =====");

        testRandomMeal();
        testSearchMeals();
        testCategories();
        testAreas();
        testIngredients();
        testFilterByCategory();
        testInvalidEndpoint();
    }

    // Test Case 1: Random Meal
    private void testRandomMeal() {
        Log.d(TAG, "\n[TEST 1] Fetching Random Meal");

        MealClient.getInstance().getRandomMeal(new MealCallback() {
            @Override
            public void onSuccess(List<Meal> meals) {
                if (meals != null && !meals.isEmpty()) {
                    Meal meal = meals.get(0);
                    Log.d(TAG, "1-SUCCESS: Received random meal - " + meal.getStrMeal());
                    logMealDetails(meal);
                } else {
                    Log.e(TAG, "FAILED: Empty response for random meal");
                }
            }

            @Override
            public void onFailure(String errorMsg) {
                Log.e(TAG, "FAILED: " + errorMsg);
            }
        });
    }

    // Test Case 2: Search Meals
    private void testSearchMeals() {
        Log.d(TAG, "\n[TEST 2] Searching Meals by Name (Pasta)");

        MealClient.getInstance().searchMealsByName("pasta", new MealCallback() {
            @Override
            public void onSuccess(List<Meal> meals) {
                if (meals != null && !meals.isEmpty()) {
                    Log.d(TAG, "2-SUCCESS: Found " + meals.size() + " pasta meals");
                    logMealDetails(meals.get(0));
                } else {
                    Log.e(TAG, "FAILED: No pasta meals found");
                }
            }

            @Override
            public void onFailure(String errorMsg) {
                Log.e(TAG, "FAILED: " + errorMsg);
            }
        });
    }

    // Test Case 3: Categories
    private void testCategories() {
        Log.d(TAG, "\n[TEST 3] Fetching Categories");

        MealClient.getInstance().getAllCategories(new CategoryCallback() {
            @Override
            public void onSuccess(List<Category> categories) {
                if (categories != null && !categories.isEmpty()) {
                    Log.d(TAG, "3-SUCCESS: Received " + categories.size() + " categories");
                    logCategoryDetails(categories.get(0));
                } else {
                    Log.e(TAG, "FAILED: Empty categories response");
                }
            }

            @Override
            public void onFailure(String errorMsg) {
                Log.e(TAG, "FAILED: " + errorMsg);
            }
        });
    }

    // Test Case 4: Areas
    private void testAreas() {
        Log.d(TAG, "\n[TEST 4] Fetching Areas");

        MealClient.getInstance().getAllAreas(new AreaCallback() {
            @Override
            public void onSuccess(List<Area> areas) {
                if (areas != null && !areas.isEmpty()) {
                    Log.d(TAG, "4-SUCCESS: Received " + areas.size() + " areas");
                    logAreaDetails(areas.get(0));
                } else {
                    Log.e(TAG, "FAILED: Empty areas response");
                }
            }

            @Override
            public void onFailure(String errorMsg) {
                Log.e(TAG, "FAILED: " + errorMsg);
            }
        });
    }

    // Test Case 5: Ingredients
    private void testIngredients() {
        Log.d(TAG, "\n[TEST 5] Fetching Ingredients");

        MealClient.getInstance().getAllIngredients(new IngredientCallback() {
            @Override
            public void onSuccess(List<Ingredient> ingredients) {
                if (ingredients != null && !ingredients.isEmpty()) {
                    Log.d(TAG, "5-SUCCESS: Received " + ingredients.size() + " ingredients");
                    logIngredientDetails(ingredients.get(0));
                } else {
                    Log.e(TAG, "FAILED: Empty ingredients response");
                }
            }

            @Override
            public void onFailure(String errorMsg) {
                Log.e(TAG, "FAILED: " + errorMsg);
            }
        });
    }

    // Test Case 6: Filter by Category
    private void testFilterByCategory() {
        Log.d(TAG, "\n[TEST 6] Filtering Meals by Category (Seafood)");

        MealClient.getInstance().filterByCategory("Seafood", new FilteredMealCallback() {
            @Override
            public void onSuccess(List<FilteredMeal> meals) {
                if (meals != null && !meals.isEmpty()) {
                    Log.d(TAG, "6-SUCCESS: Found " + meals.size() + " seafood meals");
                    logFilteredMealDetails(meals.get(0));
                } else {
                    Log.e(TAG, "FAILED: No seafood meals found");
                }
            }

            @Override
            public void onFailure(String errorMsg) {
                Log.e(TAG, "FAILED: " + errorMsg);
            }
        });
    }

    // Test Case 7: Invalid Endpoint (This will need to be handled differently)
    private void testInvalidEndpoint() {
        Log.d(TAG, "\n[TEST 7] Testing Invalid Endpoint");
        // Since we removed the generic makeNetworkCall, this test would need to be implemented differently
        Log.d(TAG, "NOTE: Invalid endpoint testing would require a different approach with the new client");
    }

    // Helper methods for logging details
    private void logMealDetails(Meal meal) {
        Log.d(TAG, "MEAL DETAILS:");
        Log.d(TAG, "Name: " + meal.getStrMeal());
        Log.d(TAG, "Category: " + meal.getStrCategory());
        Log.d(TAG, "Area: " + meal.getStrArea());
        Log.d(TAG, "Thumbnail: " + meal.getStrMealThumb());
        if (meal.getIngredientsList() != null) {
            Log.d(TAG, "Ingredients: " + meal.getIngredientsList().size());
        }
        Log.d(TAG, "Instructions: " +
                (meal.getStrInstructions() != null ?
                        meal.getStrInstructions().substring(0, Math.min(50, meal.getStrInstructions().length())) + "..." : "null"));
    }

    private void logCategoryDetails(Category category) {
        Log.d(TAG, "CATEGORY DETAILS:");
        Log.d(TAG, "Name: " + category.getStrCategory());
        Log.d(TAG, "Description: " + category.getStrCategoryDescription());
        Log.d(TAG, "Thumbnail: " + category.getStrCategoryThumb());
    }

    private void logAreaDetails(Area area) {
        Log.d(TAG, "AREA DETAILS:");
        Log.d(TAG, "Name: " + area.getStrArea());
    }

    private void logIngredientDetails(Ingredient ingredient) {
        Log.d(TAG, "INGREDIENT DETAILS:");
        Log.d(TAG, "Name: " + ingredient.getStrIngredient());
        Log.d(TAG, "Description: " + ingredient.getStrDescription());
    }

    private void logFilteredMealDetails(FilteredMeal meal) {
        Log.d(TAG, "FILTERED MEAL DETAILS:");
        Log.d(TAG, "Name: " + meal.getStrMeal());
        Log.d(TAG, "Thumbnail: " + meal.getStrMealThumb());
        Log.d(TAG, "ID: " + meal.getIdMeal());
    }
}


