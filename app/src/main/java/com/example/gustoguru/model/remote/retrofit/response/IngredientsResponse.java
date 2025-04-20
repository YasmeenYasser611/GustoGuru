package com.example.gustoguru.model.remote.retrofit.response;



import com.example.gustoguru.model.pojo.Ingredient;

import java.util.List;

public class IngredientsResponse {
    private List<Ingredient> meals;

    public List<Ingredient> getIngredients() {
        return meals;
    }


}
