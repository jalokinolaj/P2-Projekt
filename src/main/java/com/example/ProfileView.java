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

@Route("profile")
public class ProfileView extends VerticalLayout {

    private final Binder<User> binder = new Binder<>(User.class);

    private User currentUser;

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

        Button saveButton = new Button("Save Changes", event -> saveProfile());

        FormLayout formLayout = new FormLayout();
        formLayout.add(username, diet);

        binder.bind(username, User::getUsername, User::setUsername);
        binder.bind(diet, User::getDiet, User::setDiet);

        binder.readBean(currentUser);

        add(title, formLayout, saveButton);
    }

    private void saveProfile() {
        try {
            binder.writeBean(currentUser);
            userRepository.save(currentUser);

            VaadinSession.getCurrent().setAttribute("username", currentUser.getUsername());

            Notification.show("Profile updated");
        } catch (Exception e) {
            Notification.show("Error saving profile");
        }
    }
}