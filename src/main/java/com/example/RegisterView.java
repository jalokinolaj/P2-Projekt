package com.example;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import java.util.Set;

@Route("register")
public class RegisterView extends Composite<VerticalLayout> {
	private final UserRepository userRepository;
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

    public RegisterView(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

	@Override
	protected VerticalLayout initContent() {
		TextField username = new TextField("Username");
		PasswordField password1 = new PasswordField("Password");
		PasswordField password2 = new PasswordField("Confirm Password");
		Select<String> diet = new Select<>();
		diet.setLabel("Diet");
		diet.setItems("none", "Vegan", "Vegetarian", "Pescatarian", "Omnivore");
		diet.setValue("none");
		
	CheckboxGroup<String> allergies = new CheckboxGroup<>();
	allergies.setLabel("Allergener (EU-14)");
	allergies.setItems(EU14_ALLERGENS);
	allergies.setHelperText("Vælg dine Allergenerr");
		
		return new VerticalLayout(
				new H2("Register"),
				username,
				password1,
				password2,
				diet,
				allergies,
				new Button("Send", event -> register(
				username.getValue(),
				password1.getValue(),
				password2.getValue(),
				diet.getValue(),
				allergies.getValue()
				))
		);
	}
	
	private void register(String username, String password1, String password2, String diet, Set<String> allergies) {
		if (username.isEmpty() || password1.isEmpty() || password2.isEmpty()) {
            Notification.show("Please fill all fields");
            return;
        }

        if (!password1.equals(password2)) {
            Notification.show("Passwords do not match");
            return;
        }
        
        if (userRepository.findByUsername(username).isPresent()) {
            Notification.show("Username already exists");
            return;
        }

        String allergiesAsString = String.join(",", allergies);
        User user = new User(username, password1, diet, allergiesAsString);
        userRepository.save(user);
        
        Notification.show("Registration successful");
        UI.getCurrent().navigate("");
	}
}
