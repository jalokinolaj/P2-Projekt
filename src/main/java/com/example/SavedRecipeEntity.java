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
	
	private String username;
	
	@ManyToOne
	@JoinColumn(name = "recipe_id")
	private RecipeEntity recipe;
	
	public Integer getId() {return id;}
	public String getUsername() {return username;}
	public void setUsername(String username) { this.username = username; }
    public RecipeEntity getRecipe() { return recipe; }
    public void setRecipe(RecipeEntity recipe) { this.recipe = recipe; }
}
	


