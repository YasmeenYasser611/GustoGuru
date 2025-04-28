package com.example.gustoguru.model.pojo;


public class FilteredMeal
{
    private String idMeal;
    private String strMeal;

    public FilteredMeal(String idMeal, String strMeal, String strMealThumb) {
        this.idMeal = idMeal;
        this.strMeal = strMeal;
        this.strMealThumb = strMealThumb;
    }

    private String strMealThumb;



    // Getters
    public String getIdMeal() { return idMeal; }
    public String getStrMeal() { return strMeal; }
    public String getStrMealThumb() { return strMealThumb; }

    // Setters
    public void setIdMeal(String idMeal) { this.idMeal = idMeal; }
    public void setStrMeal(String strMeal) { this.strMeal = strMeal; }
    public void setStrMealThumb(String strMealThumb) { this.strMealThumb = strMealThumb; }
}