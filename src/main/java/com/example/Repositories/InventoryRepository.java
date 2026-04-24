package com.example.Repositories;

import com.example.Inventory;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
	List<Inventory> findByUsernameIDOrderByExpiryDateAscIngredientNameAsc(int usernameID);

	List<Inventory> findByUsernameIDOrderByQuantityAsc(int usernameID);

	Optional<Inventory> findByUsernameIDAndIngredientNameIgnoreCase(int usernameID, String ingredientName);

	Optional<Inventory> findByIdAndUsernameID(Long id, int usernameID);
}