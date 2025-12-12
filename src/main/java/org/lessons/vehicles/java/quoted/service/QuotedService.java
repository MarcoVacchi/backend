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
import org.lessons.vehicles.java.quoted.dto.PriceAdjustment; // Importante
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

    // --- METODI DI RECUPERO DATI ---

    public List<QuotedDTO> getAllQuoted() {
        return quotedRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<QuotedDTO> getQuotedByUserMail(String email) {
        List<Quoted> quotedEntities = quotedRepository.findByUserMail(email);
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

    // --- LOGICA DI CALCOLO E TRASFORMAZIONE ---

    /**
     * Calcola il prezzo finale e popola la lista degli aggiustamenti per il PDF.
     */
    private BigDecimal calculateFinalPrice(Quoted quoted, List<PriceAdjustment> adjustments) {
        BigDecimal total = BigDecimal.ZERO;

        Vehicle v = quoted.getVehicle();
        VehicleVariation selectedVariation = quoted.getVehicleVariation();

        // 1. Calcolo Prezzo Veicolo (Delega al PriceCalculatorService)
        // Passiamo la lista 'adjustments' così il service esterno può aggiungere le note su Anno, Cilindrata, etc.
        if (v != null) {
            VehicleVariationDTO varDTO = selectedVariation != null ? toVariationDTO(selectedVariation) : null;
            // Assicurati che PriceCalculatorService accetti la lista come terzo parametro!
            total = priceCalculatorService.calculateVehiclePrice(v, varDTO, adjustments);
        }

        // 2. Aggiunta Optionals (Semplice somma, nessuna nota di adjustment necessaria qui)
        if (quoted.getOptionals() != null) {
            for (Optionals o : quoted.getOptionals()) {
                if (o.getPrice() != null) {
                    total = total.add(o.getPrice());
                }
            }
        }

        // --- APPLICAZIONE SCONTI E REGOLE ---

        // Regola: Sconto se ci sono 3 o più optional
        int optionalCount = quoted.getOptionals() != null ? quoted.getOptionals().size() : 0;
        if (optionalCount >= 3) {
            BigDecimal discountedTotal = total.multiply(BigDecimal.valueOf(0.97));
            BigDecimal discountAmount = discountedTotal.subtract(total); // Sarà negativo
            adjustments.add(new PriceAdjustment("Sconto Pacchetto Optionals (3+)", discountAmount));
            total = discountedTotal;
        }

        // Regola: Sconto Immatricolazione anno corrente
        if (selectedVariation != null
                && selectedVariation.getImmatricolationYear() != null
                && selectedVariation.getImmatricolationYear() == Year.now().getValue()) {
            BigDecimal discountedTotal = total.multiply(BigDecimal.valueOf(0.98));
            BigDecimal discountAmount = discountedTotal.subtract(total);
            adjustments.add(new PriceAdjustment("Promo Immatricolazione Anno Corrente", discountAmount));
            total = discountedTotal;
        }

        // Regola: Luxury Tax / Sconto sopra i 20k (Logica custom)
        if (total.compareTo(BigDecimal.valueOf(20000)) > 0) {
            BigDecimal excess = total.subtract(BigDecimal.valueOf(20000));
            // Qui la logica originale era: 20000 + (eccesso * 0.95).
            // Significa che sull'eccedenza applichiamo uno sconto del 5%.
            BigDecimal discountAmount = excess.multiply(BigDecimal.valueOf(0.05)).negate(); // Negativo per indicare sconto
            
            adjustments.add(new PriceAdjustment("Sconto su importo eccedente €20.000", discountAmount));
            total = total.add(discountAmount); 
        }

        // Regola: Sconto Pacchetto Comfort (Clima + Navigatore)
        boolean hasClimatizzatore = quoted.getOptionals() != null
                && quoted.getOptionals().stream()
                .anyMatch(o -> "climatizzatore".equalsIgnoreCase(o.getNameIt())
                        || "air conditioning".equalsIgnoreCase(o.getNameEn()));
        boolean hasNavigatore = quoted.getOptionals() != null
                && quoted.getOptionals().stream()
                .anyMatch(o -> "navigatore".equalsIgnoreCase(o.getNameIt())
                        || "navigator".equalsIgnoreCase(o.getNameEn()));
        
        if (hasClimatizzatore && hasNavigatore) {
            BigDecimal discountAmount = BigDecimal.valueOf(-100);
            adjustments.add(new PriceAdjustment("Promo Pacchetto Comfort (Clima + Nav)", discountAmount));
            total = total.add(discountAmount);
        }

        return total;
    }

    public QuotedDTO toDTO(Quoted quoted) {
        if (quoted == null) return null;

        // 1. Inizializza la lista per raccogliere i dettagli del calcolo
        List<PriceAdjustment> adjustments = new ArrayList<>();

        // 2. Calcola il prezzo passando la lista (che verrà riempita)
        BigDecimal finalPrice = calculateFinalPrice(quoted, adjustments);

        // 3. Costruzione DTO standard
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
            vehicles = List.of(new VehicleDTOToQuoted(
                    vehicle.getId(), vehicle.getBrand(), vehicle.getModel(), vehicle.getBasePrice(), variationList));
        }

        List<OptionalDTOtoQuoted> optionals = quoted.getOptionals() != null
                ? quoted.getOptionals().stream().map(this::toOptionalDTO).collect(Collectors.toList())
                : List.of();

        Integer vehicleVariationId = quoted.getVehicleVariation() != null ? quoted.getVehicleVariation().getId() : null;

        // 4. Ritorna il DTO INCLUDENDO la lista 'adjustments'
        return new QuotedDTO(
                quoted.getId(),
                userId,
                userName,
                userSurname,
                userMail,
                vehicles,
                vehicleVariationId,
                optionals,
                finalPrice,
                adjustments // <--- QUI PASSIAMO LA LISTA AL PDF
        );
    }

    // --- METODI DI CREAZIONE E AGGIORNAMENTO ---

    public QuotedDTO createQuoted(QuotedDTO quotedDTO) {
        Quoted newQuoted = toEntity(quotedDTO);
        
        // Calcoliamo il prezzo per salvarlo nel DB (usiamo una lista temporanea che qui non serve al DB)
        List<PriceAdjustment> tempAdjustments = new ArrayList<>(); 
        newQuoted.setFinalPrice(calculateFinalPrice(newQuoted, tempAdjustments));

        Quoted savedQuoted = quotedRepository.save(newQuoted);

        // Quando convertiamo a DTO per il ritorno, la lista verrà ricreata correttamente da toDTO
        return toDTO(savedQuoted);
    }

    public QuotedDTO updateQuoted(Integer id, QuotedDTO quotedDTO) {
        Quoted existingQuoted = quotedRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Quotation not found with id: " + id));

        // Aggiornamento Utente
        if (quotedDTO.userId() != null) {
            User user = userRepository.findById(quotedDTO.userId()).orElseGet(() -> {
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

        // Ricalcolo Finale e Salvataggio
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

    // Cerca questo metodo e sostituiscilo con questa versione:
    private OptionalDTOtoQuoted toOptionalDTO(Optionals optional) {
        if (optional == null) return null;
        
        // Prendiamo il nome italiano, se manca usiamo quello inglese
        String typeName = optional.getNameIt() != null ? optional.getNameIt() : optional.getNameEn();
        
        // Ora passiamo 3 argomenti: ID, NOME (type), PREZZO
        return new OptionalDTOtoQuoted(optional.getId(), typeName, optional.getPrice());
    }

    private Quoted toEntity(QuotedDTO quotedDTO) {
        Quoted quoted = new Quoted();

        // Mapping User
        if (quotedDTO.userMail() != null || quotedDTO.userName() != null) {
            User user = new User();
            user.setName(quotedDTO.userName() != null ? quotedDTO.userName() : "default");
            user.setSurname(quotedDTO.userSurname() != null ? quotedDTO.userSurname() : "default");
            user.setMail(quotedDTO.userMail() != null ? quotedDTO.userMail() : "noemail-" + UUID.randomUUID());
            user.setPassword("temporary");
            user.setIsFirstQuotation(true);
            user = userRepository.save(user);
            quoted.setUser(user);
        }

        // Mapping Veicolo
        if (quotedDTO.vehicleDTOToQuoted() != null && !quotedDTO.vehicleDTOToQuoted().isEmpty()) {
            VehicleDTOToQuoted vDTO = quotedDTO.vehicleDTOToQuoted().get(0);
            Vehicle vehicle = vehicleRepository.findById(vDTO.id())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vehicle not found"));
            quoted.setVehicle(vehicle);
        }

        // Mapping Variazione
        if (quotedDTO.vehicleVariationId() != null) {
            VehicleVariation variation = vehicleVariationRepository.findById(quotedDTO.vehicleVariationId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Variation not found"));
            quoted.setVehicleVariation(variation);
        }

        // Mapping Optionals
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


