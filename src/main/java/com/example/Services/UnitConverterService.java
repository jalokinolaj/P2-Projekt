package com.example.Services;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.springframework.stereotype.Service;

@Service
public class UnitConverterService {

	private static final String BASE_MASS = "g";
	private static final String BASE_VOLUME = "ml";
	private static final String BASE_COUNT = "pcs";

	private static final Map<String, Double> MASS_FACTORS = new HashMap<>();
	private static final Map<String, Double> VOLUME_FACTORS = new HashMap<>();
	private static final Map<String, Double> COUNT_FACTORS = new HashMap<>();

	static {
		MASS_FACTORS.put("g", 1.0);
		MASS_FACTORS.put("gram", 1.0);
		MASS_FACTORS.put("grams", 1.0);
		MASS_FACTORS.put("kg", 1000.0);
		MASS_FACTORS.put("oz", 28.349523125);
		MASS_FACTORS.put("lb", 453.59237);

		VOLUME_FACTORS.put("ml", 1.0);
		VOLUME_FACTORS.put("milliliter", 1.0);
		VOLUME_FACTORS.put("milliliters", 1.0);
		VOLUME_FACTORS.put("l", 1000.0);
		VOLUME_FACTORS.put("liter", 1000.0);
		VOLUME_FACTORS.put("liters", 1000.0);
		VOLUME_FACTORS.put("fl oz", 29.5735295625);
		VOLUME_FACTORS.put("cup", 240.0);
		VOLUME_FACTORS.put("cups", 240.0);
		VOLUME_FACTORS.put("tbsp", 15.0);
		VOLUME_FACTORS.put("tsp", 5.0);
		VOLUME_FACTORS.put("pt", 473.176473);
		VOLUME_FACTORS.put("qt", 946.352946);
		VOLUME_FACTORS.put("gal", 3785.411784);

		COUNT_FACTORS.put("pcs", 1.0);
		COUNT_FACTORS.put("pc", 1.0);
		COUNT_FACTORS.put("piece", 1.0);
		COUNT_FACTORS.put("pieces", 1.0);
		COUNT_FACTORS.put("unit", 1.0);
		COUNT_FACTORS.put("units", 1.0);
	}

	public ConversionResult normalize(Double quantity, String unit) {
		double safeQuantity = quantity == null ? 0.0 : quantity;
		String safeUnit = normalizeUnitLabel(unit);

		if (MASS_FACTORS.containsKey(safeUnit)) {
			return new ConversionResult(safeQuantity * MASS_FACTORS.get(safeUnit), BASE_MASS);
		}
		if (VOLUME_FACTORS.containsKey(safeUnit)) {
			return new ConversionResult(safeQuantity * VOLUME_FACTORS.get(safeUnit), BASE_VOLUME);
		}
		if (COUNT_FACTORS.containsKey(safeUnit)) {
			return new ConversionResult(safeQuantity * COUNT_FACTORS.get(safeUnit), BASE_COUNT);
		}

		// Unknown units fall back to identity so we preserve user input without blocking save.
		return new ConversionResult(safeQuantity, safeUnit);
	}

	public String normalizeUnitLabel(String unit) {
		if (unit == null) {
			return "";
		}
		return unit.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
	}

	public record ConversionResult(double normalizedQuantity, String normalizedUnit) {
	}
}
