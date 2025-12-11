package org.lessons.vehicles.java.quoted.dto;

import java.math.BigDecimal;
import java.util.List;

import org.lessons.vehicles.java.optionals.dto.OptionalDTOtoQuoted;
import org.lessons.vehicles.java.vehicle.dto.VehicleDTOToQuoted;

public record QuotedDTO(
                List<VehicleDTOToQuoted> vehicleDTOToQuoted,
                Integer vehicleVariationId,
                List<OptionalDTOtoQuoted> optionalDTOtoQuoted,
                BigDecimal finalPrice) {

}