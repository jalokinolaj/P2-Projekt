package com.example;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@DisplayName("MainPage Tests")
public class mainTest {

    private RecipeEntity testRecipe1;
    private RecipeEntity testRecipe2;
    private RecipeEntity testRecipe3;
    private RecipeEntity testRecipe4;
    private User testUser;
    private Inventory inventoryItem1;
    private Inventory inventoryItem2;

    @BeforeEach
    void setUp() {
        // Setup test user
        testUser = new User("testuser", "password", "none", null);
        ReflectionTestUtils.setField(testUser, "id", 1L);

        // Setup test recipes using ReflectionTestUtils for setter-less entity
        testRecipe1 = new RecipeEntity();
        ReflectionTestUtils.setField(testRecipe1, "id", 1);
        ReflectionTestUtils.setField(testRecipe1, "recipeName", "Chicken Soup");
        ReflectionTestUtils.setField(testRecipe1, "ingredients", "chicken, broth, carrot, celery, onion");
        ReflectionTestUtils.setField(testRecipe1, "cuisinePath", "Soups");
        ReflectionTestUtils.setField(testRecipe1, "rating", "4.5");
        ReflectionTestUtils.setField(testRecipe1, "servings", "4");
        ReflectionTestUtils.setField(testRecipe1, "imgSrc", "https://example.com/soup.jpg");

        testRecipe2 = new RecipeEntity();
        ReflectionTestUtils.setField(testRecipe2, "id", 2);
        ReflectionTestUtils.setField(testRecipe2, "recipeName", "Vegetable Stir Fry");
        ReflectionTestUtils.setField(testRecipe2, "ingredients", "broccoli, carrot, soy sauce, garlic, ginger");
        ReflectionTestUtils.setField(testRecipe2, "cuisinePath", "Asian");
        ReflectionTestUtils.setField(testRecipe2, "rating", "4.2");
        ReflectionTestUtils.setField(testRecipe2, "servings", "2");

        testRecipe3 = new RecipeEntity();
        ReflectionTestUtils.setField(testRecipe3, "id", 3);
        ReflectionTestUtils.setField(testRecipe3, "recipeName", "Chocolate Cake");
        ReflectionTestUtils.setField(testRecipe3, "ingredients", "flour, sugar, eggs, chocolate, butter, milk");
        ReflectionTestUtils.setField(testRecipe3, "cuisinePath", "Desserts");
        ReflectionTestUtils.setField(testRecipe3, "rating", "4.8");
        ReflectionTestUtils.setField(testRecipe3, "servings", "8");

        testRecipe4 = new RecipeEntity();
        ReflectionTestUtils.setField(testRecipe4, "id", 4);
        ReflectionTestUtils.setField(testRecipe4, "recipeName", "Vegan Buddha Bowl");
        ReflectionTestUtils.setField(testRecipe4, "ingredients", "chickpeas, quinoa, kale, carrot, tahini");
        ReflectionTestUtils.setField(testRecipe4, "cuisinePath", "Bowls");
        ReflectionTestUtils.setField(testRecipe4, "rating", "4.0");
        ReflectionTestUtils.setField(testRecipe4, "servings", "1");

        // Setup inventory items
        inventoryItem1 = new Inventory(1L, "carrot", 5.0, 5.0, "pcs", "pcs", 2.0, LocalDate.now().plusDays(10));
        ReflectionTestUtils.setField(inventoryItem1, "id", 1L);

        inventoryItem2 = new Inventory(1L, "chicken", 1.0, 1.0, "pcs", "pcs", 2.0, LocalDate.now().plusDays(3));
        ReflectionTestUtils.setField(inventoryItem2, "id", 2L);
    }

    @Nested
    @DisplayName("Diet Filtering Tests")
    class DietFilteringTests {

        @Test
        @DisplayName("Should allow all recipes when diet is 'none'")
        void testDietFilterNone() {
            testUser.setDiet("none");
            List<RecipeEntity> recipes = Arrays.asList(testRecipe1, testRecipe2, testRecipe3, testRecipe4);
            
            assertTrue(recipes.stream().allMatch(r -> matchesDiet(r, "none")));
        }

        @Test
        @DisplayName("Should exclude meat recipes for vegetarian diet")
        void testVegetarianDietFilter() {
            String vegetarianDiet = "vegetarian";
            
            // Chicken soup contains chicken — should be excluded
            assertFalse(matchesDiet(testRecipe1, vegetarianDiet));
            // Vegetable stir fry has no meat — should be included
            assertTrue(matchesDiet(testRecipe2, vegetarianDiet));
            // Chocolate cake has no meat — should be included
            assertTrue(matchesDiet(testRecipe3, vegetarianDiet));
        }

