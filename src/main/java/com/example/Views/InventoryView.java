package com.example.Views;

import java.util.List;

import com.example.Inventory;
import com.example.Services.InventoryServices;
import com.example.User;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

@Route("inventory")
@PageTitle("Inventory")
public class InventoryView extends VerticalLayout {

	private final InventoryServices inventoryServices;
	private final Grid<Inventory> inventoryGrid;
	private final Grid<Inventory> runOutGrid;

	public InventoryView(InventoryServices inventoryServices) {
		this.inventoryServices = inventoryServices;
		this.inventoryGrid = new Grid<>(Inventory.class, false);
		this.runOutGrid = new Grid<>(Inventory.class, false);

		User sessionUser = (User) VaadinSession.getCurrent().getAttribute("user");
		String username = sessionUser != null ? sessionUser.getUsername() : null;
		// This view is user-specific, so block access when no session user exists.
		if (username == null || username.isBlank()) {
			add(new Span("Please login first."));
			return;
		}

		add(new H2("My Inventory"));
		add(createForm(username));
		add(configureInventoryGrid());
		add(new H2("Ingredients That Run Out First"));
		add(configureRunOutGrid());

		refreshData(username);
	}

	private HorizontalLayout createForm(String username) {
		TextField ingredientName = new TextField("Ingredient");
		ingredientName.setPlaceholder("Eggs, milk, rice...");

		NumberField quantity = new NumberField("Quantity");
		quantity.setMin(0);

		Select<String> unit = new Select<>();
		unit.setLabel("Unit");
		unit.setItems("oz", "fl oz", "cups", "tbsp", "tsp", "lb", "pt", "qt", "gal", "pcs");
		unit.setValue("oz");

		NumberField minimumQuantity = new NumberField("Low stock threshold");
		minimumQuantity.setMin(0);
		minimumQuantity.setValue(1.0);

		DatePicker expiryDate = new DatePicker("Expiry date");

		Button saveButton = new Button("Save ingredient", click -> {
			// Basic guard clauses keep invalid values out of the database.
			if (ingredientName.getValue() == null || ingredientName.getValue().isBlank()) {
				Notification.show("Ingredient name is required.");
				return;
			}

			if (quantity.getValue() == null || quantity.getValue() < 0.0) {
				Notification.show("Quantity must be 0 or higher.");
				return;
			}

			if (unit.getValue() == null || unit.getValue().isBlank()) {
				Notification.show("Please select a unit.");
				return;
			}

			Double minQtyValue = minimumQuantity.getValue();
			double minQty = minQtyValue == null ? 0.0 : minQtyValue;

			inventoryServices.addOrUpdateIngredient(
					username,
					ingredientName.getValue(),
					quantity.getValue(),
					unit.getValue(),
					minQty,
					expiryDate.getValue());

			Notification.show("Ingredient saved.");
			// Reset fields and reload grids so UI reflects the newly saved data.
			ingredientName.clear();
			quantity.clear();
			expiryDate.clear();
			refreshData(username);
		});

		Button backButton = new Button("Go back", event ->
            UI.getCurrent().navigate("main")
        );

		return new HorizontalLayout(ingredientName, quantity, unit,
			minimumQuantity, expiryDate, saveButton, backButton);
	}

	private Grid<Inventory> configureInventoryGrid() {
		inventoryGrid.addColumn(Inventory::getIngredientName).setHeader("Ingredient");
		inventoryGrid.addColumn(Inventory::getQuantity).setHeader("Quantity");
		inventoryGrid.addColumn(Inventory::getUnit).setHeader("Unit");
		inventoryGrid.addColumn(Inventory::getMinimumQuantity).setHeader("Low stock threshold");
		inventoryGrid.addColumn(item -> item.getExpiryDate() == null ? "-" : item.getExpiryDate().toString())
				.setHeader("Expiry date");

		inventoryGrid.addComponentColumn(item -> {
			Button editButton = new Button("Edit amount", click -> openEditAmountDialog(item));
			Button deleteButton = new Button("Delete", click -> {
				inventoryServices.deleteInventoryItem(item.getId());
				String username = getSessionUsername();
				refreshData(username);
			});

			return new HorizontalLayout(editButton, deleteButton);
		}).setHeader("Actions");

		inventoryGrid.setWidthFull();
		return inventoryGrid;
	}

	private void openEditAmountDialog(Inventory item) {
		String username = getSessionUsername();
		if (username == null || username.isBlank()) {
			Notification.show("Please login first.");
			return;
		}

		Dialog dialog = new Dialog();
		dialog.setHeaderTitle("Edit amount: " + item.getIngredientName());

		NumberField newQuantity = new NumberField("New quantity");
		newQuantity.setMin(0);
		newQuantity.setValue(item.getQuantity());

		Button cancelButton = new Button("Cancel", click -> dialog.close());
		Button saveButton = new Button("Save", click -> {
			Double qty = newQuantity.getValue();
			if (qty == null || qty < 0.0) {
				Notification.show("Quantity must be 0 or higher.");
				return;
			}

			boolean updated = inventoryServices.updateQuantity(username, item.getId(), qty);
			if (!updated) {
				Notification.show("Could not update amount.");
				return;
			}

			Notification.show("Amount updated.");
			dialog.close();
			refreshData(username);
		});

		dialog.add(new VerticalLayout(newQuantity));
		dialog.getFooter().add(cancelButton, saveButton);
		dialog.open();
	}

	private Grid<Inventory> configureRunOutGrid() {
		runOutGrid.addColumn(Inventory::getIngredientName).setHeader("Ingredient");
		runOutGrid.addColumn(Inventory::getQuantity).setHeader("Current quantity");
		runOutGrid.addColumn(Inventory::getMinimumQuantity).setHeader("Threshold");
		runOutGrid.addColumn(item -> item.getExpiryDate() == null ? "-" : item.getExpiryDate().toString())
				.setHeader("Expiry date");
		runOutGrid.setWidthFull();
		return runOutGrid;
	}

	private void refreshData(String username) {
		// Grid 1 = full inventory, Grid 2 = "runs out first" ordering.
		List<Inventory> inventory = inventoryServices.getInventoryForUser(username);
		inventoryGrid.setItems(inventory);
		runOutGrid.setItems(inventoryServices.getRunOutSoonForUser(username));
	}

	private String getSessionUsername() {
		User sessionUser = (User) VaadinSession.getCurrent().getAttribute("user");
		return sessionUser != null ? sessionUser.getUsername() : null;
	}
}
