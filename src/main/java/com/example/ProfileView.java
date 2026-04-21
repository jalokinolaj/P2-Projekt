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
            "Krebsdyr",
            "Æg",
            "Fisk",
            "Jordnødder",
            "Soja",
            "Mælk",
            "Nødder",
            "Selleri",
            "Sennep",
            "Sesam",
            "Svovldioxid",
            "Lupin",
            "Bløddyr"
    };

    public ProfileView(UserRepository userRepository) {
        this.userRepository = userRepository;

        String loggedInUsername = (String) VaadinSession.getCurrent().getAttribute("username");

        if (loggedInUsername == null) {
            add(new H2("You are not logged in."));
            return;
        }

        Optional<User> optionalUser = userRepository.findByUsername(loggedInUsername);

        if (optionalUser.isEmpty()) {
            add(new H2("User not found."));
            return;
        }

        currentUser = optionalUser.get();

        H2 title = new H2("My Profile");

        TextField username = new TextField("Name");

        Select<String> diet = new Select<>();
        diet.setLabel("Dietary Preference");
        diet.setItems("none", "Vegan", "Vegetarian", "Pescatarian", "Omnivore");

        allergies = new CheckboxGroup<>();
        allergies.setLabel("Allergener (EU-14)");
        allergies.setItems(EU14_ALLERGENS);
        
        Button saveButton = new Button("Save Changes", event -> saveProfile());

        FormLayout formLayout = new FormLayout();
        formLayout.add(username, diet, allergies);

        binder.bind(username, User::getUsername, User::		setUsername);
        binder.bind(diet, User::getDiet, User::setDiet);

        binder.readBean(currentUser);
        
        if (currentUser.getAllergies() != null && !currentUser.getAllergies().isBlank()) {
            allergies.setValue(Set.of(currentUser.getAllergies().split(",")));
            
        add(title, formLayout, saveButton);}
    }

    private void saveProfile() {
        try {
            binder.writeBean(currentUser);
            currentUser.setAllergies(String.join(",", allergies.getValue()));
            userRepository.save(currentUser);
            VaadinSession.getCurrent().setAttribute("username", currentUser.getUsername());

            Notification.show("Profile updated");
        } catch (Exception e) {
            Notification.show("Error saving profile"+ e.getMessage());
        }
    }
}		