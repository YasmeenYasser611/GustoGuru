package com.example.gustoguru.model.remote.retrofit.client;

import com.example.gustoguru.model.remote.retrofit.callback.AreaCallback;
import com.example.gustoguru.model.remote.retrofit.callback.CategoryCallback;
import com.example.gustoguru.model.remote.retrofit.callback.FilteredMealCallback;
import com.example.gustoguru.model.remote.retrofit.callback.IngredientCallback;
import com.example.gustoguru.model.remote.retrofit.callback.MealCallback;
import com.example.gustoguru.model.remote.retrofit.response.AreasResponse;
import com.example.gustoguru.model.remote.retrofit.response.CategoriesResponse;
import com.example.gustoguru.model.remote.retrofit.response.FilteredMealsResponse;
import com.example.gustoguru.model.remote.retrofit.response.IngredientsResponse;
import com.example.gustoguru.model.remote.retrofit.response.MealResponse;
import com.example.gustoguru.model.remote.retrofit.service.MealService;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MealClient {
    private static final String TAG = "MealClient";
    private static final String BASE_URL = "https://www.themealdb.com/api/json/v1/1/";
    private static final int TIMEOUT_SECONDS = 30;

    private static MealClient instance;
    private final MealService mealService;

    private MealClient() {


        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        mealService = retrofit.create(MealService.class);
    }

    public static synchronized MealClient getInstance() {
        if (instance == null) {
            instance = new MealClient();
        }
        return instance;
    }

    // Random Meal
    public void getRandomMeal(MealCallback callback) {
        mealService.getRandomMeal().enqueue(new Callback<MealResponse>() {
            @Override
            public void onResponse(Call<MealResponse> call, Response<MealResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getMeals() != null) {
                    callback.onSuccess(response.body().getMeals());
                } else {
                    callback.onFailure("Failed to get random meal: " + getErrorMessage(response));
                }
            }

            @Override
            public void onFailure(Call<MealResponse> call, Throwable t) {
                callback.onFailure("Network error: " + t.getMessage());
            }
        });
    }

    // Search Meals by Name
    public void searchMealsByName(String query, MealCallback callback) {
        mealService.searchMealsByName(query).enqueue(new Callback<MealResponse>() {
            @Override
            public void onResponse(Call<MealResponse> call, Response<MealResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getMeals() != null) {
                    callback.onSuccess(response.body().getMeals());
                } else {
                    callback.onFailure("No meals found for: " + query);
                }
            }

            @Override
            public void onFailure(Call<MealResponse> call, Throwable t) {
                callback.onFailure("Search failed: " + t.getMessage());
            }
        });
    }

    // Get Meal by ID
    public void getMealById(String mealId, MealCallback callback) {
        mealService.getMealById(mealId).enqueue(new Callback<MealResponse>() {
            @Override
            public void onResponse(Call<MealResponse> call, Response<MealResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getMeals() != null) {
                    callback.onSuccess(response.body().getMeals());
                } else {
                    callback.onFailure("Meal not found with ID: " + mealId);
                }
            }

            @Override
            public void onFailure(Call<MealResponse> call, Throwable t) {
                callback.onFailure("Failed to get meal: " + t.getMessage());
            }
        });
    }

    // Filter Meals
    public void filterByCategory(String category, FilteredMealCallback callback) {
        mealService.filterByCategory(category).enqueue(new Callback<FilteredMealsResponse>() {
            @Override
            public void onResponse(Call<FilteredMealsResponse> call, Response<FilteredMealsResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getMeals() != null) {
                    callback.onSuccess(response.body().getMeals());
                } else {
                    callback.onFailure("No meals found in category: " + category);
                }
            }

            @Override
            public void onFailure(Call<FilteredMealsResponse> call, Throwable t) {
                callback.onFailure("Filter failed: " + t.getMessage());
            }
        });
    }

    // Categories
    public void getAllCategories(CategoryCallback callback) {
        mealService.getAllCategories().enqueue(new Callback<CategoriesResponse>() {
            @Override
            public void onResponse(Call<CategoriesResponse> call, Response<CategoriesResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getCategories() != null) {
                    callback.onSuccess(response.body().getCategories());
                } else {
                    callback.onFailure("Failed to get categories");
                }
            }

            @Override
            public void onFailure(Call<CategoriesResponse> call, Throwable t) {
                callback.onFailure("Categories fetch failed: " + t.getMessage());
            }
        });
    }

    // Areas
    public void getAllAreas(AreaCallback callback) {
        mealService.getAllAreas().enqueue(new Callback<AreasResponse>() {
            @Override
            public void onResponse(Call<AreasResponse> call, Response<AreasResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getAreas() != null) {
                    callback.onSuccess(response.body().getAreas());
                } else {
                    callback.onFailure("Failed to get areas");
                }
            }

            @Override
            public void onFailure(Call<AreasResponse> call, Throwable t) {
                callback.onFailure("Areas fetch failed: " + t.getMessage());
            }
        });
    }

    // Ingredients
    public void getAllIngredients(IngredientCallback callback) {
        mealService.getAllIngredients().enqueue(new Callback<IngredientsResponse>() {
            @Override
            public void onResponse(Call<IngredientsResponse> call, Response<IngredientsResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getIngredients() != null) {
                    callback.onSuccess(response.body().getIngredients());
                } else {
                    callback.onFailure("Failed to get ingredients");
                }
            }

            @Override
            public void onFailure(Call<IngredientsResponse> call, Throwable t) {
                callback.onFailure("Ingredients fetch failed: " + t.getMessage());
            }
        });
    }

    // Helper method for error messages
    private String getErrorMessage(Response<?> response) {
        if (response.errorBody() != null) {
            try {
                return response.errorBody().string();
            } catch (Exception e) {
                return "Error code: " + response.code();
            }
        }
        return "Error code: " + response.code();
    }
}