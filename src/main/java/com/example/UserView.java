package com.example;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.component.button.Button;
@Route("user")
public class UserView extends VerticalLayout {
	
	public UserView() {
        String username = (String) VaadinSession.getCurrent().getAttribute("username");

		add(new H1("Welcome " + username));
		
		
		Button profileButton = new Button("My Profile", e -> {
		    getUI().ifPresent(ui -> ui.navigate("profile"));
		});
		add(profileButton);
	}
	
}