package com.example;

import java.util.Optional;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.login.LoginOverlay;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;


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
			
			Optional<User> user = userRepository.findByUsername(username);
			
			if (user.isPresent() && user.get().getPassword().equals(password)) {
				loginOverlay.setError(false);
				VaadinSession.getCurrent().setAttribute("user", user.get());
				UI.getCurrent().navigate("main");
			} else {
				loginOverlay.setError(true);
				Notification.show("Incorrect Username or Password");
			}
		});
	
	
		
		Button registerButton = new Button("Register", event -> {
            UI.getCurrent().navigate("register");
        });
		
		loginOverlay.getFooter().add(registerButton);
	}
	

	}


