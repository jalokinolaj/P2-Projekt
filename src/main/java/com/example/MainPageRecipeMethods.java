package com.example;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainPageRecipeMethods {
    public static String mapCategory(String cuisinePath) {
        if (cuisinePath == null) return "Dinner";
        String p = cuisinePath.toLowerCase();
        if (p.contains("dessert") || p.contains("cake") || p.contains("cookie")
                || p.contains("pie") || p.contains("pudding") || p.contains("cobbler")
                || p.contains("candy") || p.contains("brownie") || p.contains("fudge")
                || p.contains("shortcake") || p.contains("ice cream")) {
            return "Dessert";
        }
        if (p.contains("breakfast") || p.contains("brunch") || p.contains("bread")) {
            return "Breakfast";
        }
        if (p.contains("appetizer") || p.contains("snack") || p.contains("dip")
                || p.contains("spread")) {
            return "Appetizer";
        }
        if (p.contains("salad") || p.contains("lunch")) {
            return "Lunch";
        }
        return "Dinner";
    }

    public static int parseMinutes(RecipeEntity entity) {
        String timeStr = entity.getTotalTime() != null ? entity.getTotalTime() : entity.getCookTime();
        if (timeStr == null || timeStr.isEmpty()) return 0;
        int total = 0;
        Matcher hours = Pattern.compile("(\\d+)\\s*hr").matcher(timeStr);
        if (hours.find()) total += Integer.parseInt(hours.group(1)) * 60;
        Matcher mins = Pattern.compile("(\\d+)\\s*min").matcher(timeStr);
        if (mins.find()) total += Integer.parseInt(mins.group(1));
        return total;
    }

    public static String parseNutrition(String nutritionText) {
        if (nutritionText == null) return "";
        String target = "Protein";
        Pattern pattern = Pattern.compile(target + ":?\\s*(\\d+\\.?\\d*)\\s*g", Pattern.CASE_INSENSITIVE);
        Matcher match = pattern.matcher(nutritionText);
        if (match.find()) {
            return "🍗 " + match.group(1) + "g protein";
        }
        return "";
    }

    public static String[] parseIngredients(String ingredientsText) {
        if (ingredientsText == null) return new String[0];
        return Arrays.stream(ingredientsText.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .limit(8)
            .toArray(String[]::new);
    }

    public static String normalizeImageUrl(String rawUrl) {
        if (rawUrl == null) return null;

        String url = rawUrl.trim();
        if (url.isEmpty()) return null;

        if ((url.startsWith("\"") && url.endsWith("\""))
            || (url.startsWith("'") && url.endsWith("'"))) {
            url = url.substring(1, url.length() - 1).trim();
        }

        url = url.replace("\\/", "/");
        url = url.replace(" ", "%20");

        if (url.startsWith("//")) {
            url = "https:" + url;
        }

        String lower = url.toLowerCase(Locale.ROOT);
        if (!(lower.startsWith("http://")
            || lower.startsWith("https://")
            || lower.startsWith("data:image/"))) {
            return null;
        }

        return url;
    }

    public static boolean matchesDiet(RecipeEntity recipe, String diet) {
        if (diet == null || diet.isBlank()) {
            return true;
        }

        String userDiet = diet.toLowerCase(Locale.ROOT).trim();

        if (userDiet.equals("none") || userDiet.equals("omnivore")) {
            return true;
        }

        String ingredients = recipe.getIngredients() == null
                ? ""
                : recipe.getIngredients().toLowerCase(Locale.ROOT);

        if (userDiet.equals("vegan")) {
            return !containsAny(ingredients,
                    "chicken", "beef", "pork", "bacon", "ham", "turkey",
                    "fish", "salmon", "tuna", "shrimp", "prawn",
                    "egg", "eggs",
                    "milk", "cheese", "butter", "cream", "yogurt", "honey", "lamb", "lobster");
        }

        if (userDiet.equals("vegetarian")) {
            return !containsAny(ingredients,
                    "chicken", "beef", "pork", "bacon", "ham", "turkey",
                    "fish", "salmon", "tuna", "shrimp", "prawn", "lamb", "lobster");
        }

        if (userDiet.equals("pescatarian")) {
            return !containsAny(ingredients,
                    "chicken", "beef", "pork", "bacon", "ham", "turkey", "lamb");
        }

        return true;
    }

    public static boolean matchesAllergens(RecipeEntity recipe, List<String> allergens) {
        String ingredients = recipe.getIngredients() == null
                ? "" : recipe.getIngredients().toLowerCase(Locale.ROOT);

        for (String allergen : allergens) {
            String[] keywords = allergenKeywords(allergen.trim());
            for (String keyword : keywords) {
                if (ingredients.contains(keyword.toLowerCase(Locale.ROOT))) {
                    return false;
                }
            }
        }
        return true;
    }

    public static int calculateMatch(RecipeEntity entity, List<String> addedIngredients) {
        if (addedIngredients.isEmpty() || entity.getIngredients() == null) return 0;
        String ingText = entity.getIngredients().toLowerCase(Locale.ROOT);
        long matches = addedIngredients.stream()
            .filter(ing -> ingText.contains(ing.toLowerCase(Locale.ROOT)))
            .count();
        return (int) Math.round((double) matches / addedIngredients.size() * 100);
    }

    public static int calculateInventoryMatch(RecipeEntity entity, List<String> inventoryIngredients) {
        if (inventoryIngredients.isEmpty() || entity.getIngredients() == null) {
            return 0;
        }
        String ingText = entity.getIngredients().toLowerCase(Locale.ROOT);
        long matches = inventoryIngredients.stream()
            .filter(ingText::contains)
            .count();
        return (int) Math.round((double) matches / inventoryIngredients.size() * 100);
    }

    public static int calculateFridgeScore(RecipeEntity entity, List<String> inventoryIngredients, List<Inventory> inventoryItems) {
        if (entity.getIngredients() == null) {
            return 0;
        }

        String recipeIngredients = entity.getIngredients().toLowerCase(Locale.ROOT);
        int matchPercent = calculateInventoryMatch(entity, inventoryIngredients);

        long runOutSoonHits = inventoryItems.stream()
            .filter(item -> item.getIngredientName() != null)
            .filter(item -> recipeIngredients.contains(item.getIngredientName().toLowerCase(Locale.ROOT)))
            .filter(MainPageRecipeMethods::isAboutToRunOut)
            .count();

        return matchPercent + (int) runOutSoonHits * 10;
    }

    public static boolean isAboutToRunOut(Inventory item) {
        Double quantityValue = item.getQuantity();
        Double minimumValue = item.getMinimumQuantity();
        double quantity = quantityValue == null ? 0.0 : quantityValue;
        double minimum = minimumValue == null ? 0.0 : minimumValue;

        boolean lowStock = quantity <= minimum;
        LocalDate expiryDate = item.getExpiryDate();
        boolean expiringSoon = expiryDate != null && !expiryDate.isAfter(LocalDate.now().plusDays(7));

        return lowStock || expiringSoon;
    }

    public static double parseRating(String rating) {
        try {
            return Double.parseDouble(rating);
        } catch (Exception e) {
            return 0.0;
        }
    }

    public static boolean containsAny(String text, String... keywords) {
        if (text == null || text.isBlank()) {
            return false;
        }

        String lowerText = text.toLowerCase(Locale.ROOT);

        for (String keyword : keywords) {
            if (lowerText.contains(keyword.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }

        return false;
    }

    private static String[] allergenKeywords(String allergen) {
        return switch (allergen) {
            case "Gluten"         -> new String[]{"wheat", "flour", "bread", "barley", "rye", "gluten", "pasta", "oats", "semolina"};
            case "Crustaceans"    -> new String[]{"shrimp", "prawn", "crab", "lobster", "crawfish", "crayfish"};
            case "Eggs"           -> new String[]{"egg", "eggs"};
            case "Fish"           -> new String[]{"fish", "salmon", "tuna", "cod", "tilapia", "halibut", "bass", "trout", "anchovy", "sardine", "mackerel"};
            case "Peanuts"        -> new String[]{"peanut", "groundnut"};
            case "Soy"            -> new String[]{"soy", "soya", "tofu", "tempeh", "edamame", "miso"};
            case "Milk"           -> new String[]{"milk", "cheese", "butter", "cream", "yogurt", "lactose", "whey", "casein", "dairy"};
            case "Nuts"           -> new String[]{"almond", "walnut", "cashew", "pecan", "pistachio", "hazelnut", "macadamia", "chestnut", "nut", "nuts"};
            case "Celery"         -> new String[]{"celery", "celeriac"};
            case "Mustard"        -> new String[]{"mustard"};
            case "Sesame"         -> new String[]{"sesame", "tahini"};
            case "Sulphur dioxide"-> new String[]{"sulphite", "sulfite", "sulphur dioxide", "sulfur dioxide"};
            case "Lupin"          -> new String[]{"lupin", "lupine"};
            case "Molluscs"       -> new String[]{"oyster", "mussel", "clam", "scallop", "squid", "octopus", "snail"};
            default               -> new String[]{allergen.toLowerCase(Locale.ROOT)};
        };
    }


}
