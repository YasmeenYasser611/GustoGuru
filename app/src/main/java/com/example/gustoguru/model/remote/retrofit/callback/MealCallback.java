package com.example.gustoguru.model.remote.retrofit.callback;

import com.example.gustoguru.model.pojo.Meal;

import java.util.List;

public interface MealCallback {
    void onSuccess(List<Meal> meals);
    void onFailure(String error);
}