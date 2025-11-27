package org.lessons.vehicles.java.optionals.service;

import java.util.List;

import org.lessons.vehicles.java.optionals.dto.OptionalsDTO;
import org.lessons.vehicles.java.optionals.model.Optionals;
import org.lessons.vehicles.java.optionals.repository.OptionalsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OptionalsService {

    @Autowired
    private OptionalsRepository optionalsRepository;

    public OptionalsService(OptionalsRepository optionalsRepository) {
        this.optionalsRepository = optionalsRepository;
    }

    // read
    public List<OptionalsDTO> getAllOptional() {
        return optionalsRepository.findAll().stream()
                .map(this::toDTO)
                .toList();
    }

    // private Optionals toEntity(OptionalsDTO dto) {
    // Optionals optionals = new Optionals();
    // optionals.setId(dto.id());
    // optionals.setNameEn(dto.nameEn());
    // optionals.setNameIt(dto.nameIt());
    // optionals.setPrice(dto.price());
    // optionals.setVehicleTypeEn(dto.vehicleTypeEn());
    // optionals.setVehicleTypeIt(dto.vehicleTypeIt());
    // return optionals;
    // }

    public OptionalsDTO toDTO(Optionals optionals) {
        if (optionals == null) {
            return null;
        }
        return new OptionalsDTO(
                optionals.getId(),
                optionals.getNameEn(),
                optionals.getNameIt(),
                optionals.getPrice(),
                optionals.getVehicleTypeEn(),
                optionals.getVehicleTypeIt());

    }

}
