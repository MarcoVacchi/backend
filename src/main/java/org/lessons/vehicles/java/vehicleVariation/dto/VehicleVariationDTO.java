package org.lessons.vehicles.java.vehicleVariation.dto;

import java.sql.Date;

public record VehicleVariationDTO(
                Integer id,
                Integer cc,
                Date immatricolationMonth,
                Integer immatricolationYear,
                String fuelSystemIt,
                String fuelSystemEn) {
}
