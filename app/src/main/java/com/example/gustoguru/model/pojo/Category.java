package com.example.gustoguru.model.pojo;


public class Category {
    private String idCategory;
    private String strCategory;
    private String strCategoryThumb;
    private String strCategoryDescription;

    // Getters
    public String getIdCategory() { return idCategory; }
    public String getStrCategory() { return strCategory; }
    public String getStrCategoryThumb() { return strCategoryThumb; }
    public String getStrCategoryDescription() { return strCategoryDescription; }

    // Setters
    public void setIdCategory(String idCategory) { this.idCategory = idCategory; }
    public void setStrCategory(String strCategory) { this.strCategory = strCategory; }
    public void setStrCategoryThumb(String strCategoryThumb) { this.strCategoryThumb = strCategoryThumb; }
    public void setStrCategoryDescription(String strCategoryDescription) {
        this.strCategoryDescription = strCategoryDescription;
    }
}
