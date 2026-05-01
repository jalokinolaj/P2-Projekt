package com.example.Repositories;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import com.example.Inventory;

@DataJpaTest
class InventoryRepositoryTest {

    @Autowired
    private InventoryRepository inventoryRepository;

    @Test
    void findsIngredientCaseInsensitivelyWithinTheSameUser() {
        Inventory item = new Inventory(3L, "Tomato", 4.0, 4.0, "g", "g", 1.0, LocalDate.of(2026, 5, 15));
        inventoryRepository.save(item);

        assertThat(inventoryRepository.findByUserIdAndIngredientNameIgnoreCase(3L, "tOmAtO"))
                .isPresent()
                .get()
                .extracting(Inventory::getIngredientName)
                .isEqualTo("Tomato");
    }

    @Test
    void returnsInventorySortedByExpiryDateThenIngredientNameForUser() {
        inventoryRepository.saveAll(List.of(
                new Inventory(8L, "Zucchini", 1.0, 1.0, "pcs", "pcs", 1.0, LocalDate.of(2026, 5, 20)),
                new Inventory(8L, "Apple", 1.0, 1.0, "pcs", "pcs", 1.0, LocalDate.of(2026, 5, 10)),
                new Inventory(8L, "Banana", 1.0, 1.0, "pcs", "pcs", 1.0, LocalDate.of(2026, 5, 10)),
                new Inventory(9L, "Ignored", 1.0, 1.0, "pcs", "pcs", 1.0, LocalDate.of(2026, 5, 1))));

        List<Inventory> ordered = inventoryRepository.findByUserIdOrderByExpiryDateAscIngredientNameAsc(8L);

        assertThat(ordered).extracting(Inventory::getIngredientName)
                .containsExactly("Apple", "Banana", "Zucchini");
    }

    @Test
    void returnsInventorySortedByQuantityForRunOutSoonList() {
        inventoryRepository.saveAll(List.of(
                new Inventory(12L, "Salt", 5.0, 5.0, "g", "g", 1.0, null),
                new Inventory(12L, "Pepper", 1.0, 1.0, "g", "g", 1.0, null),
                new Inventory(12L, "Sugar", 3.0, 3.0, "g", "g", 1.0, null)));

        List<Inventory> ordered = inventoryRepository.findByUserIdOrderByQuantityAsc(12L);

        assertThat(ordered).extracting(Inventory::getIngredientName)
                .containsExactly("Pepper", "Sugar", "Salt");
    }
}