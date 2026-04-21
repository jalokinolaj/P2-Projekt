package com.example.Repositories;

import com.example.Inventory;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
	List<Inventory> findByUsernameOrderByExpiryDateAscIngredientNameAsc(String username);

	List<Inventory> findByUsernameOrderByQuantityAsc(String username);

	Optional<Inventory> findByUsernameAndIngredientNameIgnoreCase(String username, String ingredientName);

	Optional<Inventory> findByIdAndUsername(Long id, String username);
}