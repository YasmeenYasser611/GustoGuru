package com.example.gustoguru.model.remote.retrofit.callback;

import com.example.gustoguru.model.pojo.Category;

import java.util.List;

public interface CategoryCallback {
    void onSuccess(List<Category> categories);
    void onFailure(String error);
}
