package com.example;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import java.util.Set;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

@Route("profile")
public class ProfileView extends VerticalLayout {

    private final UserRepository userRepository;
    private final SavedRecipeRepository savedRecipeRepository;
    private final Binder<User> binder = new Binder<>(User.class);
        
    private User currentUser;
    private CheckboxGroup<String> allergies;

    private static final String[] EU14_ALLERGENS = {
        "Gluten", "Crustaceans", "Eggs", "Fish", "Peanuts",
        "Soy", "Milk", "Nuts", "Celery", "Mustard",
        "Sesame", "Sulphur dioxide", "Lupin", "Molluscs"
    };

    public ProfileView(UserRepository userRepository,
                       SavedRecipeRepository savedRecipeRepository) {
        this.userRepository = userRepository;
        this.savedRecipeRepository = savedRecipeRepository;

        User sessionUser = (User) VaadinSession.getCurrent().getAttribute("user");

        if (sessionUser == null) {
            add(new H2("You are not logged in."));
            return;
        }

        Optional<User> optionalUser = userRepository.findByUsername(sessionUser.getUsername());

        if (optionalUser.isEmpty()) {
            add(new H2("User not found."));
            return;
        }

        currentUser = optionalUser.get();

        // Profile form section
        H2 title = new H2("My Profile");
        TextField username = new TextField("Name");

        Select<String> diet = new Select<>();
        diet.setLabel("Dietary Preference");
        diet.setItems("None", "Vegan", "Vegetarian", "Pescatarian", "Omnivore");

        allergies = new CheckboxGroup<>();
        allergies.setLabel("Allergens (EU-14)");
        allergies.setItems(EU14_ALLERGENS);

        Button saveButton = new Button("Save Changes", event -> saveProfile());

        FormLayout formLayout = new FormLayout();
        formLayout.add(username, diet, allergies);

        binder.bind(username, User::getUsername, User::setUsername);
        binder.bind(diet, User::getDiet, User::setDiet);
        binder.readBean(currentUser);

        if (currentUser.getAllergies() != null && !currentUser.getAllergies().isBlank()) {
            allergies.setValue(Set.of(currentUser.getAllergies().split(",")));
        }

        Button backButton = new Button("Go back", event -> UI.getCurrent().navigate("main"));

        add(title, formLayout, saveButton, backButton);

        // Saved recipes section
        add(new H2("Saved Recipes"));

        List<SavedRecipeEntity> savedRecipes = savedRecipeRepository.findByUsername(currentUser.getUsername());

        if (savedRecipes.isEmpty()) {
            add(new Paragraph("You have not saved any recipes yet."));
        } else {
            FlexLayout grid = new FlexLayout();
            grid.getStyle()
                .set("flex-wrap", "wrap")
                .set("gap", "16px")
                .set("padding", "16px 0");

            for (SavedRecipeEntity saved : savedRecipes) {
                grid.add(createRecipeCard(saved.getRecipe()));
            }

            add(grid);
        }
    }

    private VerticalLayout createRecipeCard(RecipeEntity recipe) {
        VerticalLayout card = new VerticalLayout();
        card.getStyle()
            .set("border", "1px solid #ccc")
            .set("border-radius", "8px")
            .set("padding", "12px")
            .set("width", "200px")
            .set("cursor", "pointer")
            .set("box-shadow", "2px 2px 6px rgba(0,0,0,0.1)");
        card.setPadding(false);
        card.setSpacing(false);

        if (recipe.getImgSrc() != null && !recipe.getImgSrc().isEmpty()) {
            Image image = new Image(recipe.getImgSrc(), recipe.getRecipeName());
            image.setWidth("100%");
            image.getStyle().set("border-radius", "6px 6px 0 0");
            card.add(image);
        }

        H3 name = new H3(recipe.getRecipeName());
        name.getStyle().set("font-size", "0.95rem").set("margin", "8px 8px 4px");
        card.add(name);

        if (recipe.getTotalTime() != null) {
            Paragraph time = new Paragraph("Time: " + recipe.getTotalTime());
            time.getStyle().set("margin", "0 8px 4px").set("font-size", "0.85rem");
            card.add(time);
        }

        if (recipe.getServings() != null) {
            Paragraph servings = new Paragraph("Servings: " + recipe.getServings());
            servings.getStyle().set("margin", "0 8px 8px").set("font-size", "0.85rem");
            card.add(servings);
        }

        // Navigate to recipe detail when card is clicked
        card.addClickListener(e ->
            card.getUI().ifPresent(ui -> ui.navigate("recipe/" + recipe.getId()))
        );

        Button removeButton = new Button("Remove", e -> {
            savedRecipeRepository.deleteByUsernameAndRecipeId(currentUser.getUsername(), recipe.getId());
            card.setVisible(false);

            // Undo notification — re-saves the recipe if clicked within 5 seconds
            Notification notification = new Notification();
            notification.setDuration(5000);
            notification.setPosition(Notification.Position.BOTTOM_START);
            notification.addThemeVariants(NotificationVariant.LUMO_CONTRAST);

            HorizontalLayout notifLayout = new HorizontalLayout();
            notifLayout.setAlignItems(com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.CENTER);
            notifLayout.add(new Span("Recipe removed."));

            Button undoBtn = new Button("Undo", undoEvent -> {
                SavedRecipeEntity resaved = new SavedRecipeEntity();
                resaved.setUsername(currentUser.getUsername());
                resaved.setRecipe(recipe);
                savedRecipeRepository.save(resaved);
                card.setVisible(true);
                notification.close();
            });
            undoBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);

            notifLayout.add(undoBtn);
            notification.add(notifLayout);
            notification.open();
        });
        removeButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
        removeButton.getStyle().set("margin", "0 8px 8px");

        // Stop the remove click from bubbling up to the card navigation listener
        removeButton.getElement().executeJs(
            "this.addEventListener('click', e => e.stopPropagation())"
        );

        card.add(removeButton);

        return card;
    }

    private void saveProfile() {
        try {
            binder.writeBean(currentUser);
            currentUser.setAllergies(String.join(",", allergies.getValue()));
            userRepository.save(currentUser);
            VaadinSession.getCurrent().setAttribute("user", currentUser);
            Notification.show("Profile updated.");
        } catch (Exception e) {
            Notification.show("Error saving profile: " + e.getMessage());
        }
    }
}
