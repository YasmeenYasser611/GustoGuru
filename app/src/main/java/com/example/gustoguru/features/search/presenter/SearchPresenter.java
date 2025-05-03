package com.example.gustoguru.features.search.presenter;

import android.util.Log;

import com.example.gustoguru.features.search.view.SearchView;
import com.example.gustoguru.features.weekly_planner.view.PlannedView;
import com.example.gustoguru.model.pojo.Area;
import com.example.gustoguru.model.pojo.Category;
import com.example.gustoguru.model.pojo.FilteredMeal;
import com.example.gustoguru.model.pojo.Ingredient;
import com.example.gustoguru.model.pojo.Meal;
import com.example.gustoguru.model.remote.retrofit.callback.AreaCallback;
import com.example.gustoguru.model.remote.retrofit.callback.CategoryCallback;
import com.example.gustoguru.model.remote.retrofit.callback.FilteredMealCallback;
import com.example.gustoguru.model.remote.retrofit.callback.IngredientCallback;
import com.example.gustoguru.model.remote.retrofit.callback.MealCallback;
import com.example.gustoguru.model.repository.MealRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
public class SearchPresenter {
    private SearchView view;
    private final MealRepository repository;
    private List<Category> allCategories = new ArrayList<>();
    private List<Ingredient> allIngredients = new ArrayList<>();
    private List<Area> allAreas = new ArrayList<>();

    public SearchPresenter(SearchView view, MealRepository repository) {
        this.view = view;
        this.repository = repository;
    }

    public void performSearch(String query, String searchMethod) {
        if (query.isEmpty()) {
            view.showError("Please enter a search term");
            return;
        }

        switch (searchMethod) {
            case "Name":
                searchByName(query);
                break;
            case "Category":
                searchByCategory(query);
                break;
            case "Ingredient":
                searchByIngredient(query);
                break;
            case "Country":
                searchByArea(query);
                break;
            default:
                searchByName(query);
                break;
        }
    }

    public void filterSuggestions(String query, String method) {
        if (query.isEmpty()) {
            view.showSuggestions(Collections.emptyList());
            return;
        }

        List<String> filtered = new ArrayList<>();
        String queryLower = query.toLowerCase();

        switch (method) {
            case "Category":
                for (Category c : allCategories) {
                    if (c.getStrCategory().toLowerCase().contains(queryLower)) {
                        filtered.add(c.getStrCategory());
                    }
                }
                break;
            case "Ingredient":
                for (Ingredient i : allIngredients) {
                    if (i.getStrIngredient().toLowerCase().contains(queryLower)) {
                        filtered.add(i.getStrIngredient());
                    }
                }
                break;
            case "Country":
                for (Area a : allAreas) {
                    if (a.getStrArea().toLowerCase().contains(queryLower)) {
                        filtered.add(a.getStrArea());
                    }
                }
                break;
            case "Name":
                // No suggestions for name search
                break;
        }

        view.showSuggestions(filtered);
    }

    public void setCategories(List<Category> categories) {
        this.allCategories = categories;
    }

    public void setIngredients(List<Ingredient> ingredients) {
        this.allIngredients = ingredients;
    }

    public void setAreas(List<Area> areas) {
        this.allAreas = areas;
    }
    private void searchByName(String query) {
        view.showLoading();
        repository.searchMealsByName(query, new MealCallback() {
            @Override
            public void onSuccess(List<Meal> meals) {
                view.hideLoading();
                if (meals == null || meals.isEmpty()) {  // Handle null case
                    view.showError("No meals found");
                } else {
                    List<FilteredMeal> filteredMeals = new ArrayList<>();
                    for (Meal meal : meals) {
                        if (meal != null) {  // Additional null check for individual meals
                            filteredMeals.add(new FilteredMeal(
                                    meal.getIdMeal(),
                                    meal.getStrMeal(),
                                    meal.getStrMealThumb()
                            ));
                        }
                    }
                    view.showSearchResults(filteredMeals.isEmpty() ? null : filteredMeals);
                }
            }

            @Override
            public void onFailure(String message) {
                view.hideLoading();
                view.showError(message);
            }
        });
    }

    private void searchByCategory(String category) {
        view.showLoading();
        repository.filterByCategory(category, new FilteredMealCallback() {
            @Override
            public void onSuccess(List<FilteredMeal> meals) {
                view.hideLoading();
                view.showSearchResults(meals == null || meals.isEmpty() ? null : meals);
            }
            @Override
            public void onFailure(String message) {
                view.hideLoading();
                view.showError(message);
            }
        });
    }

    private void searchByIngredient(String ingredient) {
        view.showLoading();
        repository.filterByIngredient(ingredient, new FilteredMealCallback() {
            @Override
            public void onSuccess(List<FilteredMeal> meals) {
                view.hideLoading();
                view.showSearchResults(meals == null || meals.isEmpty() ? null : meals);
            }
            @Override
            public void onFailure(String message) {
                view.hideLoading();
                view.showError(message);
            }
        });
    }
    private void searchByArea(String area) {
        view.showLoading();
        repository.filterByArea(area, new FilteredMealCallback() {
            @Override
            public void onSuccess(List<FilteredMeal> meals) {
                view.hideLoading();
                view.showSearchResults(meals == null || meals.isEmpty() ? null : meals);
            }
            @Override
            public void onFailure(String message) {
                view.hideLoading();
                view.showError(message);
            }
        });
    }

    public void loadCategories() {
        repository.getAllCategories(new CategoryCallback() {
            @Override
            public void onSuccess(List<Category> categories) {
                view.showCategories(categories);
            }
            @Override
            public void onFailure(String message) {
                view.showError("Failed to load categories: " + message);
            }
        });
    }

    public void loadIngredients() {
        repository.getAllIngredients(new IngredientCallback() {
            @Override
            public void onSuccess(List<Ingredient> ingredients) {
                view.showIngredients(ingredients);
            }
            @Override
            public void onFailure(String message) {
                view.showError("Failed to load ingredients: " + message);
            }
        });
    }

    public void loadAreas() {
        repository.getAllAreas(new AreaCallback() {
            @Override
            public void onSuccess(List<Area> areas) {
                view.showAreas(areas);
            }
            @Override
            public void onFailure(String message) {
                view.showError("Failed to load areas: " + message);
            }
        });
    }

    public void attachView(SearchView view) {
        this.view = view;
    }

    public void detachView() {
        this.view = null;
    }
}