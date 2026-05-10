package com.example.Repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.RecipeEntity;

public interface RecipeRepository extends JpaRepository<RecipeEntity, Integer> {

    @Query(value = "SELECT * FROM recipes WHERE LOWER(ingredients) LIKE LOWER(CONCAT('%', :ingredient, '%'))", nativeQuery = true)
    List<RecipeEntity> findByIngredient(@Param("ingredient") String ingredient);
}
