package com.example;

import java.util.Optional;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import java.util.Set;

@Route("profile")
public class ProfileView extends VerticalLayout {

    private final UserRepository userRepository;
    private final Binder<User> binder = new Binder<>(User.class);

    private User currentUser;
    private CheckboxGroup<String> allergies;

        private static final String[] EU14_ALLERGENS = {
            "Gluten",
            "Crustaceans",
            "Eggs",
            "Fish",
            "Peanuts",
            "Soy",
            "Milk",
            "Nuts",
            "Celery",
            "Mustard",
            "Sesame",
            "Sulphur dioxide",
            "Lupin",
            "Molluscs"
    };

    public ProfileView(UserRepository userRepository) {
        this.userRepository = userRepository;

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

        // Pre-select the user's saved allergies if they have any
        if (currentUser.getAllergies() != null && !currentUser.getAllergies().isBlank()) {
            allergies.setValue(Set.of(currentUser.getAllergies().split(",")));
        }

        add(title, formLayout, saveButton);
    }

    private void saveProfile() {
        try {
            binder.writeBean(currentUser);
            currentUser.setAllergies(String.join(",", allergies.getValue()));
            userRepository.save(currentUser);
            // Store the full updated User object in session (not just the username string)
            VaadinSession.getCurrent().setAttribute("user", currentUser);

            Notification.show("Profile updated");
        } catch (Exception e) {
            Notification.show("Error saving profile: " + e.getMessage());
        }
    }
}