        @Test
        @DisplayName("Should exclude meat, dairy, eggs for vegan diet")
        void testVeganDietFilter() {
            String veganDiet = "vegan";
            
            // Chicken soup has chicken — should be excluded
            assertFalse(matchesDiet(testRecipe1, veganDiet));
            // Vegetable stir fry has no animal products — should be included
            assertTrue(matchesDiet(testRecipe2, veganDiet));
            // Chocolate cake has eggs, butter, milk — should be excluded
            assertFalse(matchesDiet(testRecipe3, veganDiet));
            // Buddha bowl is vegan-friendly
            assertTrue(matchesDiet(testRecipe4, veganDiet));
        }

        @Test
        @DisplayName("Should exclude meat but allow fish for pescatarian diet")
        void testPescatarianDietFilter() {
            String pescatarianDiet = "pescatarian";
            
            // Chicken soup has chicken — should be excluded
            assertFalse(matchesDiet(testRecipe1, pescatarianDiet));
            // Vegetable stir fry — should be included
            assertTrue(matchesDiet(testRecipe2, pescatarianDiet));
        }

        @Test
        @DisplayName("Should allow all recipes when diet is null or blank")
        void testDietNullOrBlank() {
            assertTrue(matchesDiet(testRecipe1, null));
            assertTrue(matchesDiet(testRecipe1, ""));
            assertTrue(matchesDiet(testRecipe1, "   "));
        }

        // Helper method from MainPage
        private boolean matchesDiet(RecipeEntity recipe, String diet) {
            return MainPageRecipeMethods.matchesDiet(recipe, diet);
        }

        private boolean containsAny(String text, String... keywords) {
            return MainPageRecipeMethods.containsAny(text, keywords);
        }
    }

    @Nested
    @DisplayName("Allergen Filtering Tests")
    class AllergenFilteringTests {

        @Test
        @DisplayName("Should exclude recipes with gluten keywords")
        void testGlutenAllergen() {
            List<String> allergens = Arrays.asList("Gluten");
            
            assertTrue(matchesAllergens(testRecipe1, allergens)); // chicken soup — ok
            assertTrue(matchesAllergens(testRecipe2, allergens)); // stir fry — ok
            assertFalse(matchesAllergens(testRecipe3, allergens)); // cake has flour
        }

        @Test
        @DisplayName("Should exclude recipes with egg allergen")
        void testEggAllergen() {
            List<String> allergens = Arrays.asList("Eggs");
            
            assertTrue(matchesAllergens(testRecipe1, allergens)); // soup has no eggs
            assertTrue(matchesAllergens(testRecipe2, allergens)); // stir fry has no eggs
            assertFalse(matchesAllergens(testRecipe3, allergens)); // cake has eggs
        }

        @Test
        @DisplayName("Should exclude recipes with milk allergen")
        void testMilkAllergen() {
            List<String> allergens = Arrays.asList("Milk");
            
            assertTrue(matchesAllergens(testRecipe1, allergens)); // soup has no milk
            assertFalse(matchesAllergens(testRecipe3, allergens)); // cake has milk, butter
        }

        @Test
        @DisplayName("Should handle multiple allergens")
        void testMultipleAllergens() {
            List<String> allergens = Arrays.asList("Eggs", "Milk");
            
            assertTrue(matchesAllergens(testRecipe1, allergens)); // soup — ok
            assertFalse(matchesAllergens(testRecipe3, allergens)); // cake has both
        }

        private boolean matchesAllergens(RecipeEntity recipe, List<String> allergens) {
            return MainPageRecipeMethods.matchesAllergens(recipe, allergens);
        }
    }

    @Nested
    @DisplayName("Ingredient Matching Tests")
    class IngredientMatchingTests {

        @Test
        @DisplayName("calculateMatch should return 100% when all added ingredients are in recipe")
        void testFullMatch() {
            List<String> addedIngredients = Arrays.asList("chicken", "carrot");
            int match = calculateMatch(testRecipe1, addedIngredients);
            assertEquals(100, match);
        }

        @Test
        @DisplayName("calculateMatch should return 50% when half of added ingredients match")
        void testPartialMatch() {
            List<String> addedIngredients = Arrays.asList("chicken", "unknown");
            int match = calculateMatch(testRecipe1, addedIngredients);
            assertEquals(50, match);
        }

        @Test
        @DisplayName("calculateMatch should return 0% when no ingredients match")
        void testNoMatch() {
            List<String> addedIngredients = Arrays.asList("unknown1", "unknown2");
            int match = calculateMatch(testRecipe1, addedIngredients);
            assertEquals(0, match);
        }

