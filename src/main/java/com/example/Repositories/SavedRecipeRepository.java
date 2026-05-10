package com.example.Repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import com.example.SavedRecipeEntity;

public interface SavedRecipeRepository extends JpaRepository<SavedRecipeEntity, Integer> {
    List<SavedRecipeEntity> findByUsername(String username);
    boolean existsByUsernameAndRecipeId(String username, Integer recipeId);

    @Transactional
    void deleteByUsernameAndRecipeId(String username, Integer recipeId);
}