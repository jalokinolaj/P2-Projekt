package com.example.Services;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class UnitConverterServiceTest {

    private final UnitConverterService service = new UnitConverterService();

    @Test
    void normalizeConvertsMassUnitsToGrams() {
        UnitConverterService.ConversionResult result = service.normalize(2.0, "kg");

        assertEquals(2000.0, result.normalizedQuantity());
        assertEquals("g", result.normalizedUnit());
    }

    @Test
    void normalizeConvertsVolumeUnitsToMilliliters() {
        UnitConverterService.ConversionResult result = service.normalize(2.0, "cup");

        assertEquals(480.0, result.normalizedQuantity());
        assertEquals("ml", result.normalizedUnit());
    }

    @Test
    void normalizeConvertsCountUnitsToPieces() {
        UnitConverterService.ConversionResult result = service.normalize(3.0, "units");

        assertEquals(3.0, result.normalizedQuantity());
        assertEquals("pcs", result.normalizedUnit());
    }

    @Test
    void normalizeHandlesNullQuantityAsZero() {
        UnitConverterService.ConversionResult result = service.normalize(null, "g");

        assertEquals(0.0, result.normalizedQuantity());
        assertEquals("g", result.normalizedUnit());
    }

    @Test
    void normalizeUnknownUnitFallsBackToIdentity() {
        UnitConverterService.ConversionResult result = service.normalize(12.5, " Pinch ");

        assertEquals(12.5, result.normalizedQuantity());
        assertEquals("pinch", result.normalizedUnit());
    }

    @Test
    void normalizeUnitLabelTrimsLowercasesAndCollapsesWhitespace() {
        assertEquals("fl oz", service.normalizeUnitLabel("  FL   OZ "));
    }

    @Test
    void normalizeUnitLabelReturnsEmptyStringForNull() {
        assertEquals("", service.normalizeUnitLabel(null));
    }
}
