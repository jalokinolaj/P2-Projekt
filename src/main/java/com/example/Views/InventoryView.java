package com.example.Views;

import java.util.List;

import com.example.Inventory;
import com.example.Services.InventoryServices;
import com.example.User;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
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
		Long userId = sessionUser != null ? sessionUser.getId() : null;
		// This view is user-specific, so block access when no session user exists.
		if (userId == null) {
			add(new Span("Please login first."));
			return;
		}

		HorizontalLayout headerLayout = new HorizontalLayout();
		headerLayout.setAlignItems(Alignment.CENTER);
		headerLayout.setSpacing(true);
		
		H2 title = new H2("My Inventory");
		Button returnBtn = new Button("Return to Recipes", VaadinIcon.ARROW_BACKWARD.create(), 
			e -> UI.getCurrent().navigate("main"));
		returnBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
		
		headerLayout.add(title, returnBtn);
		add(headerLayout);
		add(createForm(userId));
		add(configureInventoryGrid());
		add(new H2("Ingredients That Run Out First"));
		add(configureRunOutGrid());

		refreshData(userId);
	}

	private HorizontalLayout createForm(Long userId) {
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
					userId,
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
			refreshData(userId);
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
				Long userId = getSessionUserId();
				if (userId != null) {
					refreshData(userId);
				}
			});

			return new HorizontalLayout(editButton, deleteButton);
		}).setHeader("Actions");

		inventoryGrid.setWidthFull();
		return inventoryGrid;
	}

	private void openEditAmountDialog(Inventory item) {
		Long userId = getSessionUserId();
		if (userId == null) {
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

			boolean updated = inventoryServices.updateQuantity(userId, item.getId(), qty);
			if (!updated) {
				Notification.show("Could not update amount.");
				return;
			}

			Notification.show("Amount updated.");
			dialog.close();
			refreshData(userId);
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

	private void refreshData(Long userId) {
		// Grid 1 = full inventory, Grid 2 = "runs out first" ordering.
		List<Inventory> inventory = inventoryServices.getInventoryForUser(userId);
		inventoryGrid.setItems(inventory);
		runOutGrid.setItems(inventoryServices.getRunOutSoonForUser(userId));
	}

	private Long getSessionUserId() {
		User sessionUser = (User) VaadinSession.getCurrent().getAttribute("user");
		return sessionUser != null ? sessionUser.getId() : null;
	}
}
