package org.lessons.vehicles.java.quoted.service;

import java.util.List;

import org.lessons.vehicles.java.optionals.dto.OptionalDTOtoQuoted;
import org.lessons.vehicles.java.optionals.model.Optionals;
import org.lessons.vehicles.java.quoted.dto.QuotedDTO;
import org.lessons.vehicles.java.quoted.model.Quoted;
import org.lessons.vehicles.java.quoted.repository.QuotedRepository;
import org.lessons.vehicles.java.vehicle.dto.VehicleDTOToQuoted;
import org.lessons.vehicles.java.vehicle.model.Vehicle;
import org.lessons.vehicles.java.vehicleVariation.dto.VehicleVariationDTO;
import org.lessons.vehicles.java.vehicleVariation.model.VehicleVariation;
import org.springframework.stereotype.Service;

@Service
public class QuotedService {

    private final QuotedRepository quotedRepository;

    public QuotedService(QuotedRepository quotedRepository) {
        this.quotedRepository = quotedRepository;
    }

    public List<QuotedDTO> getAllQuoted() {
        return quotedRepository.findAll().stream()
                .map(this::toDTO)
                .toList();
    }

    private VehicleVariationDTO toVehicleVariationDTO(VehicleVariation variation) {
        if (variation == null) {
            return null;
        }
        return new VehicleVariationDTO(
                variation.getCc(),
                variation.getImmatricolationMonth(),
                variation.getImmatricolationYear(),
                variation.getFuelSystemIt(),
                variation.getFuelSystemEn());
    }

    private VehicleDTOToQuoted toVariationDTO(Vehicle vehicle) {
        if (vehicle == null) {
            return null;
        }

        List<VehicleVariationDTO> variationList = vehicle.getVehicleVariations() != null
                ? vehicle.getVehicleVariations().stream()
                        .map(this::toVehicleVariationDTO)
                        .toList()
                : List.of();

        return new VehicleDTOToQuoted(
                vehicle.getId(),
                vehicle.getBrand(),
                vehicle.getModel(),
                vehicle.getBasePrice(),
                variationList);
    }

    private OptionalDTOtoQuoted toOptionalDTO(Optionals optional) {
        if (optional == null) {
            return null;
        }
        return new OptionalDTOtoQuoted(
                optional.getId(),
                optional.getPrice());
    }

    public QuotedDTO toDTO(Quoted quoted) {
        if (quoted == null) {
            return null;
        }

        List<VehicleDTOToQuoted> vehicleList = quoted.getVehicles() != null
                ? quoted.getVehicles().stream()
                        .map(this::toVariationDTO)
                        .toList()
                : List.of();

        List<OptionalDTOtoQuoted> optionalList = quoted.getOptionals() != null
                ? quoted.getOptionals().stream()
                        .map(this::toOptionalDTO)
                        .toList()
                : List.of();

        return new QuotedDTO(vehicleList, optionalList);
    }
}