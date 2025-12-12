package org.lessons.vehicles.java.quoted.service;

import java.math.BigDecimal;
import java.time.Year;
import java.util.List;
import org.lessons.vehicles.java.quoted.dto.PriceAdjustment; // Importa il nuovo record
import org.lessons.vehicles.java.vehicle.model.Vehicle;
import org.lessons.vehicles.java.vehicleVariation.dto.VehicleVariationDTO;
import org.springframework.stereotype.Service;

@Service
public class PriceCalculatorService {

    // Aggiungi il parametro 'adjustments' dove salveremo la storia del prezzo
    public BigDecimal calculateVehiclePrice(Vehicle vehicle, VehicleVariationDTO variation, List<PriceAdjustment> adjustments) {
        
        BigDecimal price = vehicle.getBasePrice() != null ? vehicle.getBasePrice() : BigDecimal.ZERO;

        // 1. CILINDRATA
        int cc = variation != null && variation.cc() != null ? variation.cc() : 0;
        int extraBlocks = Math.max(0, (cc - 1000) / 500);
        BigDecimal ccMultiplier = BigDecimal.valueOf(1 + 0.05 * extraBlocks);
        
        if (ccMultiplier.compareTo(BigDecimal.ONE) > 0) {
            BigDecimal increasedPrice = price.multiply(ccMultiplier);
            BigDecimal diff = increasedPrice.subtract(price);
            adjustments.add(new PriceAdjustment("Maggiorazione Cilindrata (" + cc + "cc)", diff));
            price = increasedPrice; // Aggiorna il prezzo corrente
        }

        // 2. ANNO IMMATRICOLAZIONE
        int currentYear = Year.now().getValue();
        int immYear = variation != null && variation.immatricolationYear() != null ? variation.immatricolationYear() : currentYear;
        BigDecimal yearMultiplier = BigDecimal.ONE;

        if (immYear < currentYear) { // Solo se diverso dall'anno corrente
            if (immYear >= currentYear - 2) {
                yearMultiplier = BigDecimal.valueOf(1.04);
            } else if (immYear >= currentYear - 4) {
                yearMultiplier = BigDecimal.valueOf(1.08);
            } else {
                yearMultiplier = BigDecimal.valueOf(1.12);
            }
        }

        if (yearMultiplier.compareTo(BigDecimal.ONE) > 0) {
            BigDecimal increasedPrice = price.multiply(yearMultiplier);
            BigDecimal diff = increasedPrice.subtract(price);
            adjustments.add(new PriceAdjustment("Fattore Anzianità (" + immYear + ")", diff));
            price = increasedPrice;
        }

        // 3. ALIMENTAZIONE
        String fuelEn = variation != null && variation.fuelSystemEn() != null ? variation.fuelSystemEn().toLowerCase() : "";
        String fuelIt = variation != null && variation.fuelSystemIt() != null ? variation.fuelSystemIt().toLowerCase() : "";
        BigDecimal fuelMultiplier = BigDecimal.ONE;
        String fuelLabel = "";

        if ("diesel".equals(fuelEn) || "diesel".equals(fuelIt)) {
            fuelMultiplier = BigDecimal.valueOf(1.03);
            fuelLabel = "Diesel";
        } else if ("electric".equals(fuelEn) || "elettrica".equals(fuelIt)) {
            fuelMultiplier = BigDecimal.valueOf(1.10);
            fuelLabel = "Elettrico";
        } else if ("hybrid".equals(fuelEn) || "ibrida".equals(fuelIt)) {
            fuelMultiplier = BigDecimal.valueOf(1.05);
            fuelLabel = "Ibrido";
        } else if ("gpl".equals(fuelEn) || "gpl".equals(fuelIt)) {
            fuelMultiplier = BigDecimal.valueOf(1.05);
            fuelLabel = "GPL";
        }

        if (fuelMultiplier.compareTo(BigDecimal.ONE) > 0) {
            BigDecimal increasedPrice = price.multiply(fuelMultiplier);
            BigDecimal diff = increasedPrice.subtract(price);
            adjustments.add(new PriceAdjustment("Supplemento Alimentazione (" + fuelLabel + ")", diff));
            price = increasedPrice;
        }

        return price;
    }
}