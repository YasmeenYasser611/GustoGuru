package com.example.gustoguru.features.home.view;

import com.example.gustoguru.model.pojo.Category;
import com.example.gustoguru.model.pojo.FilteredMeal;
import com.example.gustoguru.model.pojo.Meal;

import java.util.List;

public interface HomeView {
    void showCategories(List<Category> categories);

    void showError(String message);

    void showMealOfTheDay(Meal meal);
    public void showSearchResults(List<FilteredMeal> meals);



}
