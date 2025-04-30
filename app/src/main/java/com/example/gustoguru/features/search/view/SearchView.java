package com.example.gustoguru.features.search.view;

import com.example.gustoguru.model.pojo.Area;
import com.example.gustoguru.model.pojo.Category;
import com.example.gustoguru.model.pojo.FilteredMeal;
import com.example.gustoguru.model.pojo.Ingredient;

import java.util.List;

public interface SearchView {
    void showLoading();
    void hideLoading();
    void showSearchResults(List<FilteredMeal> meals);
    void showCategories(List<Category> categories);
    void showIngredients(List<Ingredient> ingredients);
    void showAreas(List<Area> areas);
    void showSuggestions(List<String> suggestions);
    void showError(String message);
}
