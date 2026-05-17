package com.example.Services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.EmptyResultDataAccessException;

import com.example.Inventory;
import com.example.Repositories.InventoryRepository;

@ExtendWith(MockitoExtension.class)
class InventoryServicesTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private InventoryServices inventoryServices;

    @Test
    void getInventoryForUserDelegatesToRepository() {
        List<Inventory> expected = List.of(new Inventory());
        when(inventoryRepository.findByUserIdOrderByExpiryDateAscIngredientNameAsc(42L)).thenReturn(expected);

        List<Inventory> result = inventoryServices.getInventoryForUser(42L);

        assertSame(expected, result);
    }

    @Test
    void getRunOutSoonForUserDelegatesToRepository() {
        List<Inventory> expected = List.of(new Inventory());
        when(inventoryRepository.findByUserIdOrderByQuantityAsc(42L)).thenReturn(expected);

        List<Inventory> result = inventoryServices.getRunOutSoonForUser(42L);

        assertSame(expected, result);
    }

    @Test
    void addOrUpdateIngredientUpdatesExistingItem() {
        Inventory existing = new Inventory();
        when(inventoryRepository.findByUserIdAndIngredientNameIgnoreCase(7L, "Sugar")).thenReturn(Optional.of(existing));
        when(inventoryRepository.save(existing)).thenReturn(existing);

        Inventory saved = inventoryServices.addOrUpdateIngredient(7L, "  Sugar  ", 2.5, " g ", 1.0, LocalDate.now().plusDays(3));

        assertSame(existing, saved);
        assertEquals(7L, existing.getUserId());
        assertEquals("Sugar", existing.getIngredientName());
        assertEquals(2.5, existing.getQuantity());
        assertEquals("g", existing.getUnit());
        assertEquals(2.5, existing.getNormalizedQuantity());
        assertEquals("g", existing.getNormalizedUnit());
        assertNotNull(existing.getUpdatedAt());
    }

    @Test
    void addOrUpdateIngredientCreatesNewItemWhenMissing() {
        when(inventoryRepository.findByUserIdAndIngredientNameIgnoreCase(8L, "Flour")).thenReturn(Optional.empty());
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Inventory saved = inventoryServices.addOrUpdateIngredient(8L, " Flour ", 4.0, " kg ", 1.5, LocalDate.now().plusDays(10));

        assertEquals(8L, saved.getUserId());
        assertEquals("Flour", saved.getIngredientName());
        assertEquals("kg", saved.getUnit());
        assertEquals(4.0, saved.getNormalizedQuantity());
        assertEquals("kg", saved.getNormalizedUnit());
    }

    @Test
    void updateQuantityRejectsNullOrNegativeValues() {
        assertFalse(inventoryServices.updateQuantity(1L, 2L, null));
        assertFalse(inventoryServices.updateQuantity(1L, 2L, -0.1));

        verify(inventoryRepository, never()).findByIdAndUserId(any(), any());
    }

    @Test
    void updateQuantityUpdatesExistingItemAndReturnsTrue() {
        Inventory existing = new Inventory();
        existing.setQuantity(1.0);
        existing.setNormalizedQuantity(1.0);
        when(inventoryRepository.findByIdAndUserId(3L, 4L)).thenReturn(Optional.of(existing));

        boolean updated = inventoryServices.updateQuantity(4L, 3L, 9.0);

        assertTrue(updated);
        assertEquals(9.0, existing.getQuantity());
        assertEquals(9.0, existing.getNormalizedQuantity());
        assertNotNull(existing.getUpdatedAt());
        verify(inventoryRepository).save(existing);
    }

    @Test
    void updateQuantityReturnsFalseWhenItemMissing() {
        when(inventoryRepository.findByIdAndUserId(5L, 6L)).thenReturn(Optional.empty());

        boolean updated = inventoryServices.updateQuantity(6L, 5L, 2.0);

        assertFalse(updated);
    }

    @Test
    void deleteInventoryItemReturnsTrueOnSuccess() {
        boolean deleted = inventoryServices.deleteInventoryItem(11L);

        assertTrue(deleted);
        verify(inventoryRepository).deleteById(11L);
    }

    @Test
    void deleteInventoryItemReturnsFalseWhenAlreadyMissing() {
        org.mockito.Mockito.doThrow(new EmptyResultDataAccessException(1)).when(inventoryRepository).deleteById(12L);

        boolean deleted = inventoryServices.deleteInventoryItem(12L);

        assertFalse(deleted);
    }
}
