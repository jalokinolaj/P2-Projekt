package com.example;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

@Route("register")
public class RegisterView extends Composite<Component> {
	private final UserRepository userRepository;

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
		return new VerticalLayout(
				new H2("Register"),
				username,
				password1,
				password2,
				diet,
				new Button("Send", event -> register (
				username.getValue(),
				password1.getValue(),
				password2.getValue(),	
				diet.getValue()
				))		
		);
	}
	
	private void register(String username, String password1, String password2, String diet) {
		
		
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
        User user = new User(username, password1, diet);
        userRepository.save(user);
        
        Notification.show("Registration successful");
        UI.getCurrent().navigate("");
	}
}


