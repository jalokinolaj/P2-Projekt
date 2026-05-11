package com.example.Repositories;
import com.example.RecipeEntity;
import static org.assertj.core.api.Assertions.assertThat;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
/**
 * Integration test for RecipeRepository.
 *
 * Tests:
 * 1. Database connection works
 * 2. Recipes can be loaded from database
 * 3. Ingredient filtering works correctly
 */
@DataJpaTest
@Sql("/test-recipes.sql") 
public class RecipeRepositoryIntegrationTest {

    @Autowired
    private RecipeRepository recipeRepository;

    /**
     * Tests that recipes can be fetched from the database.
     */
    @Test
    void shouldLoadRecipesFromDatabase() {

        List<RecipeEntity> recipes = recipeRepository.findAll();

        assertThat(recipes).isNotNull();
        assertThat(recipes.size()).isGreaterThan(0);
    }

    /**
     * Tests that ingredient filtering works.
     */
    @Test
    void shouldFindRecipesContainingChicken() {

        List<RecipeEntity> recipes = recipeRepository.findByIngredient("chicken");

        assertThat(recipes).isNotNull();

        assertThat(recipes.stream()
                .allMatch(recipe ->
                        recipe.getIngredients() != null &&
                        recipe.getIngredients().toLowerCase().contains("chicken")))
                .isTrue();
    }

    /**
     * Tests that recipe names exist.
     */
    @Test
    void recipesShouldHaveNames() {

        List<RecipeEntity> recipes = recipeRepository.findAll();

        assertThat(recipes.stream()
                .allMatch(recipe ->
                        recipe.getRecipeName() != null &&
                        !recipe.getRecipeName().isBlank()))
                .isTrue();
    }
}