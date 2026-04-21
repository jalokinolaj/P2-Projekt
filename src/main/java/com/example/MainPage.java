package com.example;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.example.Services.InventoryServices;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;


/**
 * MainPage is the primary view of the Recipe Finder application.
 * It is accessible at the "/main" route and displays a searchable,
 * filterable grid of recipes loaded from the PostgreSQL database.
 *
 * Users can:
 *  - Search recipes by name (live filtering as they type)
 *  - Search recipes by ingredients (chips-based multi-ingredient filter)
 *  - Filter by category (Breakfast, Lunch, Dinner, Dessert, Appetizer)
 *  - View their profile and log out
 *
 * If no user is logged in, an error screen is shown instead.
 */
@Route("main")
public class MainPage extends VerticalLayout {

    // Tracks which category button is currently selected (default: show all)
    private String currentCategory = "All";

    // Tracks the current text in the recipe name search field
    private String nameSearchQuery = "";

    // List of ingredients the user has added via the ingredient search bar
    private final List<String> addedIngredients = new ArrayList<>();

    // The row of ingredient chip tags shown below the ingredient search field
    private HorizontalLayout ingredientChips;

    // Keeps a reference to all category buttons so we can update their style on click
    private final List<Button> categoryButtons = new ArrayList<>();

    // The grid container that holds the recipe cards
    private Div recipeGrid;

    // Spring Data JPA repository used to query the recipes table
    private final RecipeRepository recipeRepository;

    // All recipes loaded from the database at startup, used for in-memory filtering
    private final List<RecipeEntity> cachedRecipes;

    // Current user's inventory rows, used for fridge matching and urgency sorting.
    private final List<Inventory> inventoryItems;

    // Current user's inventory ingredient names, used for fridge-mode sorting.
    private final List<String> inventoryIngredients;

    // When true, recipes are sorted by inventory match from the user's fridge.
    private boolean fridgeModeEnabled = false;

    /**
     * Constructor — called by Vaadin/Spring when the user navigates to "/main".
     * Checks if a user is logged in, and either shows the main UI or an error screen.
     *
     * @param recipeRepository injected by Spring, provides access to the recipes table
     */
    public MainPage(RecipeRepository recipeRepository, InventoryServices inventoryServices) {
        this.recipeRepository = recipeRepository;

        // Check if a user is stored in the current session
        User sessionUser = (User) VaadinSession.getCurrent().getAttribute("user");
        if (sessionUser == null) {
            // No logged-in user — show an error card and stop building the page
            this.cachedRecipes = List.of();
            this.inventoryItems = List.of();
            this.inventoryIngredients = List.of();
            setSizeFull();
            setJustifyContentMode(JustifyContentMode.CENTER);
            setAlignItems(Alignment.CENTER);

            Div errorCard = new Div();
            errorCard.getStyle()
                .set("text-align", "center")
                .set("padding", "3rem")
                .set("background", "var(--lumo-base-color)")
                .set("border-radius", "12px")
                .set("box-shadow", "0 4px 20px rgba(0,0,0,0.1)");

            H2 msg = new H2("You need to login");
            msg.getStyle().set("margin-bottom", "1rem");

            Button loginBtn = new Button("Go to Login", VaadinIcon.SIGN_IN.create(),
                e -> UI.getCurrent().navigate(""));
            loginBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

            errorCard.add(msg, loginBtn);
            add(errorCard);
            return;
        }

        // Load all recipes from the database once and store them in memory
        this.cachedRecipes = recipeRepository.findAll();
        this.inventoryItems = inventoryServices.getInventoryForUser(sessionUser.getUsername());
        this.inventoryIngredients = inventoryItems.stream()
            .map(Inventory::getIngredientName)
            .filter(name -> name != null && !name.isBlank())
            .map(name -> name.trim().toLowerCase(Locale.ROOT))
            .distinct()
            .collect(Collectors.toList());

        setSizeFull();
        setPadding(false);
        setSpacing(false);
        addClassName("main-page");

        // pageWrapper centers and constrains the content width via CSS
        Div pageWrapper = new Div();
        pageWrapper.addClassName("page-wrapper");

        // Build each section and add them to the page in order
        pageWrapper.add(createHeader());
        pageWrapper.add(createNameSearchSection());
        pageWrapper.add(createSearchSection());
        pageWrapper.add(createCategoryFilters());

        H2 sectionTitle = new H2("Today's Best Recipes");
        sectionTitle.addClassName("section-title");
        pageWrapper.add(sectionTitle);

        // Create the recipe grid and populate it with the initial set of recipes
        recipeGrid = new Div();
        recipeGrid.addClassName("recipe-grid");
        refreshRecipeGrid();
        pageWrapper.add(recipeGrid);

        add(pageWrapper);
    }

