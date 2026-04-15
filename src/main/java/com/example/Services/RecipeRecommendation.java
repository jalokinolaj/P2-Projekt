package com.example.Services;

import com.example.Recipes;

public record RecipeRecommendation(
        Recipes recipe,
        double matchPercent,
        String missingIngredients,
        String runOutFirstIngredient,
        double urgencyScore) {
}
