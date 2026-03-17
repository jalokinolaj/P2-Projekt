package com.example.Views;
import java.lang.String;
import com.example.Recipes;
import com.example.Services.RecipeServices;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route("recipes")
@PageTitle("Recipes")
public class RecipeViewsGrid extends VerticalLayout {

    public RecipeViewsGrid(RecipeServices recipeServices) {
        Grid<Recipes> grid = new Grid<>(Recipes.class, false);

        // Use the exact Java field names from your Recipes class
        grid.addColumn(Recipes::getId).setHeader("ID");
        grid.addColumn(Recipes::getRecipeName).setHeader("Recipe Name");
        grid.addColumn(Recipes::getRating).setHeader("Rating");

        // Image column needs a component renderer
        grid.addComponentColumn(recipe -> {
            Image img = new Image(recipe.getImgSrc(), recipe.getRecipeName());
            img.setWidth("80px");
            img.setHeight("80px");
            img.getStyle().set("object-fit", "cover").set("border-radius", "8px");
            img.getElement().setAttribute("onerror",
                "this.src='/images/placeholder.png'; this.onerror=null;");
            return img;
        }).setHeader("Picture");

        // Load data sorted by rating
        grid.setItems(recipeServices.getRecipesSortedByRating());
        setSizeFull();
        add(grid);
    }
}