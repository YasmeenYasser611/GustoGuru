package com.example.gustoguru.model.local;


import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.gustoguru.model.pojo.Meal;

import java.util.List;

@Dao
public interface PlannedMealDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertPlannedMeal(Meal meal);

    @Query("SELECT * FROM meal WHERE plannedDate IS NOT NULL")
    LiveData<List<Meal>> getAllPlannedMeals();
}