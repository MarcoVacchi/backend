package org.lessons.vehicles.java.quoted.dto;

import java.math.BigDecimal;
import java.util.List;

import org.lessons.vehicles.java.optionals.dto.OptionalDTOtoQuoted;
import org.lessons.vehicles.java.vehicle.dto.VehicleDTOToQuoted;

public record QuotedDTO(
        Integer id,
        Integer userId,
        String userName,
        String userSurname,
        String userMail,
        List<VehicleDTOToQuoted> vehicleDTOToQuoted,
        Integer vehicleVariationId,
        List<OptionalDTOtoQuoted> optionalDTOtoQuoted,
        BigDecimal finalPrice,
        // LA NUOVA LISTA VA QUI, DENTRO LE TONDE:
        List<PriceAdjustment> priceAdjustments 
) {
    // Le parentesi graffe rimangono vuote se non hai costruttori personalizzati o metodi extra
}