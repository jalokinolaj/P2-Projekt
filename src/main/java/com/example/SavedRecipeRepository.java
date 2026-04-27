package com.example;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SavedRecipeRepository extends JpaRepository<SavedRecipeEntity, Integer> {
    List<SavedRecipeEntity> findByUsername(String username);
    boolean existsByUsernameAndRecipeId(String username, Integer recipeId);
    void deleteByUsernameAndRecipeId(String username, Integer recipeId);
}