    /**
     * Builds the top header bar containing the page title and the Profile button.
     * A spacer div on the left balances the Profile button on the right,
     * keeping the title visually centered.
     */
    private Div createHeader() {
        Div header = new Div();
        header.addClassName("page-header");

        // Empty div that pushes the title to the center
        Div spacer = new Div();
        spacer.addClassName("header-spacer");

        VerticalLayout titleArea = new VerticalLayout();
        titleArea.addClassName("title-area");
        titleArea.setPadding(false);
        titleArea.setSpacing(false);
        titleArea.setAlignItems(Alignment.CENTER);

        H1 title = new H1("Recipe finder");
        title.addClassName("main-title");

        Span subtitle = new Span("Enter your ingredients and discover delicious recipes");
        subtitle.addClassName("subtitle");

        titleArea.add(title, subtitle);

        // Profile button in the top-right corner; opens a dialog with user info and logout
        Button profileBtn = new Button("Profile", VaadinIcon.USER.create());
        profileBtn.addClassName("profile-btn");
        profileBtn.addClickListener(e -> openProfileDialog());

        header.add(spacer, titleArea, profileBtn);
        return header;
    }

    /**
     * Builds the recipe name search card.
     * Updates nameSearchQuery and refreshes the grid every time the user changes the text field.
     */
    private Div createNameSearchSection() {
        Div card = new Div();
        card.addClassName("search-card");

        TextField nameField = new TextField();
        nameField.setLabel("Search by recipe name");
        nameField.setPlaceholder("e.g. Chicken Soup, Apple Pie...");
        nameField.setPrefixComponent(VaadinIcon.BOOK.create());
        nameField.setWidthFull();
        nameField.addClassName("search-field");

        // Fires on every keystroke — updates the query and re-renders the grid
        nameField.addValueChangeListener(e -> {
            nameSearchQuery = e.getValue().trim();
            refreshRecipeGrid();
        });

        card.add(nameField);
        return card;
    }

    /**
     * Builds the ingredient search card.
     * Users type an ingredient and click "Add" to add it to the active filter list.
     * Each added ingredient appears as a removable chip below the input.
     */
    private Div createSearchSection() {
        Div searchCard = new Div();
        searchCard.addClassName("search-card");

        HorizontalLayout searchRow = new HorizontalLayout();
        searchRow.setWidthFull();
        searchRow.setAlignItems(Alignment.BASELINE);
        searchRow.setSpacing(true);

        TextField ingredientField = new TextField();
        ingredientField.setPlaceholder("Enter an ingredient (e.g., tomato, chicken, garlic)");
        ingredientField.setPrefixComponent(VaadinIcon.SEARCH.create());
        ingredientField.setWidthFull();
        ingredientField.addClassName("search-field");

        Button addBtn = new Button("Add");
        addBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addBtn.addClassName("add-btn");
        addBtn.addClickListener(e -> {
            String val = ingredientField.getValue().trim().toLowerCase();
            if (!val.isEmpty() && !addedIngredients.contains(val)) {
                // Add the ingredient to the filter list, update the chip row and grid
                addedIngredients.add(val);
                ingredientField.clear();
                refreshChips();
                refreshRecipeGrid();
            } else if (val.isEmpty()) {
                Notification.show("Please enter an ingredient");
            } else {
                Notification.show("Ingredient already added");
            }
        });

        searchRow.add(ingredientField, addBtn);
        searchRow.expand(ingredientField);

        // Chip container is hidden until at least one ingredient is added
        ingredientChips = new HorizontalLayout();
        ingredientChips.addClassName("ingredient-chips");
        ingredientChips.setSpacing(true);
        ingredientChips.setVisible(false);

        searchCard.add(searchRow, ingredientChips);
        return searchCard;
    }

