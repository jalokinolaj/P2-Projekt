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

	@Column(name = "user_id", nullable = false)
	// Owner of ingredient row with user_id
	private Long userId;

	@Column(name = "ingredient_name", nullable = false)

	private String ingredientName;

	@Column(nullable = false)
	// Current amount available in stock.
	private Double quantity;

	@Column(name = "normalized_quantity", nullable = false)
	// normalized quantity not really used but may be useful.
	private Double normalizedQuantity;	

	@Column(nullable = false)
	// Unit for quantity, for example: pcs, lb, qt.
	private String unit;

	@Column(name = "normalized_unit", nullable = false)
	// again not useed
	private String normalizedUnit;

	@Column(name = "minimum_quantity", nullable = false)
	// Low-stock threshold not implemented correctly.
	private Double minimumQuantity;

	@Column(name = "expiry_date")
	private LocalDate expiryDate;

	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	public Inventory() {
	}

	public Inventory(Long userId, String ingredientName, Double quantity, Double normalizedQuantity, String unit, String normalizedUnit, Double minimumQuantity,
			LocalDate expiryDate) {
		// timestamp to track when ingredient updated
		this.userId = userId;
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

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
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
