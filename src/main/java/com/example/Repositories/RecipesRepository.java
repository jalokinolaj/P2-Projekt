package com.example.Repositories;
import com.example.Recipes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface RecipesRepository extends JpaRepository<Recipes, Long> {
    List<Recipes> findAllByOrderByRatingDesc();
    List<Recipes> findByRecipeNameContainingIgnoreCase(String name);
}