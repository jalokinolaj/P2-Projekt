package com.example.Services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.Inventory;
import com.example.RecipeIngredient;
import com.example.Recipes;
import com.example.Repositories.RecipeIngredientRepository;
import com.example.Repositories.RecipesRepository;

@ExtendWith(MockitoExtension.class)
class RecipeServicesTest {

    @Mock
    private RecipesRepository recipesRepository;

    @Mock
    private RecipeIngredientRepository recipeIngredientRepository;

    @Mock
    private InventoryServices inventoryServices;

    @Spy
    private UnitConverterService unitConverterService = new UnitConverterService();

    @InjectMocks
    private RecipeServices recipeServices;

    @Test
    void getAllRecipesDelegatesToRepository() {
        List<Recipes> expected = List.of(new Recipes("A", null, "4.0"));
        when(recipesRepository.findAll()).thenReturn(expected);

        assertEquals(expected, recipeServices.getAllRecipes());
    }

    @Test
    void getRecipeByIdReturnsRecipeWhenFound() {
        Recipes recipe = new Recipes("Soup", null, "4.2");
        when(recipesRepository.findById(1L)).thenReturn(Optional.of(recipe));

        assertEquals(recipe, recipeServices.getRecipeById(1L));
    }

    @Test
    void getRecipeByIdReturnsNullWhenMissing() {
        when(recipesRepository.findById(2L)).thenReturn(Optional.empty());

        assertNull(recipeServices.getRecipeById(2L));
    }

    @Test
    void getRecipesSortedByRatingDelegatesToRepository() {
        List<Recipes> expected = List.of(new Recipes("A", null, "4.0"));
        when(recipesRepository.findAllByOrderByRatingDesc()).thenReturn(expected);

        assertEquals(expected, recipeServices.getRecipesSortedByRating());
    }

    @Test
    void searchRecipesByNameDelegatesToRepository() {
        List<Recipes> expected = List.of(new Recipes("Apple Pie", null, "4.8"));
        when(recipesRepository.findByRecipeNameContainingIgnoreCase("apple")).thenReturn(expected);

        assertEquals(expected, recipeServices.searchRecipesByName("apple"));
    }

    @Test
    void getRankedRecipesForUserHandlesRecipesWithoutIngredients() {
        Recipes recipe = new Recipes("Plain Water", null, "5.0");
        recipe.setId(100L);

        when(inventoryServices.getInventoryForUser(7L)).thenReturn(List.of());
        when(recipesRepository.findAll()).thenReturn(List.of(recipe));
        when(recipeIngredientRepository.findByRecipeId(100L)).thenReturn(List.of());

        List<RecipeRecommendation> result = recipeServices.getRankedRecipesForUser(7L);

        assertEquals(1, result.size());
        assertEquals(0.0, result.getFirst().matchPercent());
        assertEquals("No ingredients added for this recipe", result.getFirst().missingIngredients());
        assertEquals("-", result.getFirst().runOutFirstIngredient());
    }

    @Test
    void getRankedRecipesForUserComputesMatchMissingAndUrgency() {
        Recipes recipe = new Recipes("Omelette", null, "4.5");
        recipe.setId(101L);

        RecipeIngredient egg = new RecipeIngredient(recipe, "Egg", 2.0, "pcs");
        RecipeIngredient milk = new RecipeIngredient(recipe, "Milk", 100.0, "ml");

        Inventory eggInventory = new Inventory();
        eggInventory.setIngredientName("egg");
        eggInventory.setNormalizedQuantity(3.0);
        eggInventory.setNormalizedUnit("pcs");
        eggInventory.setMinimumQuantity(1.0);
        eggInventory.setUnit("pcs");
        eggInventory.setExpiryDate(LocalDate.now().plusDays(10));

        Inventory milkInventory = new Inventory();
        milkInventory.setIngredientName("milk");
        milkInventory.setNormalizedQuantity(50.0);
        milkInventory.setNormalizedUnit("ml");
        milkInventory.setMinimumQuantity(40.0);
        milkInventory.setUnit("ml");

        when(inventoryServices.getInventoryForUser(3L)).thenReturn(List.of(eggInventory, milkInventory));
        when(recipesRepository.findAll()).thenReturn(List.of(recipe));
        when(recipeIngredientRepository.findByRecipeId(101L)).thenReturn(List.of(egg, milk));

        List<RecipeRecommendation> result = recipeServices.getRankedRecipesForUser(3L);

        assertEquals(1, result.size());
        RecipeRecommendation recommendation = result.getFirst();
        assertEquals(50.0, recommendation.matchPercent());
        assertEquals("Milk", recommendation.missingIngredients());
        assertEquals("Milk", recommendation.runOutFirstIngredient());
        assertTrue(recommendation.urgencyScore() > 0.0);
    }

    @Test
    void getRankedRecipesForUserTreatsUnitMismatchAsMissing() {
        Recipes recipe = new Recipes("Sauce", null, "4.0");
        recipe.setId(102L);

        RecipeIngredient sugar = new RecipeIngredient(recipe, "Sugar", 10.0, "g");

        Inventory sugarInventory = new Inventory();
        sugarInventory.setIngredientName("sugar");
        sugarInventory.setNormalizedQuantity(10.0);
        sugarInventory.setNormalizedUnit("ml");
        sugarInventory.setMinimumQuantity(1.0);
        sugarInventory.setUnit("ml");

        when(inventoryServices.getInventoryForUser(4L)).thenReturn(List.of(sugarInventory));
        when(recipesRepository.findAll()).thenReturn(List.of(recipe));
        when(recipeIngredientRepository.findByRecipeId(102L)).thenReturn(List.of(sugar));

        List<RecipeRecommendation> result = recipeServices.getRankedRecipesForUser(4L);

        assertEquals(1, result.size());
        assertEquals(0.0, result.getFirst().matchPercent());
        assertEquals("Sugar", result.getFirst().missingIngredients());
    }

    @Test
    void getRankedRecipesForUserHandlesNonNumericRatingsWithoutFailing() {
        Recipes recipe = new Recipes("Soup", null, "N/A");
        recipe.setId(103L);

        RecipeIngredient ingredient = new RecipeIngredient(recipe, "Water", 100.0, "ml");

        Inventory water = new Inventory();
        water.setIngredientName("water");
        water.setNormalizedQuantity(200.0);
        water.setNormalizedUnit("ml");
        water.setMinimumQuantity(50.0);
        water.setUnit("ml");

        when(inventoryServices.getInventoryForUser(5L)).thenReturn(List.of(water));
        when(recipesRepository.findAll()).thenReturn(List.of(recipe));
        when(recipeIngredientRepository.findByRecipeId(103L)).thenReturn(List.of(ingredient));

        List<RecipeRecommendation> result = recipeServices.getRankedRecipesForUser(5L);

        assertEquals(1, result.size());
        assertNotNull(result.getFirst());
    }
}
