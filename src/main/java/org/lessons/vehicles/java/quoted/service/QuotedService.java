package org.lessons.vehicles.java.quoted.service;

import java.math.BigDecimal;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Service
public class QuotedService {

    private final QuotedRepository quotedRepository;
    private final PriceCalculatorService priceCalculatorService;

    public QuotedService(QuotedRepository quotedRepository,
            PriceCalculatorService priceCalculatorService) {
        this.quotedRepository = quotedRepository;
        this.priceCalculatorService = priceCalculatorService;
    }

    public List<QuotedDTO> getAllQuoted() {
        return quotedRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private VehicleDTOToQuoted toVehicleDTO(Vehicle vehicle) {
        List<VehicleVariationDTO> variationList = vehicle.getVehicleVariations() != null
                ? vehicle.getVehicleVariations().stream()
                        .map(this::toVariationDTO)
                        .collect(Collectors.toList())
                : List.of();

        return new VehicleDTOToQuoted(
                vehicle.getId(),
                vehicle.getBrand(),
                vehicle.getModel(),
                vehicle.getBasePrice(),
                variationList);
    }

    private VehicleVariationDTO toVariationDTO(VehicleVariation variation) {
        return new VehicleVariationDTO(
                variation.getCc(),
                variation.getImmatricolationMonth(),
                variation.getImmatricolationYear(),
                variation.getFuelSystemIt(),
                variation.getFuelSystemEn());
    }

    private OptionalDTOtoQuoted toOptionalDTO(Optionals optional) {
        return optional != null
                ? new OptionalDTOtoQuoted(optional.getId(), optional.getPrice())
                : null;
    }

    private BigDecimal calculateVehiclePrice(Vehicle vehicle, VehicleVariationDTO variation) {
        BigDecimal price = vehicle.getBasePrice() != null
                ? vehicle.getBasePrice()
                : BigDecimal.ZERO;

        int cc = variation != null && variation.cc() != null
                ? variation.cc()
                : 0;
        int extraBlocks = Math.max(0, (cc - 1000) / 500);
        BigDecimal ccMultiplier = BigDecimal.valueOf(1 + 0.05 * extraBlocks);
        price = price.multiply(ccMultiplier);

        int currentYear = Year.now().getValue();
        int immYear = variation != null && variation.immatricolationYear() != null
                ? variation.immatricolationYear()
                : currentYear;

        if (immYear == currentYear) {
        } else if (immYear >= currentYear - 2) {
            price = price.multiply(BigDecimal.valueOf(1.04));
        } else if (immYear >= currentYear - 4) {
            price = price.multiply(BigDecimal.valueOf(1.08));
        } else {
            price = price.multiply(BigDecimal.valueOf(1.12));
        }

        String fuel = variation != null && variation.fuelSystemEn() != null
                ? variation.fuelSystemEn()
                : "";
        switch (fuel.toLowerCase()) {
            case "diesel" -> price = price.multiply(BigDecimal.valueOf(1.03));
            case "electric" -> price = price.multiply(BigDecimal.valueOf(1.10));
            case "hybrid" -> price = price.multiply(BigDecimal.valueOf(1.05));
            case "gpl" -> price = price.multiply(BigDecimal.valueOf(1.05));
            default -> {
            }
        }

        return price;
    }

    private BigDecimal calculateFinalPrice(Quoted quoted) {
        BigDecimal total = BigDecimal.ZERO;

        Vehicle v = quoted.getVehicle();
        if (v != null) {
            if (v.getVehicleVariations() != null && !v.getVehicleVariations().isEmpty()) {
                for (VehicleVariation variation : v.getVehicleVariations()) {
                    VehicleVariationDTO varDTO = toVariationDTO(variation);
                    total = total.add(calculateVehiclePrice(v, varDTO));
                }
            } else {
                total = total.add(v.getBasePrice() != null
                        ? v.getBasePrice()
                        : BigDecimal.ZERO);
            }
        }

        if (quoted.getOptionals() != null) {
            for (Optionals o : quoted.getOptionals()) {
                if (o.getPrice() != null) {
                    total = total.add(o.getPrice());
                }
            }
        }

        int optionalCount = quoted.getOptionals() != null
                ? quoted.getOptionals().size()
                : 0;

        if (optionalCount >= 3) {
            total = total.multiply(BigDecimal.valueOf(0.97));
        }

        if (quoted.getVehicle() != null) {
            Vehicle singleVehicle = quoted.getVehicle();
            if (singleVehicle.getVehicleVariations() != null
                    && !singleVehicle.getVehicleVariations().isEmpty()) {
                VehicleVariation firstVariation = singleVehicle.getVehicleVariations().get(0);
                if (firstVariation.getImmatricolationYear() != null
                        && firstVariation.getImmatricolationYear() == Year.now().getValue()) {
                    total = total.multiply(BigDecimal.valueOf(0.98));
                }
            }
        }

        if (total.compareTo(BigDecimal.valueOf(20000)) > 0) {
            BigDecimal excess = total.subtract(BigDecimal.valueOf(20000));
            total = BigDecimal.valueOf(20000).add(excess.multiply(BigDecimal.valueOf(0.95)));
        }

        boolean hasClimatizzatore = quoted.getOptionals() != null
                && quoted.getOptionals().stream()
                        .anyMatch(o -> "climatizzatore".equalsIgnoreCase(o.getNameIt())
                                || "air conditioning".equalsIgnoreCase(o.getNameEn()));
        boolean hasNavigatore = quoted.getOptionals() != null
                && quoted.getOptionals().stream()
                        .anyMatch(o -> "navigatore".equalsIgnoreCase(o.getNameIt())
                                || "navigator".equalsIgnoreCase(o.getNameEn()));
        if (hasClimatizzatore && hasNavigatore) {
            total = total.subtract(BigDecimal.valueOf(100));
        }

        return total;
    }

    public QuotedDTO toDTO(Quoted quoted) {
        if (quoted == null) {
            return null;
        }

        List<VehicleDTOToQuoted> vehicles = quoted.getVehicle() != null
                ? List.of(this.toVehicleDTO(quoted.getVehicle()))
                : List.of();

        List<OptionalDTOtoQuoted> optionals = quoted.getOptionals() != null
                ? quoted.getOptionals().stream()
                        .map(this::toOptionalDTO)
                        .collect(Collectors.toList())
                : List.of();

        BigDecimal finalPrice = calculateFinalPrice(quoted);

        return new QuotedDTO(vehicles, optionals, finalPrice);
    }

    private Quoted toEntity(QuotedDTO quotedDTO) {
        Quoted quoted = new Quoted();

        if (quotedDTO.vehicleDTOToQuoted() != null && !quotedDTO.vehicleDTOToQuoted().isEmpty()) {
            VehicleDTOToQuoted vDTO = quotedDTO.vehicleDTOToQuoted().get(0);

            Vehicle vehicle = new Vehicle();
            vehicle.setId(vDTO.id());
            vehicle.setBrand(vDTO.brand());
            vehicle.setModel(vDTO.model());
            vehicle.setBasePrice(vDTO.basePrice());
            quoted.setVehicle(vehicle);
        }

        return quoted;
    }

    public QuotedDTO createQuoted(QuotedDTO quotedDTO) {
        Quoted newQuoted = toEntity(quotedDTO);

        newQuoted.setFinalPrice(calculateFinalPrice(newQuoted));

        Quoted savedQuoted = quotedRepository.save(newQuoted);

        return toDTO(savedQuoted);
    }

    public QuotedDTO getQuotedById(Integer id) {
        Quoted quoted = quotedRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Quotation not found with id: " + id));
        return toDTO(quoted);
    }

    public QuotedDTO updateQuoted(Integer id, QuotedDTO quotedDTO) {
        Quoted existingQuoted = quotedRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Quotation not found with id: " + id));

        if (existingQuoted.getOptionals() == null) {
            existingQuoted.setOptionals(new ArrayList<>());
        }

        if (quotedDTO.vehicleDTOToQuoted() != null && !quotedDTO.vehicleDTOToQuoted().isEmpty()) {
            VehicleDTOToQuoted vDTO = quotedDTO.vehicleDTOToQuoted().get(0);

            if (existingQuoted.getVehicle() != null && existingQuoted.getVehicle().getId().equals(vDTO.id())) {
                existingQuoted.getVehicle().setBrand(vDTO.brand());
                existingQuoted.getVehicle().setModel(vDTO.model());
                existingQuoted.getVehicle().setBasePrice(vDTO.basePrice());
            } else if (existingQuoted.getVehicle() == null) {
            }
        }

        if (quotedDTO.optionalDTOtoQuoted() != null) {
            for (OptionalDTOtoQuoted oDTO : quotedDTO.optionalDTOtoQuoted()) {
                existingQuoted.getOptionals().stream()
                        .filter(o -> o.getId().equals(oDTO.id()))
                        .findFirst()
                        .ifPresent(o -> o.setPrice(oDTO.price()));
            }
        }

        Quoted savedQuoted = quotedRepository.save(existingQuoted);

        return toDTO(savedQuoted);
    }
}