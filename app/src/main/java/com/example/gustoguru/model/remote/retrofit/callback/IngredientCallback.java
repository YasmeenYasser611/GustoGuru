package com.example.gustoguru.model.remote.retrofit.callback;

import com.example.gustoguru.model.pojo.Ingredient;

import java.util.List;

public interface IngredientCallback {
    void onSuccess(List<Ingredient> ingredients);
    void onFailure(String error);
}
