package com.example.Views;
import com.example.Services.RecipeRecommendation;
import com.example.Services.RecipeServices;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import java.util.List;
import java.util.stream.Collectors;

@Route("recipes")
@PageTitle("Recipes")
public class RecipeViewsGrid extends VerticalLayout {

    private final Grid<RecipeRecommendation> grid;
    private final String username;
    private List<RecipeRecommendation> recommendations;

    public RecipeViewsGrid(RecipeServices recipeServices) {
        this.grid = new Grid<>(RecipeRecommendation.class, false);
        // Session stores a User object — extract the username string from it
        com.example.User sessionUser = (com.example.User) VaadinSession.getCurrent().getAttribute("user");
        this.username = sessionUser != null ? sessionUser.getUsername() : null;

        // Recommendations depend on user inventory, so require a logged-in user.
        if (this.username == null || this.username.isBlank()) {
            add(new Span("Please login first."));
            return;
        }

        // Search field
        TextField searchField = new TextField();
        searchField.setPlaceholder("Search recipes by name");
        searchField.setWidth("100%");
        searchField.addValueChangeListener(event -> filterRecipes(event.getValue()));

        // Configure grid
        grid.addColumn(item -> item.recipe().getId()).setHeader("ID");
        grid.addColumn(item -> item.recipe().getRecipeName()).setHeader("Recipe Name");
        grid.addColumn(item -> String.format("%.0f%%", item.matchPercent())).setHeader("Match");
        grid.addColumn(RecipeRecommendation::missingIngredients).setHeader("Missing Ingredients");
        grid.addColumn(RecipeRecommendation::runOutFirstIngredient).setHeader("Runs Out First");
        grid.addColumn(item -> item.recipe().getRating()).setHeader("Rating");

        grid.addComponentColumn(item -> {
            Image img = new Image(item.recipe().getImgSrc(), item.recipe().getRecipeName());
            img.setWidth("80px");
            img.setHeight("80px");
            img.getStyle().set("object-fit", "cover").set("border-radius", "8px");
            img.getElement().setAttribute("onerror",
                "this.src='/images/placeholder.png'; this.onerror=null;");
            return img;
        }).setHeader("Picture");

        // Initial load: already ranked by match %, urgency, then rating.
        recommendations = recipeServices.getRankedRecipesForUser(username);
        grid.setItems(recommendations);

        setSizeFull();
        add(searchField, grid);
    }

    private void filterRecipes(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            grid.setItems(recommendations);
        } else {
            // Client-side filtering on top of the pre-ranked recommendations list.
            String normalized = searchTerm.trim().toLowerCase();
            grid.setItems(recommendations.stream()
                    .filter(item -> item.recipe().getRecipeName().toLowerCase().contains(normalized))
                    .collect(Collectors.toList()));
        }
    }
}