package com.example.gustoguru.model.local;


import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.gustoguru.model.pojo.Meal;

import java.util.List;

@Dao
public interface FavoriteMealDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertFavorite(Meal meal);

    @Delete
    void deleteFavorite(Meal meal);

    @Query("SELECT * FROM meal WHERE isFavorite = 1")
    LiveData<List<Meal>> getAllFavorites();

    @Query("SELECT isFavorite FROM meal WHERE idMeal = :mealId")
    boolean isFavorite(String mealId);

    @Query("UPDATE meal SET isFavorite = :isFavorite WHERE idMeal = :mealId")
    void updateFavoriteStatus(String mealId, boolean isFavorite);

    @Query("SELECT COUNT(*) FROM meal WHERE idMeal = :mealId")
    int mealExists(String mealId);


}