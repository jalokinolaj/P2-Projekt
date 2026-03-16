package com.example;

import com.vaadin.flow.component.button.Button;

import java.security.PrivateKey;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.login.LoginOverlay;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.server.VaadinSession;
import java.util.Optional;
import com.vaadin.flow.router.Route;


@Route("")
	public class LoginView extends Composite<LoginOverlay> {
	 private final UserRepository userRepository;
	 	 
	 public LoginView(UserRepository userRepository) {
		this.userRepository = userRepository;
		
		LoginOverlay loginOverlay = getContent(); 
		loginOverlay.setTitle("Recipe App");
		loginOverlay.setDescription("Find new recipes");
		loginOverlay.setOpened(true);
		
		loginOverlay.addLoginListener(event -> {
			String username  = event.getUsername();
			String password  = event.getPassword();
			
			// make sure error state is reset for a fresh attempt
			loginOverlay.setError(false);
			
			Optional<User> user = userRepository.findByUsername(username);
			
			if (user.isPresent() && user.get().getPassword().equals(password)) {
				VaadinSession.getCurrent().setAttribute("username", username);
				UI.getCurrent().navigate("user");
			} else {
				Notification.show("Incorrect Username or Password");
				UI.getCurrent().getPage().reload(); // refresh to reset overlay
			}
		});
	
	 
		
		Button registerButton = new Button("Register", event -> {
            UI.getCurrent().navigate("register");
        });
		 
		loginOverlay.getFooter().add(registerButton);
	 }
	 

	}


