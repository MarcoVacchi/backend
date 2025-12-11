package org.lessons.vehicles.java.quoted.service;

import java.math.BigDecimal;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.lessons.vehicles.java.optionals.dto.OptionalDTOtoQuoted;
import org.lessons.vehicles.java.optionals.model.Optionals;
import org.lessons.vehicles.java.optionals.repository.OptionalsRepository;
import org.lessons.vehicles.java.quoted.dto.QuotedDTO;
import org.lessons.vehicles.java.quoted.model.Quoted;
import org.lessons.vehicles.java.quoted.repository.QuotedRepository;
import org.lessons.vehicles.java.user.model.User;
import org.lessons.vehicles.java.user.repository.UserRepository;
import org.lessons.vehicles.java.vehicle.dto.VehicleDTOToQuoted;
import org.lessons.vehicles.java.vehicle.model.Vehicle;
import org.lessons.vehicles.java.vehicle.repository.VehicleRepository;
import org.lessons.vehicles.java.vehicleVariation.dto.VehicleVariationDTO;
import org.lessons.vehicles.java.vehicleVariation.model.VehicleVariation;
import org.lessons.vehicles.java.vehicleVariation.repository.VehicleVariationRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Service
public class QuotedService {

    private final QuotedRepository quotedRepository;
    private final PriceCalculatorService priceCalculatorService;
    private final VehicleRepository vehicleRepository;
    private final OptionalsRepository optionalsRepository;
    private final VehicleVariationRepository vehicleVariationRepository;
    private final UserRepository userRepository;

    public QuotedService(QuotedRepository quotedRepository,
            PriceCalculatorService priceCalculatorService,
            VehicleRepository vehicleRepository,
            OptionalsRepository optionalsRepository,
            VehicleVariationRepository vehicleVariationRepository,
            UserRepository userRepository) {
        this.quotedRepository = quotedRepository;
        this.priceCalculatorService = priceCalculatorService;
        this.vehicleRepository = vehicleRepository;
        this.optionalsRepository = optionalsRepository;
        this.vehicleVariationRepository = vehicleVariationRepository;
        this.userRepository = userRepository;
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
                variation.getId(),
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
            case "gpl", "lpg" -> price = price.multiply(BigDecimal.valueOf(1.05));
            default -> {
            }
        }

