package com.example;

import java.util.Arrays;
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

        if ((url.startsWith("\"") && url.endsWith("\"")) || (url.startsWith("'") && url.endsWith("'"))) {
            url = url.substring(1, url.length() - 1).trim();
        }

        url = url.replace("\\/", "/");
        url = url.replace(" ", "%20");

        if (url.startsWith("//")) {
            url = "https:" + url;
        }

        String lower = url.toLowerCase(Locale.ROOT);
        if (!(lower.startsWith("http://") || lower.startsWith("https://") || lower.startsWith("data:image/"))) {
            return null;
        }

        return url;
    }
   

}
