package com.example.gustoguru.model.remote.firebase;

import android.util.Log;

import com.example.gustoguru.model.pojo.Meal;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirebaseSyncHelper {
    private final FirebaseFirestore db;
    private final String currentUserId;

    public FirebaseSyncHelper(String userId) {
        this.db = FirebaseFirestore.getInstance();
        this.currentUserId = userId;
    }

    // Sync single favorite meal to Firebase
    public void syncFavoriteToFirebase(Meal meal) {
        if (meal == null || currentUserId == null) return;

        // Create a simplified version for Firebase
        Map<String, Object> favMeal = new HashMap<>();
        favMeal.put("idMeal", meal.getIdMeal());
        favMeal.put("strMeal", meal.getStrMeal());
        favMeal.put("strMealThumb", meal.getStrMealThumb());
        favMeal.put("userId", currentUserId);
        favMeal.put("lastUpdated", System.currentTimeMillis());

        db.collection("userFavorites")
                .document(currentUserId + "_" + meal.getIdMeal())
                .set(favMeal)
                .addOnSuccessListener(aVoid -> Log.d("SYNC", "Favorite synced to Firebase"))
                .addOnFailureListener(e -> Log.e("SYNC", "Error syncing favorite", e));
    }

    // Remove favorite from Firebase
    public void removeFavoriteFromFirebase(String mealId) {
        if (mealId == null || currentUserId == null) return;

        db.collection("userFavorites")
                .document(currentUserId + "_" + mealId)
                .delete()
                .addOnSuccessListener(aVoid -> Log.d("SYNC", "Favorite removed from Firebase"))
                .addOnFailureListener(e -> Log.e("SYNC", "Error removing favorite", e));
    }

    // Sync planned meal to Firebase
    public void syncPlannedMealToFirebase(Meal meal) {
        if (meal == null || currentUserId == null) return;

        Map<String, Object> plannedMeal = new HashMap<>();
        plannedMeal.put("idMeal", meal.getIdMeal());
        plannedMeal.put("strMeal", meal.getStrMeal());
        plannedMeal.put("strMealThumb", meal.getStrMealThumb());
        plannedMeal.put("plannedDate", meal.getPlannedDate());
        plannedMeal.put("userId", currentUserId);
        plannedMeal.put("lastUpdated", System.currentTimeMillis());

        db.collection("userPlannedMeals")
                .document(currentUserId + "_" + meal.getIdMeal() + "_" + meal.getPlannedDate())
                .set(plannedMeal)
                .addOnSuccessListener(aVoid -> Log.d("SYNC", "Planned meal synced to Firebase"))
                .addOnFailureListener(e -> Log.e("SYNC", "Error syncing planned meal", e));
    }

    // Download user's favorites from Firebase
    public void downloadUserFavorites(FirebaseCallback<List<Meal>> callback) {
        if (currentUserId == null) {
            callback.onFailure(new Exception("User not authenticated"));
            return;
        }

        db.collection("userFavorites")
                .whereEqualTo("userId", currentUserId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Meal> meals = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot) {
                        Meal meal = new Meal();
                        meal.setIdMeal(doc.getString("idMeal"));
                        meal.setStrMeal(doc.getString("strMeal"));
                        meal.setStrMealThumb(doc.getString("strMealThumb"));
                        meal.setUserId(currentUserId);
                        meal.setFavorite(true);
                        meal.setLastSyncTimestamp(doc.getLong("lastUpdated"));
                        meals.add(meal);
                    }
                    callback.onSuccess(meals);
                })
                .addOnFailureListener(callback::onFailure);
    }

    public void downloadUserPlannedMeals(FirebaseCallback<List<Meal>> callback) {
        if (currentUserId == null) {
            callback.onFailure(new Exception("User not authenticated"));
            return;
        }

        db.collection("userPlannedMeals")
                .whereEqualTo("userId", currentUserId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Meal> meals = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot) {
                        Meal meal = new Meal();
                        meal.setIdMeal(doc.getString("idMeal"));
                        meal.setStrMeal(doc.getString("strMeal"));
                        meal.setStrMealThumb(doc.getString("strMealThumb"));
                        meal.setPlannedDate(doc.getString("plannedDate"));
                        meal.setUserId(currentUserId);
                        meal.setLastSyncTimestamp(doc.getLong("lastUpdated"));
                        meals.add(meal);
                    }
                    callback.onSuccess(meals);
                })
                .addOnFailureListener(callback::onFailure);
    }

    public void removePlannedMealFromFirebase(String mealId, String date) {
        if (mealId == null || date == null || currentUserId == null) return;

        db.collection("userPlannedMeals")
                .document(currentUserId + "_" + mealId + "_" + date)
                .delete()
                .addOnSuccessListener(aVoid -> Log.d("SYNC", "Planned meal removed from Firebase"))
                .addOnFailureListener(e -> Log.e("SYNC", "Error removing planned meal", e));
    }

    public interface FirebaseCallback<T> {
        void onSuccess(T result);
        void onFailure(Exception e);
    }
}