        return price;
    }

    private BigDecimal calculateFinalPrice(Quoted quoted) {
        BigDecimal total = BigDecimal.ZERO;

        Vehicle v = quoted.getVehicle();
        VehicleVariation selectedVariation = quoted.getVehicleVariation();

        if (v != null && selectedVariation != null) {
            VehicleVariationDTO varDTO = toVariationDTO(selectedVariation);
            total = total.add(calculateVehiclePrice(v, varDTO));
        } else if (v != null) {
            total = total.add(v.getBasePrice() != null ? v.getBasePrice() : BigDecimal.ZERO);
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

        if (selectedVariation != null
                && selectedVariation.getImmatricolationYear() != null
                && selectedVariation.getImmatricolationYear() == Year.now().getValue()) {
            total = total.multiply(BigDecimal.valueOf(0.98));
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

        Integer userId = quoted.getUser() != null ? quoted.getUser().getId() : null;
        String userName = quoted.getUser() != null ? quoted.getUser().getName() : null;
        String userSurname = quoted.getUser() != null ? quoted.getUser().getSurname() : null;
        String userMail = quoted.getUser() != null ? quoted.getUser().getMail() : null;

        List<VehicleDTOToQuoted> vehicles = List.of();

        if (quoted.getVehicle() != null) {
            Vehicle vehicle = quoted.getVehicle();

            List<VehicleVariationDTO> variationList = List.of();
            if (quoted.getVehicleVariation() != null) {
                variationList = List.of(toVariationDTO(quoted.getVehicleVariation()));
            }

            VehicleDTOToQuoted vehicleDTO = new VehicleDTOToQuoted(
                    vehicle.getId(),
                    vehicle.getBrand(),
                    vehicle.getModel(),
                    vehicle.getBasePrice(),
                    variationList);

            vehicles = List.of(vehicleDTO);
        }

        Integer vehicleVariationId = quoted.getVehicleVariation() != null
                ? quoted.getVehicleVariation().getId()
                : null;

        List<OptionalDTOtoQuoted> optionals = quoted.getOptionals() != null
                ? quoted.getOptionals().stream()
                        .map(this::toOptionalDTO)
                        .collect(Collectors.toList())
                : List.of();

        BigDecimal finalPrice = calculateFinalPrice(quoted);

        return new QuotedDTO(userId, userName, userSurname, userMail, vehicles, vehicleVariationId, optionals,
                finalPrice);
    }

    private Quoted toEntity(QuotedDTO quotedDTO) {
        Quoted quoted = new Quoted();

        if (quotedDTO.userMail() != null || quotedDTO.userName() != null) {
            User user = new User();
            user.setName(quotedDTO.userName() != null ? quotedDTO.userName() : "default");
            user.setSurname(quotedDTO.userSurname() != null ? quotedDTO.userSurname() : "default");
            user.setMail(quotedDTO.userMail() != null ? quotedDTO.userMail() : "noemail-" + UUID.randomUUID());
            user.setPassword("temporary");
            user.setIsFirstQuotation(true);

            user = userRepository.save(user); // salva
            quoted.setUser(user); // associa
        }

        if (quotedDTO.vehicleDTOToQuoted() != null && !quotedDTO.vehicleDTOToQuoted().isEmpty()) {
            VehicleDTOToQuoted vDTO = quotedDTO.vehicleDTOToQuoted().get(0);

            Vehicle vehicle = vehicleRepository.findById(vDTO.id())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Vehicle not found with id: " + vDTO.id()));

            quoted.setVehicle(vehicle);
        }

        if (quotedDTO.vehicleVariationId() != null) {
            VehicleVariation variation = vehicleVariationRepository.findById(quotedDTO.vehicleVariationId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Vehicle variation not found with id: " + quotedDTO.vehicleVariationId()));

            quoted.setVehicleVariation(variation);
        }

        if (quotedDTO.optionalDTOtoQuoted() != null && !quotedDTO.optionalDTOtoQuoted().isEmpty()) {
            List<Optionals> optionals = new ArrayList<>();

            for (OptionalDTOtoQuoted oDTO : quotedDTO.optionalDTOtoQuoted()) {
                Optionals optional = optionalsRepository.findById(oDTO.id())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                "Optional not found with id: " + oDTO.id()));

                optionals.add(optional);
            }

            quoted.setOptionals(optionals);
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

        if (quotedDTO.userId() != null) {
            User user = userRepository.findById(quotedDTO.userId())
                    .orElseGet(() -> {
                        User newUser = new User();
                        newUser.setName(quotedDTO.userName() != null ? quotedDTO.userName() : "default");
                        newUser.setSurname(quotedDTO.userSurname() != null ? quotedDTO.userSurname() : "default");
                        newUser.setMail(quotedDTO.userMail() != null ? quotedDTO.userMail() : "default@email.com");
                        newUser.setPassword("temporary");
                        newUser.setIsFirstQuotation(true);
                        return userRepository.save(newUser);
                    });
            existingQuoted.setUser(user);
        }

        if (existingQuoted.getOptionals() == null) {
            existingQuoted.setOptionals(new ArrayList<>());
        }

        if (quotedDTO.vehicleDTOToQuoted() != null && !quotedDTO.vehicleDTOToQuoted().isEmpty()) {
            VehicleDTOToQuoted vDTO = quotedDTO.vehicleDTOToQuoted().get(0);

            Vehicle vehicle = vehicleRepository.findById(vDTO.id())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Vehicle not found with id: " + vDTO.id()));

            existingQuoted.setVehicle(vehicle);
        }

        if (quotedDTO.vehicleVariationId() != null) {
            VehicleVariation variation = vehicleVariationRepository.findById(quotedDTO.vehicleVariationId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Vehicle variation not found with id: " + quotedDTO.vehicleVariationId()));

            existingQuoted.setVehicleVariation(variation);
        }

        if (quotedDTO.optionalDTOtoQuoted() != null && !quotedDTO.optionalDTOtoQuoted().isEmpty()) {
            List<Optionals> optionals = new ArrayList<>();

            for (OptionalDTOtoQuoted oDTO : quotedDTO.optionalDTOtoQuoted()) {
                Optionals optional = optionalsRepository.findById(oDTO.id())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                "Optional not found with id: " + oDTO.id()));

                optionals.add(optional);
            }

            existingQuoted.setOptionals(optionals);
        }

        existingQuoted.setFinalPrice(calculateFinalPrice(existingQuoted));

        Quoted savedQuoted = quotedRepository.save(existingQuoted);

        return toDTO(savedQuoted);
    }
}