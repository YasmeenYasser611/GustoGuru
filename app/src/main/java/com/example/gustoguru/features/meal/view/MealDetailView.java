package com.example.gustoguru.features.meal.view;

import com.example.gustoguru.model.pojo.Meal;

import java.util.Map;

public interface MealDetailView {
    void showLoading();
    void hideLoading();

    void showMealDetails(Meal meal);
    void showIngredients(Map<String, String> ingredientMeasureMap);
    void showInstructions(String instructions);
    void showYoutubeVideo(String videoUrl);

    void showFavoriteStatus(boolean isFavorite);
    void showError(String message);

    void navigateBack();
}