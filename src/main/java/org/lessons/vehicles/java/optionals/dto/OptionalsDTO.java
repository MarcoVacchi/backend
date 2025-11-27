package org.lessons.vehicles.java.optionals.dto;

import java.math.BigDecimal;

public record OptionalsDTO(
        Integer id,
        String nameIt,
        String nameEn,
        BigDecimal price,
        String vehicleTypeIt,
        String vehicleTypeEn) {

}
