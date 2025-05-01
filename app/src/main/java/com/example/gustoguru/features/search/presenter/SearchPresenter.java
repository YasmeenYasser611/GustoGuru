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

    public void performSearch(String query) {
        if (query.isEmpty()) {
            view.showError("Please enter a search term");
            return;
        }

        // Auto-detect search type
        for (Category c : allCategories) {
            if (c.getStrCategory().equalsIgnoreCase(query)) {
                searchByCategory(query);
                return;
            }
        }

        for (Area a : allAreas) {
            if (a.getStrArea().equalsIgnoreCase(query)) {
                searchByArea(query);
                return;
            }
        }

        for (Ingredient i : allIngredients) {
            if (i.getStrIngredient().equalsIgnoreCase(query)) {
                searchByIngredient(query);
                return;
            }
        }

        // Default to name search
        searchByName(query);
    }

    public void filterSuggestions(String query, String method)
    {
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
            case "Area":
                for (Area a : allAreas) {
                    if (a.getStrArea().toLowerCase().contains(queryLower)) {
                        filtered.add(a.getStrArea());
                    }
                }
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

    public void searchByName(String query) {
        Log.d("SearchDebug", "Presenter.searchByName called with: " + query);

        if (query.isEmpty()) {
            view.showError("Please enter a search term");
            return;
        }

        view.showLoading();
        repository.searchMealsByName(query, new MealCallback() {
            @Override
            public void onSuccess(List<Meal> meals) {
                Log.d("SearchDebug", "Repository returned " + meals.size() + " meals");
                view.hideLoading();
                if (meals.isEmpty()) {
                    view.showError("No meals found");
                } else {
                    List<FilteredMeal> filteredMeals = new ArrayList<>();
                    for (Meal meal : meals) {
                        filteredMeals.add(new FilteredMeal(
                                meal.getIdMeal(),
                                meal.getStrMeal(),
                                meal.getStrMealThumb()
                        ));
                    }
                    view.showSearchResults(filteredMeals);
                }
            }

            @Override
            public void onFailure(String message) {
                Log.e("SearchDebug", "Search failed: " + message);
                view.hideLoading();
                view.showError(message);
            }
        });
    }

    public void searchByCategory(String category) {
        view.showLoading();
        repository.filterByCategory(category, new FilteredMealCallback() {
            @Override
            public void onSuccess(List<FilteredMeal> meals) {
                view.hideLoading();
                view.showSearchResults(meals);
            }
            @Override
            public void onFailure(String message) {
                view.hideLoading();
                view.showError(message);
            }
        });
    }

    public void searchByIngredient(String ingredient) {
        view.showLoading();
        repository.filterByIngredient(ingredient, new FilteredMealCallback() {
            @Override
            public void onSuccess(List<FilteredMeal> meals) {
                view.hideLoading();
                view.showSearchResults(meals);
            }
            @Override
            public void onFailure(String message) {
                view.hideLoading();
                view.showError(message);
            }
        });
    }

    public void searchByArea(String area) {
        view.showLoading();
        repository.filterByArea(area, new FilteredMealCallback() {
            @Override
            public void onSuccess(List<FilteredMeal> meals) {
                view.hideLoading();
                view.showSearchResults(meals);
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