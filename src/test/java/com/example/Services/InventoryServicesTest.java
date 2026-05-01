package com.example.Services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
    void addOrUpdateIngredientTrimsAndCreatesNewInventoryRow() {
        long userId = 7L;
        when(inventoryRepository.findByUserIdAndIngredientNameIgnoreCase(userId, "Tomato"))
                .thenReturn(Optional.empty());
        when(inventoryRepository.save(any(Inventory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Inventory saved = inventoryServices.addOrUpdateIngredient(
                userId,
                " Tomato ",
                2.5,
                " g ",
                1.0,
                LocalDate.of(2026, 5, 10));

        assertThat(saved.getUserId()).isEqualTo(userId);
        assertThat(saved.getIngredientName()).isEqualTo("Tomato");
        assertThat(saved.getQuantity()).isEqualTo(2.5);
        assertThat(saved.getNormalizedQuantity()).isEqualTo(2.5);
        assertThat(saved.getUnit()).isEqualTo("g");
        assertThat(saved.getNormalizedUnit()).isEqualTo("g");
        assertThat(saved.getMinimumQuantity()).isEqualTo(1.0);
        assertThat(saved.getExpiryDate()).isEqualTo(LocalDate.of(2026, 5, 10));
        assertThat(saved.getUpdatedAt()).isNotNull();

        ArgumentCaptor<Inventory> inventoryCaptor = ArgumentCaptor.forClass(Inventory.class);
        verify(inventoryRepository).save(inventoryCaptor.capture());
        assertThat(inventoryCaptor.getValue().getUpdatedAt()).isNotNull();
    }

    @Test
    void updateQuantityReturnsFalseWhenItemDoesNotBelongToUser() {
        when(inventoryRepository.findByIdAndUserId(11L, 5L)).thenReturn(Optional.empty());

        boolean updated = inventoryServices.updateQuantity(5L, 11L, 3.0);

        assertThat(updated).isFalse();
        verify(inventoryRepository, never()).save(any(Inventory.class));
    }

    @Test
    void updateQuantityUpdatesNormalizedQuantityForMatchingOwner() {
        Inventory item = new Inventory();
        item.setUserId(5L);
        item.setIngredientName("Milk");
        item.setQuantity(1.0);
        item.setNormalizedQuantity(1.0);
        item.setUnit("ml");
        item.setNormalizedUnit("ml");
        item.setMinimumQuantity(0.5);
        item.setUpdatedAt(LocalDateTime.of(2026, 5, 1, 10, 0));

        when(inventoryRepository.findByIdAndUserId(11L, 5L)).thenReturn(Optional.of(item));
        when(inventoryRepository.save(any(Inventory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        boolean updated = inventoryServices.updateQuantity(5L, 11L, 3.5);

        assertThat(updated).isTrue();
        assertThat(item.getQuantity()).isEqualTo(3.5);
        assertThat(item.getNormalizedQuantity()).isEqualTo(3.5);
        assertThat(item.getUpdatedAt()).isAfter(LocalDateTime.of(2026, 5, 1, 10, 0));
        verify(inventoryRepository).save(item);
    }

    @Test
    void deleteInventoryItemReturnsFalseWhenRowIsAlreadyGone() {
        org.mockito.Mockito.doThrow(new EmptyResultDataAccessException(1))
                .when(inventoryRepository)
                .deleteById(99L);

        boolean deleted = inventoryServices.deleteInventoryItem(99L);

        assertThat(deleted).isFalse();
    }
}