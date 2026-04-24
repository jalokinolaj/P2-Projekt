package com.example.Services;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

import com.example.Inventory;
import com.example.Repositories.InventoryRepository;

@Service
public class InventoryServices {

	@Autowired
	private InventoryRepository inventoryRepository;

	public List<Inventory> getInventoryForUser(Long userId) {
		// Main inventory list: sorted by expiry first so older items are visible sooner.
		return inventoryRepository.findByUserIdOrderByExpiryDateAscIngredientNameAsc(userId);
	}

	public List<Inventory> getRunOutSoonForUser(Long userId) {
		// "Run out first" list: smallest quantity first.
		return inventoryRepository.findByUserIdOrderByQuantityAsc(userId);
	}

	public Inventory addOrUpdateIngredient(Long userId, String ingredientName, Double quantity, String unit,
			Double minimumQuantity, LocalDate expiryDate) {
		// Upsert: update existing ingredient row for this user, otherwise create a new one.
		String trimmedIngredientName = ingredientName.trim();
		String trimmedUnit = unit.trim();

		Inventory item = inventoryRepository.findByUserIdAndIngredientNameIgnoreCase(userId, trimmedIngredientName)
				.orElseGet(Inventory::new);

		item.setUserId(userId);
		item.setIngredientName(trimmedIngredientName);
		item.setQuantity(quantity);
		item.setUnit(trimmedUnit);
		// Keep normalized values in sync until dedicated unit conversion is introduced.
		item.setNormalizedQuantity(quantity);
		item.setNormalizedUnit(trimmedUnit);
		item.setMinimumQuantity(minimumQuantity);
		item.setExpiryDate(expiryDate);
		item.setUpdatedAt(LocalDateTime.now());

		return inventoryRepository.save(item);
	}

	public boolean updateQuantity(Long userId, Long id, Double quantity) {
		if (quantity == null || quantity < 0.0) {
			return false;
		}

		return inventoryRepository.findByIdAndUserId(id, userId)
				.map(item -> {
					item.setQuantity(quantity);
					// Keep normalized values in sync until dedicated unit conversion is introduced.
					item.setNormalizedQuantity(quantity);
					item.setUpdatedAt(LocalDateTime.now());
					inventoryRepository.save(item);
					return true;
				})
				.orElse(false);
	}

	public boolean deleteInventoryItem(Long id) {
		try {
			inventoryRepository.deleteById(id);
			return true;
		} catch (EmptyResultDataAccessException ex) {
			// Return false instead of crashing if the row was already removed.
			return false;
		}
	}
}
