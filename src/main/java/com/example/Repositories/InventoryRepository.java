package com.example.Repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.Inventory;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
	List<Inventory> findByUserIdOrderByExpiryDateAscIngredientNameAsc(Long userId);

	List<Inventory> findByUserIdOrderByQuantityAsc(Long userId);

	Optional<Inventory> findByUserIdAndIngredientNameIgnoreCase(Long userId, String ingredientName);

	Optional<Inventory> findByIdAndUserId(Long id, Long userId);
}