    /**
     * Redraws the ingredient chips row from scratch based on the current addedIngredients list.
     * Each chip shows the ingredient name and an "×" button to remove it from the filter.
     */
    private void refreshChips() {
        ingredientChips.removeAll();
        ingredientChips.setVisible(!addedIngredients.isEmpty());
        for (String ing : addedIngredients) {
            Span chip = new Span();
            chip.addClassName("ingredient-chip");

            Span label = new Span(ing);
            Button remove = new Button("×");
            remove.addClassName("chip-remove");
            remove.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);

            // Clicking "×" removes this ingredient from the filter and refreshes the grid
            remove.addClickListener(e -> {
                addedIngredients.remove(ing);
                refreshChips();
                refreshRecipeGrid();
            });

            chip.add(label, remove);
            ingredientChips.add(chip);
        }
    }

    /**
     * Builds the row of category filter buttons (All, Breakfast, Lunch, Dinner, Dessert, Appetizer).
     * The active category button is styled as primary; all others are tertiary (outlined).
     * Clicking a button updates currentCategory and refreshes the grid.
     */
    private Div createCategoryFilters() {
        Div filterRow = new Div();
        filterRow.addClassName("filter-row");

        String[] categories = {"All", "Breakfast", "Lunch", "Dinner", "Dessert", "Appetizer"};

        for (String cat : categories) {
            Button btn = new Button(cat);
            btn.addClassName("filter-btn");

            // Highlight the currently selected category
            if (cat.equals(currentCategory)) {
                btn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            } else {
                btn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            }

            btn.addClickListener(e -> {
                currentCategory = cat;

                // Reset all buttons to outlined, then highlight only the clicked one
                categoryButtons.forEach(b -> {
                    b.removeThemeVariants(ButtonVariant.LUMO_PRIMARY);
                    b.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
                });
                btn.removeThemeVariants(ButtonVariant.LUMO_TERTIARY);
                btn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
                refreshRecipeGrid();
            });
            categoryButtons.add(btn);
            filterRow.add(btn);
        }

        Button fridgeModeBtn = new Button("Fridge mode: Off", VaadinIcon.ARCHIVE.create());
        fridgeModeBtn.addClassName("filter-btn");
        fridgeModeBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        fridgeModeBtn.addClickListener(e -> {
            fridgeModeEnabled = !fridgeModeEnabled;
            if (fridgeModeEnabled) {
                fridgeModeBtn.setText("Fridge mode: On");
                fridgeModeBtn.removeThemeVariants(ButtonVariant.LUMO_TERTIARY);
                fridgeModeBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            } else {
                fridgeModeBtn.setText("Fridge mode: Off");
                fridgeModeBtn.removeThemeVariants(ButtonVariant.LUMO_PRIMARY);
                fridgeModeBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            }
            refreshRecipeGrid();
        });
        filterRow.add(fridgeModeBtn);

        return filterRow;
    }

    /**
     * Core method that rebuilds the recipe grid based on the current filter state.
     *
     * Filter pipeline (applied in order):
     *  1. Ingredient filter  — if ingredients are added, query the DB for the first ingredient,
     *                          then narrow down in memory for any additional ingredients.
     *  2. Name filter        — if nameSearchQuery is non-empty, keep only recipes whose
     *                          name contains the search text (case-insensitive).
     *  3. Category filter    — keep only recipes that map to the selected category.
     *  4. Sort               — if ingredients are active, sort by match % descending.
     *  5. Limit              — cap results at 50 cards to avoid overloading the UI.
     */
    private void refreshRecipeGrid() {
        if (recipeGrid == null) return;
        recipeGrid.removeAll();

        List<RecipeEntity> results;

        // Step 1: Ingredient filter
        // Use the DB to fetch recipes containing the first ingredient (faster than scanning all),
        // then filter in memory for each additional ingredient.
        if (!addedIngredients.isEmpty()) {
            results = recipeRepository.findByIngredient(addedIngredients.get(0));
            for (int i = 1; i < addedIngredients.size(); i++) {
                final String ing = addedIngredients.get(i);
                results = results.stream()
                    .filter(r -> r.getIngredients() != null &&
                                r.getIngredients().toLowerCase().contains(ing.toLowerCase()))
                    .collect(Collectors.toList());
            }
        } else {
            // No ingredient filter — start with the full cached recipe list
            results = new ArrayList<>(cachedRecipes);
        }

        // Step 2: Name filter — partial, case-insensitive match on recipe_name
        if (!nameSearchQuery.isEmpty()) {
            results = results.stream()
                .filter(r -> r.getRecipeName() != null &&
                            r.getRecipeName().toLowerCase().contains(nameSearchQuery.toLowerCase()))
                .collect(Collectors.toList());
        }

        // Step 3: Category filter — map the DB's cuisine_path to our category labels
        if (!currentCategory.equals("All")) {
            results = results.stream()
                .filter(r -> mapCategory(r.getCuisinePath()).equals(currentCategory))
                .collect(Collectors.toList());
        }

        // Step 4: Sort by fridge match when fridge mode is enabled.
        // Otherwise keep existing behavior: sort by manual ingredient match when chips are active.
        if (fridgeModeEnabled) {
            results = results.stream()
                .sorted((a, b) -> calculateFridgeScore(b) - calculateFridgeScore(a))
                .collect(Collectors.toList());
        } else if (!addedIngredients.isEmpty()) {
            results = results.stream()
                .sorted((a, b) -> calculateMatch(b) - calculateMatch(a))
                .collect(Collectors.toList());
        }

        // Step 5: Cap at 50 results
        results = results.stream().limit(50).collect(Collectors.toList());

        if (results.isEmpty()) {
            Span noResults = new Span("No recipes found.");
            noResults.getStyle().set("color", "var(--lumo-secondary-text-color)");
            noResults.getStyle().set("padding", "2rem");
            recipeGrid.add(noResults);
            return;
        }

        // Render a card for each recipe
        for (RecipeEntity entity : results) {
            int matchPercent = fridgeModeEnabled ? calculateInventoryMatch(entity) : calculateMatch(entity);
            recipeGrid.add(createRecipeCard(entity, matchPercent));
        }
    }

    private int calculateInventoryMatch(RecipeEntity entity) {
        if (inventoryIngredients.isEmpty() || entity.getIngredients() == null) {
            return 0;
        }
        String ingText = entity.getIngredients().toLowerCase(Locale.ROOT);
        long matches = inventoryIngredients.stream()
            .filter(ingText::contains)
            .count();
        return (int) Math.round((double) matches / inventoryIngredients.size() * 100);
    }

    // Simple fridge score:
    // 1) base = normal inventory match percent
    // 2) +10 points for each matching ingredient that is about to run out
    private int calculateFridgeScore(RecipeEntity entity) {
        if (entity.getIngredients() == null) {
            return 0;
        }

        String recipeIngredients = entity.getIngredients().toLowerCase(Locale.ROOT);
        int matchPercent = calculateInventoryMatch(entity);

        long runOutSoonHits = inventoryItems.stream()
            .filter(item -> item.getIngredientName() != null)
            .filter(item -> recipeIngredients.contains(item.getIngredientName().toLowerCase(Locale.ROOT)))
            .filter(this::isAboutToRunOut)
            .count();

        return matchPercent + (int) runOutSoonHits * 10;
    }

    private boolean isAboutToRunOut(Inventory item) {
        Double quantityValue = item.getQuantity();
        Double minimumValue = item.getMinimumQuantity();
        double quantity = quantityValue == null ? 0.0 : quantityValue;
        double minimum = minimumValue == null ? 0.0 : minimumValue;

        boolean lowStock = quantity <= minimum;
        LocalDate expiryDate = item.getExpiryDate();
        boolean expiringSoon = expiryDate != null && !expiryDate.isAfter(LocalDate.now().plusDays(7));

        return lowStock || expiringSoon;
    }

    /**
     * Calculates how many of the user's added ingredients appear in the recipe's ingredient text,
     * expressed as a percentage of the total number of added ingredients.
     *
     * Example: user added ["chicken", "garlic"], recipe contains "chicken" but not "garlic" → 50%
     *
     * @param entity the recipe to check
     * @return match percentage (0–100), or 0 if no ingredients have been added
     */
    private int calculateMatch(RecipeEntity entity) {
        if (addedIngredients.isEmpty() || entity.getIngredients() == null) return 0;
        String ingText = entity.getIngredients().toLowerCase();
        long matches = addedIngredients.stream()
            .filter(ing -> ingText.contains(ing.toLowerCase()))
            .count();
        return (int) Math.round((double) matches / addedIngredients.size() * 100);
    }

    /**
     * Maps the database's cuisine_path (e.g. "/Desserts/Cakes/Chocolate Cake Recipes/")
     * to one of the five app categories: Dessert, Breakfast, Appetizer, Lunch, or Dinner.
     * Dinner is the default fallback for anything that doesn't match the other keywords.
     *
     * @param cuisinePath the raw cuisine_path string from the database
     * @return one of: "Dessert", "Breakfast", "Appetizer", "Lunch", "Dinner"
     */
    private String mapCategory(String cuisinePath) {
        if (cuisinePath == null) return "Dinner";
        String p = cuisinePath.toLowerCase();
        if (p.contains("dessert") || p.contains("cake") || p.contains("cookie")
                || p.contains("pie") || p.contains("pudding") || p.contains("cobbler")
                || p.contains("candy") || p.contains("brownie") || p.contains("fudge")
                || p.contains("shortcake") || p.contains("ice cream"))
            return "Dessert";
        if (p.contains("breakfast") || p.contains("brunch") || p.contains("bread"))
            return "Breakfast";
        if (p.contains("appetizer") || p.contains("snack") || p.contains("dip")
                || p.contains("spread"))
            return "Appetizer";
        if (p.contains("salad") || p.contains("lunch"))
            return "Lunch";
        return "Dinner";
    }

    /**
     * Parses a human-readable time string (e.g. "1 hrs 25 mins", "45 mins") into total minutes.
     * Prefers total_time over cook_time when both are available.
     *
     * @param entity the recipe entity containing the time fields
     * @return total time in minutes, or 0 if the time string is missing or unparseable
     */
    private int parseMinutes(RecipeEntity entity) {
        String timeStr = entity.getTotalTime() != null ? entity.getTotalTime() : entity.getCookTime();
        if (timeStr == null || timeStr.isEmpty()) return 0;
        int total = 0;
        Matcher hours = Pattern.compile("(\\d+)\\s*hr").matcher(timeStr);
        if (hours.find()) total += Integer.parseInt(hours.group(1)) * 60;
        Matcher mins = Pattern.compile("(\\d+)\\s*min").matcher(timeStr);
        if (mins.find()) total += Integer.parseInt(mins.group(1));
        return total;
    }

    /**
     * Parses the nutrition text to extract the protein amount.
     * @param nutritionText the raw nutrition text from the database
     * @return a string with the protein amount or empty if not found
     */    
    private String parseNutrition(String nutritionText){
        if (nutritionText == null) return "";
        String target = "Protein";
        Pattern pattern = Pattern.compile(target + ":?\\s*(\\d+\\.?\\d*)\\s*g", Pattern.CASE_INSENSITIVE);
        Matcher match = pattern.matcher(nutritionText);
        if (match.find()) {
            return "🍗 " + match.group(1) + "g protein";
        } else {
            return "";
        }
    } 



    /**
     * Splits the raw ingredients string (a comma-separated list of ingredient descriptions)
     * into an array of individual ingredient strings, capped at 8 items for display purposes.
     *
     * Example input:  "2 cups flour, 1 egg, 1 tsp salt, ..."
     * Example output: ["2 cups flour", "1 egg", "1 tsp salt", ...]
     *
     * @param ingredientsText the raw ingredients text from the database
     * @return array of trimmed ingredient strings (max 8 entries)
     */
    private String[] parseIngredients(String ingredientsText) {
        if (ingredientsText == null) return new String[0];
        return Arrays.stream(ingredientsText.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .limit(8)
            .toArray(String[]::new);
    }

    /**
     * Normalizes image URLs from the database so they work reliably in the browser.
     * Handles common stored variants like quoted strings and escaped slashes.
     */
    // Written by GitHub Copilot: URL normalization helper for robust external image loading.
    private String normalizeImageUrl(String rawUrl) {
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

    /**
     * Builds a single recipe card Div to be placed in the recipe grid.
     * Each card contains:
     *  - A photo from the database (or a fallback emoji if no image is available)
     *  - The recipe name and a match % badge
     *  - A category chip
     *  - Cook time and servings
     *  - The first 8 ingredients
     *
     * @param entity       the recipe data from the database
     * @param matchPercent how well this recipe matches the user's added ingredients (0–100)
     * @return a fully constructed recipe card Div
     */
    private Div createRecipeCard(RecipeEntity entity, int matchPercent) {
        Div card = new Div();
        card.addClassName("recipe-card");

        // Image area — shows the recipe photo or a fallback emoji
        Div imageArea = new Div();
        imageArea.addClassName("recipe-image");
        // Written by GitHub Copilot: normalize DB URL before rendering to the browser.
        String imageUrl = normalizeImageUrl(entity.getImgSrc());
        if (imageUrl != null) {
            Image img = new Image(imageUrl, entity.getRecipeName());
            img.addClassName("recipe-photo");
            // Written by GitHub Copilot: some image hosts block requests with referrer headers.
            img.getElement().setAttribute("referrerpolicy", "no-referrer");
            // Written by GitHub Copilot: lazy loading to reduce image request pressure.
            img.getElement().setAttribute("loading", "lazy");
            imageArea.add(img);
        } else {
            Span chefIcon = new Span("🍳");
            chefIcon.addClassName("chef-icon");
            imageArea.add(chefIcon);
        }

        Div body = new Div();
        body.addClassName("card-body");

        // Title row: recipe name on the left, match % badge on the right
        Div titleRow = new Div();
        titleRow.addClassName("title-row");

        H3 titleEl = new H3(entity.getRecipeName() != null ? entity.getRecipeName() : "Unknown Recipe");
        titleEl.addClassName("recipe-title");

        Span matchBadge = new Span(matchPercent + "% match");
        matchBadge.addClassName("match-badge");

        titleRow.add(titleEl, matchBadge);

        // Category chip derived from the cuisine_path in the database
        Span catChip = new Span(mapCategory(entity.getCuisinePath()));
        catChip.addClassName("category-chip");

        // Meta row: cook time and serving count
        Div metaRow = new Div();
        metaRow.addClassName("meta-row");
        int minutes = parseMinutes(entity);
        metaRow.add(new Span(minutes > 0 ? "⏱ " + minutes + " min" : "⏱ N/A"));
        metaRow.add(new Span("👥 " + (entity.getServings() != null ? entity.getServings() : "?") + " servings"));

        // Ingredient list (first 8 ingredients from the comma-separated DB field)
        Paragraph ingLabel = new Paragraph("Ingredients:");
        ingLabel.addClassName("ingredients-label");

        String[] ingredients = parseIngredients(entity.getIngredients());
        Span ingList = new Span(String.join(", ", ingredients));
        ingList.addClassName("ingredients-list");

        body.add(titleRow, catChip, metaRow, ingLabel, ingList, new Span(parseNutrition(entity.getNutrition())));
        card.add(imageArea, body);
        return card;
    }

    /*
     * Opens a modal dialog showing the logged-in user's profile information.
     * Displays the username, number of ingredients currently added, and a logout button.
     * Logging out clears the session and redirects to the login page.
     */

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        // Inactivity timer only starts when you are actually logged in and not in the login page
        if (VaadinSession.getCurrent().getAttribute("user") == null) return;

        getElement().executeJs(
            "var idleTimer, warnTimer;" +
            "var WARN_MS  = 540000;" +  // 9 minuter 540000
            "var IDLE_MS  = 600000;" +  // 10 minuter 600000
            "function resetTimers() {" +
            "  clearTimeout(idleTimer); clearTimeout(warnTimer);" +
            "  warnTimer = setTimeout(function() { $0.$server.warnInactivity(); }, WARN_MS);" +
            "  idleTimer = setTimeout(function() { $0.$server.logoutDueToInactivity(); }, IDLE_MS);" +
            "}" +
            "['mousemove','keydown','mousedown','touchstart','scroll','click']" +
            "  .forEach(function(e) { document.addEventListener(e, resetTimers, true); });" +
            "resetTimers();",
            getElement()
        );
    }

    // This is the message that pops up right before you get logged out
    // Duration is exactly 60000 ms, so right as you are getting logged out, thats when it goes away
    // You then have plenty of time to see the pop up message
    @ClientCallable
    public void warnInactivity() {
        Notification warning = new Notification(
            "You will be logged out in 1 minute due to inactivity.", 60000,
            Notification.Position.TOP_CENTER);
        warning.addThemeVariants(NotificationVariant.LUMO_WARNING);
        warning.open();
    }

    @ClientCallable
    public void logoutDueToInactivity() {
        VaadinSession.getCurrent().close();
        UI ui = UI.getCurrent();
        Notification done = new Notification(
            "You have been logged out due to inactivity.", 4000,
            Notification.Position.MIDDLE);
        done.addThemeVariants(NotificationVariant.LUMO_PRIMARY);
        done.open();
        ui.navigate("");
    }

    private void openProfileDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("My Profile");
        dialog.setWidth("360px");

        VerticalLayout content = new VerticalLayout();
        content.setSpacing(true);
        content.setPadding(false);
        content.setAlignItems(Alignment.CENTER);

        // Read the user object from the session to display their username
        User user = (User) VaadinSession.getCurrent().getAttribute("user");
        if (user != null) {
            // Avatar circle showing the first letter of the username
            Div avatar = new Div();
            avatar.addClassName("profile-avatar");
            avatar.add(new Span(user.getUsername().substring(0, 1).toUpperCase()));

            H3 username = new H3("@" + user.getUsername());
            content.add(avatar, username);
        } else {
            content.add(new Span("Not logged in."));
        }

        Span ingCount = new Span("Ingredients added: " + addedIngredients.size());
        ingCount.getStyle().set("color", "var(--lumo-secondary-text-color)");
        content.add(ingCount);
        Button inventoryBtn = new Button("Go to Inventory", e -> {
            dialog.close();
            UI.getCurrent().navigate("inventory");
        });
        // Logout: close the Vaadin session and navigate back to the login page
        Button logoutBtn = new Button("Logout", VaadinIcon.SIGN_OUT.create(), e -> {
            VaadinSession.getCurrent().close();
            dialog.close();
            UI.getCurrent().navigate("");
        });
        logoutBtn.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY);
        logoutBtn.setWidthFull();

        Button closeBtn = new Button("Close", e -> dialog.close());
        closeBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        closeBtn.setWidthFull();

        inventoryBtn.setWidthFull();
        content.add(inventoryBtn, logoutBtn, closeBtn);
        dialog.add(content);
        dialog.open();
    }
}
