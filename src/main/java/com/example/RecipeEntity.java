package com.example;

import jakarta.persistence.*;

@Entity
@Table(name = "recipes")
public class RecipeEntity {

    @Id
    private Integer id;

    @Column(name = "recipe_name")
    private String recipeName;

    @Column(name = "total_time")
    private String totalTime;

    @Column(name = "cook_time")
    private String cookTime;

    private String servings;

    @Column(columnDefinition = "TEXT")
    private String ingredients;

    @Column(name = "cuisine_path")
    private String cuisinePath;

    @Column(name = "img_src")
    private String imgSrc;

    public Integer getId() { return id; }
    public String getRecipeName() { return recipeName; }
    public String getTotalTime() { return totalTime; }
    public String getCookTime() { return cookTime; }
    public String getServings() { return servings; }
    public String getIngredients() { return ingredients; }
    public String getCuisinePath() { return cuisinePath; }
    public String getImgSrc() { return imgSrc; }
}
