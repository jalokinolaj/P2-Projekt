package com.example;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Column;

@Entity
@Table(name = "saved_recipes")
public class SavedRecipeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Links to app_user.id stable even if the username changes
    @Column(name = "user_id")
    private Long userId;

    @ManyToOne
    @JoinColumn(name = "recipe_id")
    private RecipeEntity recipe;

    public Integer getId() { return id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public RecipeEntity getRecipe() { return recipe; }
    public void setRecipe(RecipeEntity recipe) { this.recipe = recipe; }
}
