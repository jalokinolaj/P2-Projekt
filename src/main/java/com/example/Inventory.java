package com.example;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "inventory_items")
public class Inventory {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	// Owner of this ingredient row; used to keep each user's inventory separate.
	private String username;

	@Column(name = "ingredient_name", nullable = false)
	// Ingredient identifier shown in UI and matched against recipe requirements.
	private String ingredientName;

	@Column(nullable = false)
	// Current amount available in stock.
	private Double quantity;

	@Column(name = "normalized_quantity", nullable = false)
	// Normalized quantity for matching against recipe requirements, for example: grams of total weight instead of pieces.
	private Double normalizedQuantity;	

	@Column(nullable = false)
	// Unit for quantity, for example: pcs, g, ml.
	private String unit;

	@Column(name = "normalized_unit", nullable = false)
	// Normalized unit for matching against recipe requirements, for example: pcs, g, ml.
	private String normalizedUnit;

	@Column(name = "minimum_quantity", nullable = false)
	// Low-stock threshold used by urgency/risk logic.
	private Double minimumQuantity;

	@Column(name = "expiry_date")
	// Optional date for expiry-aware recipe prioritization.
	private LocalDate expiryDate;

	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	public Inventory() {
	}

	public Inventory(String username, String ingredientName, Double quantity, Double normalizedQuantity, String unit, String normalizedUnit, Double minimumQuantity,
			LocalDate expiryDate) {
		// This timestamp helps when you later want "recently changed" sorting.
		this.username = username;
		this.ingredientName = ingredientName;
		this.quantity = quantity;
		this.normalizedQuantity = normalizedQuantity;
		this.unit = unit;
		this.normalizedUnit = normalizedUnit;
		this.minimumQuantity = minimumQuantity;
		this.expiryDate = expiryDate;
		this.updatedAt = LocalDateTime.now();
	}

	public Long getId() {
		return id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) { 
		this.username = username;
	}

	public String getIngredientName() {
		return ingredientName;
	}

	public void setIngredientName(String ingredientName) {
		this.ingredientName = ingredientName;
	}

	public Double getQuantity() {
		return quantity;
	}

	public void setQuantity(Double quantity) {
		this.quantity = quantity;
	}
	public Double getNormalizedQuantity() {
		return normalizedQuantity;
	}
	public void setNormalizedQuantity(Double normalizedQuantity) {
		this.normalizedQuantity = normalizedQuantity;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public String getNormalizedUnit() {
		return normalizedUnit;
	}

	public void setNormalizedUnit(String normalizedUnit) {
		this.normalizedUnit = normalizedUnit;
	}

	public Double getMinimumQuantity() {
		return minimumQuantity;
	}

	public void setMinimumQuantity(Double minimumQuantity) {
		this.minimumQuantity = minimumQuantity;
	}

	public LocalDate getExpiryDate() {
		return expiryDate;
	}

	public void setExpiryDate(LocalDate expiryDate) {
		this.expiryDate = expiryDate;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}
}
