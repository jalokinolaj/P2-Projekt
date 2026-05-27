package com.example.Views;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
// admin view does nothing maybe some stuff for future usage.
@Route("admin")
public class AdminView extends VerticalLayout {

public AdminView() {
	add(new H1("Admin View!"));
	}
	

}

