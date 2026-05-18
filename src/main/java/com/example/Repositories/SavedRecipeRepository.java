package com.example.Repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import com.example.SavedRecipeEntity;

public interface SavedRecipeRepository extends JpaRepository<SavedRecipeEntity, Integer> {
    List<SavedRecipeEntity> findByUserId(Long userId);
    boolean existsByUserIdAndRecipeId(Long userId, Integer recipeId);

    @Transactional
    void deleteByUserIdAndRecipeId(Long userId, Integer recipeId);
}
