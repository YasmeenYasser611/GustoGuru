package com.example.gustoguru.model.remote.retrofit.callback;

import com.example.gustoguru.model.pojo.Area;

import java.util.List;

public interface AreaCallback {
    void onSuccess(List<Area> areas);
    void onFailure(String error);
}
