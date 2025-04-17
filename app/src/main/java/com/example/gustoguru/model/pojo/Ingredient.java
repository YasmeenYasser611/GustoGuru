package com.example.gustoguru.model.pojo;



public class Ingredient {
    private String idIngredient;
    private String strIngredient;
    private String strDescription;
    private String strType;
    private String strImageUrl; // Constructed from name

    // Getters
    public String getIdIngredient() { return idIngredient; }
    public String getStrIngredient() { return strIngredient; }
    public String getStrDescription() { return strDescription; }
    public String getStrType() { return strType; }
    public String getStrImageUrl() {
        return "https://www.themealdb.com/images/ingredients/" + strIngredient + "-Small.png";
    }

    // Setters
    public void setIdIngredient(String idIngredient) { this.idIngredient = idIngredient; }
    public void setStrIngredient(String strIngredient) { this.strIngredient = strIngredient; }
    public void setStrDescription(String strDescription) { this.strDescription = strDescription; }
    public void setStrType(String strType) { this.strType = strType; }
}