package com.example.gustoguru.model.remote.retrofit.callback;

import com.example.gustoguru.model.pojo.FilteredMeal;

import java.util.List;

public interface FilteredMealCallback {
    void onSuccess(List<FilteredMeal> meals);
    void onFailure(String error);
}