        @Test
        @DisplayName("calculateMatch should be case-insensitive")
        void testMatchCaseInsensitive() {
            List<String> addedIngredients = Arrays.asList("CHICKEN", "CARROT");
            int match = calculateMatch(testRecipe1, addedIngredients);
            assertEquals(100, match);
        }

        @Test
        @DisplayName("calculateMatch should return 0 when addedIngredients is empty")
        void testCalculateMatchEmptyAddedIngredients() {
            List<String> addedIngredients = new ArrayList<>();
            int match = calculateMatch(testRecipe1, addedIngredients);
            assertEquals(0, match);
        }

        @Test
        @DisplayName("calculateMatch should return 0 when recipe ingredients are null")
        void testCalculateMatchNullRecipeIngredients() {
            RecipeEntity emptyRecipe = new RecipeEntity();
            ReflectionTestUtils.setField(emptyRecipe, "ingredients", null);
            List<String> addedIngredients = Arrays.asList("chicken");
            int match = calculateMatch(emptyRecipe, addedIngredients);
            assertNotNull(match);
            assertEquals(0, match);
        }

        private int calculateMatch(RecipeEntity entity, List<String> addedIngredients) {
            return MainPageRecipeMethods.calculateMatch(entity, addedIngredients);
        }

        @Test
        @DisplayName("calculateMatch currently matches substrings (egg vs eggplant)")
        void testSubstringFalsePositive() {
            RecipeEntity r = new RecipeEntity();
            ReflectionTestUtils.setField(r, "ingredients", "eggplant, tomato");
            List<String> added = Arrays.asList("egg");
            int match = calculateMatch(r, added);
            assertEquals(100, match, "Substring matching is currently based on contains()");
        }

        @Test
        @DisplayName("calculateMatch should handle plural forms (tomato vs tomatoes)")
        void testPluralMatching() {
            RecipeEntity r = new RecipeEntity();
            ReflectionTestUtils.setField(r, "ingredients", "tomatoes, basil");
            List<String> added = Arrays.asList("tomato");
            int match = calculateMatch(r, added);
            assertEquals(100, match, "Singular should match plural form");
        }

        @Test
        @DisplayName("calculateMatch does not normalize punctuation differences")
        void testPunctuationTolerance() {
            RecipeEntity r = new RecipeEntity();
            ReflectionTestUtils.setField(r, "ingredients", "brown-sugar, flour");
            List<String> added = Arrays.asList("brown sugar");
            int match = calculateMatch(r, added);
            assertEquals(0, match, "Punctuation differences are not normalized yet");
        }

        @Test
        @DisplayName("calculateMatch should not inflate match percentage due to duplicate added ingredients")
        void testDuplicateAddedIngredients() {
            List<String> added = Arrays.asList("chicken", "chicken");
            int match = calculateMatch(testRecipe1, added);
            assertEquals(100, match, "Duplicate ingredients should not change the intended match proportion");
        }

        @Test
        @DisplayName("calculateMatch does not currently resolve aliases (garbanzo -> chickpeas)")
        void testAliasMatching() {
            RecipeEntity r = new RecipeEntity();
            ReflectionTestUtils.setField(r, "ingredients", "chickpeas, lemon");
            List<String> added = Arrays.asList("garbanzo");
            int match = calculateMatch(r, added);
            assertEquals(0, match, "Alias resolution is not implemented yet");
        }
    }

    @Nested
    @DisplayName("Inventory Match Tests")
    class InventoryMatchTests {

        @Test
        @DisplayName("calculateInventoryMatch should return correct percentage")
        void testInventoryMatchPercentage() {
            List<String> inventoryIngredients = Arrays.asList("carrot", "chicken");
            int match = calculateInventoryMatch(testRecipe1, inventoryIngredients);
            // Recipe has carrot and chicken, inventory has both
            assertEquals(100, match);
        }

        @Test
        @DisplayName("calculateInventoryMatch should return 50% for partial match")
        void testInventoryMatchPartial() {
            List<String> inventoryIngredients = Arrays.asList("carrot", "unknown");
            int match = calculateInventoryMatch(testRecipe1, inventoryIngredients);
            // Recipe has carrot but not unknown
            assertEquals(50, match);
        }

        @Test
        @DisplayName("calculateInventoryMatch should return 0 for empty inventory")
        void testInventoryMatchEmpty() {
            List<String> inventoryIngredients = new ArrayList<>();
            int match = calculateInventoryMatch(testRecipe1, inventoryIngredients);
            assertEquals(0, match);
        }

        private int calculateInventoryMatch(RecipeEntity entity, List<String> inventoryIngredients) {
            return MainPageRecipeMethods.calculateInventoryMatch(entity, inventoryIngredients);
        }
    }

    @Nested
    @DisplayName("Fridge Score Tests")
    class FridgeScoreTests {

