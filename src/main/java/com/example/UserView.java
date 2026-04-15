package com.example;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

@Route("user")
public class UserView extends VerticalLayout {
	
	public UserView() {
        String username = (String) VaadinSession.getCurrent().getAttribute("username");
		Image image = new Image("https://www.allrecipes.com/thmb/1I95oiTGz6aEpuHd2DAr33w7Mgg=/1500x0/filters:no_upscale():max_bytes(150000):strip_icc()/12682-apple-pie-by-grandma-ople-natasha-titanov-1x1-1-7f1cd9f631d24ff0b90cac0163247686.jpg", "Apple Pie");
		Button inventoryButton = new Button("Manage Inventory", click -> getUI().ifPresent(ui -> ui.navigate("inventory")));
		Button recipesButton = new Button("Recommended Recipes", click -> getUI().ifPresent(ui -> ui.navigate("recipes")));
		HorizontalLayout navigation = new HorizontalLayout(inventoryButton, recipesButton);
		add(new H1("Welcome " + username));
		add(navigation);
		add(image);
	}
	
}