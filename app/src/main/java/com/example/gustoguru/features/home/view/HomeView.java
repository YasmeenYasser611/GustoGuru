package com.example.gustoguru.features.home.view;

import com.example.gustoguru.model.pojo.Category;
import com.example.gustoguru.model.pojo.FilteredMeal;

import java.util.List;

public interface HomeView
{
    void showCategories(List<Category> categories);

    void showError(String message);
}
