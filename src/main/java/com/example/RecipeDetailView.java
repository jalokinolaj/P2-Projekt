package com.example;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.UI;

@Route("recipe/:id")
public class RecipeDetailView extends VerticalLayout implements BeforeEnterObserver {

    private final RecipeRepository recipeRepository;

    public RecipeDetailView(RecipeRepository recipeRepository) {
        this.recipeRepository = recipeRepository;

        setPadding(true);
        setSpacing(true);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        removeAll(); // Skærmen bliver clear

        String idStr = event.getRouteParameters().get("id").orElse(null);

        if (idStr == null) {
            add(new H1("Recipe not found"));
            return;
        }

        try {
            Integer id = Integer.parseInt(idStr); // finder ret id/som integer

            RecipeEntity recipe = recipeRepository.findById(id).orElse(null);

            if (recipe == null) {
                add(new H1("Recipe not found"));
                return;
            }

            // tilbage knap
            Button back = new Button("← Back", e ->
                UI.getCurrent().navigate("main")
            );

            // Retnavn
            H1 title = new H1(recipe.getRecipeName());

            // Billed
            if (recipe.getImgSrc() != null && !recipe.getImgSrc().isEmpty()) {
                Image image = new Image(recipe.getImgSrc(), recipe.getRecipeName());
                image.setWidth("300px");
                add(back, title, image);
            } else {
                add(back, title);
            }

            // tid
            add(new Paragraph("Time: " +
                (recipe.getTotalTime() != null ? recipe.getTotalTime() : "N/A")));

            // hvor mange serveringer
            add(new Paragraph("Servings: " +
                (recipe.getServings() != null ? recipe.getServings() : "N/A")));

            // ingredienser
            add(new H2("Ingredients"));
            add(new Paragraph(recipe.getIngredients()));

            // Dette skal ændres når instruktioner bliver added til recipyEntity
            add(new H2("Instructions"));
            add(new Paragraph(recipe.getDirections()));

        } catch (Exception e) {
            add(new H1("Error loading recipe"));
        }
    }
}
	
	
	
	
	
	
	







   
       
