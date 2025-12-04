package org.lessons.vehicles.java.vehicle.dto;

import java.math.BigDecimal;
import java.util.List;

import org.lessons.vehicles.java.vehicleVariation.dto.VehicleVariationDTO;

public record VehicleDTOToQuoted(
                Integer id,
                String brand,
                String model,
                BigDecimal basePice,
                List<VehicleVariationDTO> vehicleVariations) {
}
