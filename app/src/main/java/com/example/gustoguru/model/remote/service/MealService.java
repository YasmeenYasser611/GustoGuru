package com.example.gustoguru.model.remote.service;

import com.example.gustoguru.model.remote.response.AreasResponse;
import com.example.gustoguru.model.remote.response.CategoriesResponse;
import com.example.gustoguru.model.remote.response.FilteredMealsResponse;
import com.example.gustoguru.model.remote.response.IngredientsResponse;
import com.example.gustoguru.model.remote.response.MealResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface MealService {
    // Random meal
    @GET("random.php")
    Call<MealResponse> getRandomMeal();

    // Lookup by ID
    @GET("lookup.php")
    Call<MealResponse> getMealById(@Query("i") String mealId);

    // Search by name
    @GET("search.php")
    Call<MealResponse> searchMealsByName(@Query("s") String query);

    // Filter by category/area/ingredient
    @GET("filter.php")
    Call<FilteredMealsResponse> filterByCategory(@Query("c") String category);

    @GET("filter.php")
    Call<FilteredMealsResponse> filterByArea(@Query("a") String area);

    @GET("filter.php")
    Call<FilteredMealsResponse> filterByIngredient(@Query("i") String ingredient);

    // Lists (categories/areas/ingredients)
    @GET("categories.php")
    Call<CategoriesResponse> getAllCategories();

    @GET("list.php?a=list")
    Call<AreasResponse> getAllAreas();

    @GET("list.php?i=list")
    Call<IngredientsResponse> getAllIngredients();
}