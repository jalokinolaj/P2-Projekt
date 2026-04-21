package com.example;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface RecipeRepository extends JpaRepository<RecipeEntity, Integer> {

    @Query(value = "SELECT * FROM recipes WHERE LOWER(ingredients) LIKE LOWER(CONCAT('%', :ingredient, '%'))", nativeQuery = true)
    List<RecipeEntity> findByIngredient(@Param("ingredient") String ingredient);
}
