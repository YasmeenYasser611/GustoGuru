package com.example.gustoguru.model.local;



import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.gustoguru.model.local.FavoriteMealDao;
import com.example.gustoguru.model.local.PlannedMealDao;
import com.example.gustoguru.model.pojo.Meal;


    @Database(entities = {Meal.class}, version = 1)
    public abstract class AppDatabase extends RoomDatabase {
        public abstract FavoriteMealDao favoriteMealDao();
        public abstract PlannedMealDao plannedMealDao();

        private static volatile AppDatabase INSTANCE;

        public static AppDatabase getInstance(Context context) {
            if (INSTANCE == null) {
                synchronized (AppDatabase.class) {
                    if (INSTANCE == null) {
                        INSTANCE = Room.databaseBuilder(
                                context.getApplicationContext(),
                                AppDatabase.class,
                                "meal_database"
                        ).build();
                    }
                }
            }
            return INSTANCE;
        }
    }