package com.example.Services;
import com.example.Inventory;
import com.example.RecipeIngredient;
import com.example.Recipes;
import com.example.Repositories.RecipeIngredientRepository;
import com.example.Repositories.RecipesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RecipeServices {
    @Autowired
    private RecipesRepository recipesRepository;

    @Autowired
    private RecipeIngredientRepository recipeIngredientRepository;

    @Autowired
    private InventoryServices inventoryServices;

    public List<Recipes> getAllRecipes() {
        return recipesRepository.findAll();
    }

    public Recipes getRecipeById(Long id) {
        return recipesRepository.findById(id).orElse(null);
    }

    public List<Recipes> getRecipesSortedByRating() {
        return recipesRepository.findAllByOrderByRatingDesc();
    }

    public List<Recipes> searchRecipesByName(String name) {
        return recipesRepository.findByRecipeNameContainingIgnoreCase(name);
    }

    public List<RecipeRecommendation> getRankedRecipesForUser(String username) {
        // Fast lookup map so ingredient matching is O(1) per ingredient name.
        Map<String, Inventory> inventoryByIngredient = new HashMap<>();
        for (Inventory inventoryItem : inventoryServices.getInventoryForUser(username)) {
            inventoryByIngredient.put(normalize(inventoryItem.getIngredientName()), inventoryItem);
        }

        // Sort priority: best ingredient match, then urgency to use items, then recipe rating.
        return recipesRepository.findAll().stream()
                .map(recipe -> buildRecommendation(recipe, inventoryByIngredient))
                .sorted(Comparator
                        .comparingDouble(RecipeRecommendation::matchPercent).reversed()
                        .thenComparingDouble(RecipeRecommendation::urgencyScore).reversed()
                    .thenComparingDouble(r -> {
                        // rating is stored as text in DB, parse it safely
                        try { return Double.parseDouble(r.recipe().getRating()); }
                        catch (Exception e) { return 0.0; }
                    })
                        .reversed())
                .toList();
    }

    private RecipeRecommendation buildRecommendation(Recipes recipe, Map<String, Inventory> inventoryByIngredient) {
        // Read ingredient requirements for this recipe from recipe_ingredients table.
        List<RecipeIngredient> requirements = recipeIngredientRepository.findByRecipeId(recipe.getId());
        if (requirements.isEmpty()) {
            return new RecipeRecommendation(recipe, 0.0, "No ingredients added for this recipe", "-", 0.0);
        }

        int matched = 0;
        String runOutFirstIngredient = "-";
        double highestUrgency = 0.0;

        List<String> missing = new java.util.ArrayList<>();

        for (RecipeIngredient requirement : requirements) {
            Inventory available = inventoryByIngredient.get(normalize(requirement.getIngredientName()));
            Double required = requirement.getRequiredQuantity();
            double needed = required == null ? 0.0 : required;
            Double currentQuantity = available == null ? null : available.getQuantity();
            double has = currentQuantity == null ? 0.0 : currentQuantity;

            // Ingredient counts as matched only if user has enough for this recipe.
            if (has >= needed && needed > 0.0) {
                matched++;
            } else {
                missing.add(requirement.getIngredientName());
            }

            // Keep track of the most urgent ingredient to consume for this recipe.
            if (available != null) {
                double urgency = computeUrgency(available, needed);
                if (urgency > highestUrgency) {
                    highestUrgency = urgency;
                    runOutFirstIngredient = available.getIngredientName();
                }
            }
        }

        double matchPercent = (matched * 100.0) / requirements.size();
        String missingIngredients = missing.isEmpty() ? "None" : missing.stream().collect(Collectors.joining(", "));

        return new RecipeRecommendation(recipe, matchPercent, missingIngredients, runOutFirstIngredient, highestUrgency);
    }

    private double computeUrgency(Inventory inventory, double requiredQuantity) {
        Double inventoryQuantity = inventory.getQuantity();
        Double inventoryMinimum = inventory.getMinimumQuantity();
        double quantity = inventoryQuantity == null ? 0.0 : inventoryQuantity;
        double minimum = inventoryMinimum == null ? 0.0 : inventoryMinimum;
        // Risk 1: This recipe consumes a large share of what is left in stock.
        double stockRisk = (requiredQuantity <= 0.0 || quantity <= 0.0)
                ? 0.0
                : Math.max(0.0, Math.min(1.0, requiredQuantity / quantity));

        // Risk 2: Current stock is close to or below low-stock threshold.
        double thresholdRisk = quantity <= minimum ? 1.0 : Math.max(0.0, minimum / Math.max(quantity, 1.0));

        double expiryRisk = 0.0;
        LocalDate expiryDate = inventory.getExpiryDate();
        if (expiryDate != null) {
            long daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), expiryDate);
            // Risk 3: Expired or near-expiry ingredients should be consumed first.
            if (daysLeft <= 0) {
                expiryRisk = 1.0;
            } else if (daysLeft <= 14) {
                expiryRisk = (14.0 - daysLeft) / 14.0;
            }
        }

        // Weighted urgency score in range [0..1].
        return (stockRisk * 0.5) + (thresholdRisk * 0.2) + (expiryRisk * 0.3);
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }
}