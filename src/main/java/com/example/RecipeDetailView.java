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
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinSession;


@Route("recipe/:id")
public class RecipeDetailView extends VerticalLayout implements BeforeEnterObserver {

    private final RecipeRepository recipeRepository;
    private final SavedRecipeRepository savedRecipeRepository;


    public RecipeDetailView(RecipeRepository recipeRepository,
    						SavedRecipeRepository savedRecipeRepository) {
        this.recipeRepository = recipeRepository;
        this.savedRecipeRepository = savedRecipeRepository;

        setPadding(true);
        setSpacing(true);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        removeAll(); // Skærmen bliver clear
        
        User currentUser = (User) VaadinSession.getCurrent().getAttribute("user");

        if (currentUser == null) {
            UI.getCurrent().navigate("");
            return;
        }
        
        final String username = currentUser.getUsername();


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
            
            boolean alreadySaved = savedRecipeRepository
            		.existsByUsernameAndRecipeId(username, recipe.getId());
            
            Button saveButton = new Button(alreadySaved ? "Remove Recipe" : "Save Recipe");

            saveButton.addClickListener(e -> {
                boolean isSaved = savedRecipeRepository
                                    .existsByUsernameAndRecipeId(username, recipe.getId());
                if (isSaved) {
                    savedRecipeRepository.deleteByUsernameAndRecipeId(username, recipe.getId());
                    saveButton.setText("Save Recipe");

                    // Undo notification — re-saves the recipe if clicked within 5 seconds
                    Notification notification = new Notification();
                    notification.setDuration(5000);
                    notification.setPosition(Notification.Position.BOTTOM_START);
                    notification.addThemeVariants(NotificationVariant.LUMO_CONTRAST);

                    HorizontalLayout notifLayout = new HorizontalLayout();
                    notifLayout.setAlignItems(FlexComponent.Alignment.CENTER);
                    notifLayout.add(new Span("Recipe removed."));

                    Button undoBtn = new Button("Undo", undoEvent -> {
                        SavedRecipeEntity resaved = new SavedRecipeEntity();
                        resaved.setUsername(username);
                        resaved.setRecipe(recipe);
                        savedRecipeRepository.save(resaved);
                        saveButton.setText("Remove Recipe");
                        notification.close();
                    });
                    undoBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
                    notifLayout.add(undoBtn);
                    notification.add(notifLayout);
                    notification.open();
                } else {
                    SavedRecipeEntity saved = new SavedRecipeEntity();
                    saved.setUsername(username);
                    saved.setRecipe(recipe);
                    savedRecipeRepository.save(saved);
                    saveButton.setText("Remove Recipe");
                }
            });
            
            add(saveButton);

            
        

        } catch (Exception e) {
            add(new H1("Error loading recipe"));
            
            

        }
    }
}
