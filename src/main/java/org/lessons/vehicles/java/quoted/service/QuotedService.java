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
import org.lessons.vehicles.java.quoted.dto.PriceAdjustment;
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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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

    // Nota: Ho mantenuto il metodo che cerca per email, adattandolo al campo corretto
    public List<QuotedDTO> getQuotedByUserMail(String email) {
        // Verifica se nel repository il metodo si chiama findByUserEmail o findByUserMail
        // Se ti da errore qui, cambia in findByUserMail
        List<Quoted> quotedEntities = quotedRepository.findByUserEmail(email);

        if (quotedEntities.isEmpty()) {
            return List.of();
        }

        return quotedEntities.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    public QuotedDTO getQuotedById(Integer id) {
        Quoted quoted = quotedRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Quotation not found with id: " + id));
        return toDTO(quoted);
    }

    // --- METODI DI CALCOLO UNIFICATI ---

    private BigDecimal calculateFinalPrice(Quoted quoted, List<PriceAdjustment> adjustments) {
        BigDecimal total = BigDecimal.ZERO;

        Vehicle v = quoted.getVehicle();
        VehicleVariation selectedVariation = quoted.getVehicleVariation();

        // 1. Prezzo Veicolo Base + Variazioni
        if (v != null) {
            VehicleVariationDTO varDTO = selectedVariation != null ? toVariationDTO(selectedVariation) : null;
            total = priceCalculatorService.calculateVehiclePrice(v, varDTO, adjustments);
        }

        // 2. Aggiunta Optionals
        if (quoted.getOptionals() != null) {
            for (Optionals o : quoted.getOptionals()) {
                if (o.getPrice() != null) {
                    total = total.add(o.getPrice());
                }
            }
        }

        // 3. Sconto 3+ Optionals
        int optionalCount = quoted.getOptionals() != null ? quoted.getOptionals().size() : 0;
        if (optionalCount >= 3) {
            BigDecimal discountedTotal = total.multiply(BigDecimal.valueOf(0.97));
            BigDecimal discountAmount = discountedTotal.subtract(total);
            adjustments.add(new PriceAdjustment("Sconto Pacchetto Optionals (3+)", discountAmount));
            total = discountedTotal;
        }

        // 4. Sconto Immatricolazione Anno Corrente
        if (selectedVariation != null
                && selectedVariation.getImmatricolationYear() != null
                && selectedVariation.getImmatricolationYear() == Year.now().getValue()) {
            BigDecimal discountedTotal = total.multiply(BigDecimal.valueOf(0.98));
            BigDecimal discountAmount = discountedTotal.subtract(total);
            adjustments.add(new PriceAdjustment("Promo Immatricolazione Anno Corrente", discountAmount));
            total = discountedTotal;
        }

        // 5. Luxury Tax / Sconto > 20k
        if (total.compareTo(BigDecimal.valueOf(20000)) > 0) {
            BigDecimal excess = total.subtract(BigDecimal.valueOf(20000));
            BigDecimal discountAmount = excess.multiply(BigDecimal.valueOf(0.05)).negate();
            adjustments.add(new PriceAdjustment("Sconto su importo eccedente €20.000", discountAmount));
            total = total.add(discountAmount);
        }

        // 6. Pacchetto Comfort
        boolean hasClimatizzatore = quoted.getOptionals() != null && quoted.getOptionals().stream()
                .anyMatch(o -> "climatizzatore".equalsIgnoreCase(o.getNameIt()) || "air conditioning".equalsIgnoreCase(o.getNameEn()));
        boolean hasNavigatore = quoted.getOptionals() != null && quoted.getOptionals().stream()
                .anyMatch(o -> "navigatore".equalsIgnoreCase(o.getNameIt()) || "navigator".equalsIgnoreCase(o.getNameEn()));

        if (hasClimatizzatore && hasNavigatore) {
            BigDecimal discountAmount = BigDecimal.valueOf(-100);
            adjustments.add(new PriceAdjustment("Promo Pacchetto Comfort (Clima + Nav)", discountAmount));
            total = total.add(discountAmount);
        }

        // 7. Sconto Primo Preventivo (Logica del collega INTEGRATA col PDF)
        boolean isFirstQuotation = quoted.getUser() != null && Boolean.TRUE.equals(quoted.getUser().getIsFirstQuotation());

        if (isFirstQuotation) {
            BigDecimal discountedTotal = total.multiply(BigDecimal.valueOf(0.98)); // 2% di sconto
            BigDecimal discountAmount = discountedTotal.subtract(total);
            adjustments.add(new PriceAdjustment("Sconto Benvenuto (Primo Preventivo)", discountAmount));
            total = discountedTotal;
        }

        return total;
    }

    // --- MAPPING DTO ---

    public QuotedDTO toDTO(Quoted quoted) {
        if (quoted == null) return null;
        
        List<PriceAdjustment> adjustments = new ArrayList<>();
        BigDecimal finalPrice = calculateFinalPrice(quoted, adjustments);

        Integer id = quoted.getId();
        Integer userId = quoted.getUser() != null ? quoted.getUser().getId() : null;
        String userName = quoted.getUser() != null ? quoted.getUser().getName() : null;
        String userSurname = quoted.getUser() != null ? quoted.getUser().getSurname() : null;
        String userMail = quoted.getUser() != null ? quoted.getUser().getMail() : null;
        String userEmail = quoted.getUser() != null ? quoted.getUser().getEmail() : null;

        List<VehicleDTOToQuoted> vehicles = List.of();
        if (quoted.getVehicle() != null) {
            Vehicle vehicle = quoted.getVehicle();
            List<VehicleVariationDTO> variationList = List.of();
            if (quoted.getVehicleVariation() != null) {
                variationList = List.of(toVariationDTO(quoted.getVehicleVariation()));
            }
            vehicles = List.of(new VehicleDTOToQuoted(vehicle.getId(), vehicle.getBrand(), vehicle.getModel(), vehicle.getBasePrice(), variationList));
        }

        Integer vehicleVariationId = quoted.getVehicleVariation() != null ? quoted.getVehicleVariation().getId() : null;

        List<OptionalDTOtoQuoted> optionals = quoted.getOptionals() != null
                ? quoted.getOptionals().stream().map(this::toOptionalDTO).collect(Collectors.toList())
                : List.of();

        return new QuotedDTO(id, userId, userName, userSurname, userMail, userEmail, vehicles, vehicleVariationId,
                optionals, finalPrice, adjustments);
    }

    // --- CREAZIONE E UPDATE (UNIFICATI) ---

    public QuotedDTO createQuoted(QuotedDTO quotedDTO) {
        Quoted newQuoted = toEntity(quotedDTO);

        // Controllo se è il primo preventivo PRIMA di salvare
        boolean wasFirstQuotation = newQuoted.getUser() != null && Boolean.TRUE.equals(newQuoted.getUser().getIsFirstQuotation());

        List<PriceAdjustment> tempAdjustments = new ArrayList<>();
        newQuoted.setFinalPrice(calculateFinalPrice(newQuoted, tempAdjustments));

        Quoted savedQuoted = quotedRepository.save(newQuoted);

        // Logica del collega: Se era il primo, ora non lo è più
        if (wasFirstQuotation && savedQuoted.getUser() != null) {
            User user = savedQuoted.getUser();
            user.setIsFirstQuotation(false);
            userRepository.save(user);
        }

        return toDTO(savedQuoted);
    }

    public QuotedDTO updateQuoted(Integer id, QuotedDTO quotedDTO) {
        Quoted existingQuoted = quotedRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Quotation not found with id: " + id));

        // Aggiornamento Utente
        if (quotedDTO.userId() != null) {
            User user = userRepository.findById(quotedDTO.userId())
                    .orElseGet(() -> {
                        // Logica fallback creazione utente
                        User newUser = new User();
                        newUser.setName(quotedDTO.userName() != null ? quotedDTO.userName() : "default");
                        newUser.setSurname(quotedDTO.userSurname() != null ? quotedDTO.userSurname() : "default");
                        newUser.setMail(quotedDTO.userMail() != null ? quotedDTO.userMail() : "default@email.com");
                        newUser.setEmail(quotedDTO.userEmail() != null ? quotedDTO.userEmail() : "default@email.com");
                        newUser.setPassword("temporary");
                        newUser.setIsFirstQuotation(true);
                        return userRepository.save(newUser);
                    });
            existingQuoted.setUser(user);
        }

        // Aggiornamento Veicolo
        if (quotedDTO.vehicleDTOToQuoted() != null && !quotedDTO.vehicleDTOToQuoted().isEmpty()) {
            VehicleDTOToQuoted vDTO = quotedDTO.vehicleDTOToQuoted().get(0);
            Vehicle vehicle = vehicleRepository.findById(vDTO.id())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vehicle not found"));
            existingQuoted.setVehicle(vehicle);
        }

        // Aggiornamento Variazione
        if (quotedDTO.vehicleVariationId() != null) {
            VehicleVariation variation = vehicleVariationRepository.findById(quotedDTO.vehicleVariationId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Variation not found"));
            existingQuoted.setVehicleVariation(variation);
        }

        // Aggiornamento Optionals
        if (quotedDTO.optionalDTOtoQuoted() != null) {
            List<Optionals> optionals = new ArrayList<>();
            for (OptionalDTOtoQuoted oDTO : quotedDTO.optionalDTOtoQuoted()) {
                Optionals optional = optionalsRepository.findById(oDTO.id())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Optional not found"));
                optionals.add(optional);
            }
            existingQuoted.setOptionals(optionals);
        }

        // Ricalcolo
        List<PriceAdjustment> tempAdjustments = new ArrayList<>();
        existingQuoted.setFinalPrice(calculateFinalPrice(existingQuoted, tempAdjustments));

        Quoted savedQuoted = quotedRepository.save(existingQuoted);
        return toDTO(savedQuoted);
    }

    // --- HELPER DI MAPPING ---

    private VehicleVariationDTO toVariationDTO(VehicleVariation variation) {
        if (variation == null) return null;
        return new VehicleVariationDTO(
                variation.getId(),
                variation.getCc(),
                variation.getImmatricolationMonth(),
                variation.getImmatricolationYear(),
                variation.getFuelSystemIt(),
                variation.getFuelSystemEn());
    }

    private OptionalDTOtoQuoted toOptionalDTO(Optionals optional) {
        if (optional == null) return null;
        return new OptionalDTOtoQuoted(
                optional.getId(),
                optional.getVehicleTypeIt(), 
                optional.getVehicleTypeEn(), 
                optional.getNameIt(),        
                optional.getNameEn(),        
                optional.getPrice()
        );
    }
    
    // Ho mantenuto la versione "avanzata" del toEntity del tuo collega che gestisce meglio l'utente
    private Quoted toEntity(QuotedDTO quotedDTO) {
        Quoted quoted = new Quoted();

        if (quotedDTO.userMail() != null || quotedDTO.userName() != null) {
            User user;
            // Usa l'email per cercare l'utente se presente
            if (quotedDTO.userEmail() != null) {
                user = userRepository.findByEmail(quotedDTO.userEmail())
                        .orElseGet(() -> {
                            User newUser = new User();
                            newUser.setName(quotedDTO.userName() != null ? quotedDTO.userName() : "default");
                            newUser.setSurname(quotedDTO.userSurname() != null ? quotedDTO.userSurname() : "default");
                            newUser.setMail(quotedDTO.userMail() != null ? quotedDTO.userMail() : quotedDTO.userEmail());
                            newUser.setEmail(quotedDTO.userEmail());
                            newUser.setPassword("temporary");
                            newUser.setIsFirstQuotation(true);
                            return userRepository.save(newUser);
                        });
            } else {
                // Fallback vecchio stile
                user = new User();
                user.setName(quotedDTO.userName() != null ? quotedDTO.userName() : "default");
                user.setSurname(quotedDTO.userSurname() != null ? quotedDTO.userSurname() : "default");
                user.setMail("noemail-" + UUID.randomUUID());
                user.setEmail("noemail-" + UUID.randomUUID());
                user.setPassword("temporary");
                user.setIsFirstQuotation(true);
                user = userRepository.save(user);
            }
            quoted.setUser(user);
        }

        if (quotedDTO.vehicleDTOToQuoted() != null && !quotedDTO.vehicleDTOToQuoted().isEmpty()) {
            VehicleDTOToQuoted vDTO = quotedDTO.vehicleDTOToQuoted().get(0);
            Vehicle vehicle = vehicleRepository.findById(vDTO.id())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vehicle not found"));
            quoted.setVehicle(vehicle);
        }

        if (quotedDTO.vehicleVariationId() != null) {
            VehicleVariation variation = vehicleVariationRepository.findById(quotedDTO.vehicleVariationId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Variation not found"));
            quoted.setVehicleVariation(variation);
        }

        if (quotedDTO.optionalDTOtoQuoted() != null && !quotedDTO.optionalDTOtoQuoted().isEmpty()) {
            List<Optionals> optionals = new ArrayList<>();
            for (OptionalDTOtoQuoted oDTO : quotedDTO.optionalDTOtoQuoted()) {
                Optionals optional = optionalsRepository.findById(oDTO.id())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Optional not found"));
                optionals.add(optional);
            }
            quoted.setOptionals(optionals);
        }

        return quoted;
    }
}