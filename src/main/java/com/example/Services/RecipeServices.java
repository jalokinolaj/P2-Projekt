package com.example.Services;
import com.example.Recipes;
import com.example.Repositories.RecipesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class RecipeServices {
    @Autowired
    private RecipesRepository recipesRepository;

    public List<Recipes> getAllRecipes() {
        return recipesRepository.findAll();
    }

    public Recipes getRecipeById(Long id) {
        return recipesRepository.findById(id).orElse(null);
    }

    public List<Recipes> getRecipesSortedByRating() {
        return recipesRepository.findAllByOrderByRatingDesc();
    }
}