        @Test
        @DisplayName("Fridge score should include base match + urgency bonus")
        void testFridgeScoreWithUrgency() {
            List<String> inventoryIngredients = Arrays.asList("chicken", "carrot");
            List<Inventory> inventoryItems = Arrays.asList(inventoryItem1, inventoryItem2);
            
            int score = calculateFridgeScore(testRecipe1, inventoryIngredients, inventoryItems);
            // Base match: 100%, plus 20 points (chicken is low + carrot is ok)
            // chicken is below minimum (1.0 <= 2.0) = +10
            // carrot is ok (5.0 > 2.0) = +0
            assertTrue(score >= 100);
        }

        @Test
        @DisplayName("isAboutToRunOut should detect low stock")
        void testLowStock() {
            assertTrue(MainPageRecipeMethods.isAboutToRunOut(inventoryItem2));
            assertFalse(MainPageRecipeMethods.isAboutToRunOut(inventoryItem1));
        }

        @Test
        @DisplayName("isAboutToRunOut should detect items expiring soon")
        void testExpiringItems() {
            Inventory expiringItem = new Inventory();
            expiringItem.setIngredientName("milk");
            expiringItem.setQuantity(5.0);
            expiringItem.setMinimumQuantity(1.0);
            expiringItem.setExpiryDate(LocalDate.now().plusDays(3));
            
            assertTrue(MainPageRecipeMethods.isAboutToRunOut(expiringItem));
        }

        @Test
        @DisplayName("isAboutToRunOut should treat quantity equal to minimum as low stock")
        void testQuantityEqualMinimum() {
            Inventory equalItem = new Inventory();
            equalItem.setIngredientName("test");
            equalItem.setQuantity(2.0);
            equalItem.setMinimumQuantity(2.0);
            equalItem.setExpiryDate(null);

            assertTrue(MainPageRecipeMethods.isAboutToRunOut(equalItem));
        }

        @Test
        @DisplayName("isAboutToRunOut should consider expiry exactly 7 days away as expiring soon")
        void testExpiryExactlySevenDays() {
            Inventory sevenDayItem = new Inventory();
            sevenDayItem.setIngredientName("milk");
            sevenDayItem.setQuantity(5.0);
            sevenDayItem.setMinimumQuantity(1.0);
            sevenDayItem.setExpiryDate(LocalDate.now().plusDays(7));

            assertTrue(MainPageRecipeMethods.isAboutToRunOut(sevenDayItem));
        }

        @Test
        @DisplayName("isAboutToRunOut should return false for null expiry when stock is sufficient")
        void testNullExpirySufficientStock() {
            Inventory item = new Inventory();
            item.setIngredientName("rice");
            item.setQuantity(10.0);
            item.setMinimumQuantity(1.0);
            item.setExpiryDate(null);

            assertFalse(MainPageRecipeMethods.isAboutToRunOut(item));
        }

        private int calculateFridgeScore(RecipeEntity entity, List<String> inventoryIngredients, List<Inventory> inventoryItems) {
            return MainPageRecipeMethods.calculateFridgeScore(entity, inventoryIngredients, inventoryItems);
        }
    }

    @Nested
    @DisplayName("Text Utility Tests")
    class TextUtilityTests {

        @Test
        @DisplayName("containsAny should find keywords in text")
        void testContainsAny() {
            assertTrue(MainPageRecipeMethods.containsAny("chicken soup", "chicken", "beef"));
            assertFalse(MainPageRecipeMethods.containsAny("vegetable soup", "chicken", "beef"));
        }

        @Test
        @DisplayName("containsAny should be case-insensitive")
        void testContainsAnyCaseInsensitive() {
            assertTrue(MainPageRecipeMethods.containsAny("CHICKEN Soup", "chicken"));
            assertTrue(MainPageRecipeMethods.containsAny("Beef stew", "BEEF"));
        }

        @Test
        @DisplayName("containsAny should return false for null text")
        void testContainsAnyNullText() {
            assertFalse(MainPageRecipeMethods.containsAny(null, "chicken"));
        }
    }

    @Nested
    @DisplayName("Recipe Rating Parse Tests")
    class RatingParseTests {

        @Test
        @DisplayName("parseRating should convert valid string to double")
        void testParseValidRating() {
            assertEquals(4.5, MainPageRecipeMethods.parseRating("4.5"));
            assertEquals(4.0, MainPageRecipeMethods.parseRating("4"));
        }

        @Test
        @DisplayName("parseRating should return 0 for invalid string")
        void testParseInvalidRating() {
            assertEquals(0.0, MainPageRecipeMethods.parseRating("invalid"));
            assertEquals(0.0, MainPageRecipeMethods.parseRating(null));
        }
    }

}
