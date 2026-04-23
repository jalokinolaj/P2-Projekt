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

	@Autowired
	private UnitConverterService unitConverterService;

	public List<Inventory> getInventoryForUser(String username) {
		// Main inventory list: sorted by expiry first so older items are visible sooner.
		return inventoryRepository.findByUsernameOrderByExpiryDateAscIngredientNameAsc(username);
	}

	public List<Inventory> getRunOutSoonForUser(String username) {
		// "Run out first" list: smallest quantity first.
		return inventoryRepository.findByUsernameOrderByQuantityAsc(username);
	}

	public Inventory addOrUpdateIngredient(String username, String ingredientName, Double quantity, String unit,
			Double minimumQuantity, LocalDate expiryDate) {
		// Upsert: update existing ingredient row for this user, otherwise create a new one.
		String trimmedIngredientName = ingredientName.trim();
		String trimmedUnit = unitConverterService.normalizeUnitLabel(unit);
		UnitConverterService.ConversionResult conversion = unitConverterService.normalize(quantity, trimmedUnit);

		Inventory item = inventoryRepository.findByUsernameAndIngredientNameIgnoreCase(username, trimmedIngredientName)
				.orElseGet(Inventory::new);

		item.setUsername(username);
		item.setIngredientName(trimmedIngredientName);
		item.setQuantity(quantity);
		item.setUnit(trimmedUnit);
		item.setNormalizedQuantity(conversion.normalizedQuantity());
		item.setNormalizedUnit(conversion.normalizedUnit());
		item.setMinimumQuantity(minimumQuantity);
		item.setExpiryDate(expiryDate);
		item.setUpdatedAt(LocalDateTime.now());

		return inventoryRepository.save(item);
	}

	public boolean updateQuantity(String username, Long id, Double quantity) {
		if (quantity == null || quantity < 0.0) {
			return false;
		}

		return inventoryRepository.findByIdAndUsername(id, username)
				.map(item -> {
					UnitConverterService.ConversionResult conversion = unitConverterService.normalize(quantity, item.getUnit());
					item.setQuantity(quantity);
					item.setNormalizedQuantity(conversion.normalizedQuantity());
					item.setNormalizedUnit(conversion.normalizedUnit());
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
