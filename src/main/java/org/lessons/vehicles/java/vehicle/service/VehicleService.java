package org.lessons.vehicles.java.vehicle.service;

import java.util.List;

import org.lessons.vehicles.java.vehicle.dto.VehicleDTO;
import org.lessons.vehicles.java.vehicle.model.Vehicle;
import org.lessons.vehicles.java.vehicle.repository.VehicleRepository;
import org.lessons.vehicles.java.vehicleVariation.dto.VehicleVariationDTO;
import org.lessons.vehicles.java.vehicleVariation.model.VehicleVariation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class VehicleService {

    @Autowired
    private VehicleRepository vehicleRepository;

    public VehicleService(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    // READ - tutti i veicoli
    public List<VehicleDTO> getAllvehicle() {
        return vehicleRepository.findAll().stream()
                .map(this::toDTO)
                .toList();
    }

    // metodo che per implementare la logica del dto get all enri

    private Vehicle toEntity(VehicleDTO dto) {
        Vehicle vehicle = new Vehicle();
        vehicle.setVehicleTypeIt(dto.vehicleTypeIt());
        vehicle.setVehicleTypeEn(dto.vehicleTypeEn());
        vehicle.setBrand(dto.brand());
        vehicle.setModel(dto.model());
        vehicle.setBasePrice(dto.basePrice());
        vehicle.setImg(dto.img());
        return vehicle;
    }

    public VehicleDTO toDTO(Vehicle vehicle) {
        if (vehicle == null) {
            return null;
        }

        return new VehicleDTO(
                vehicle.getId(),
                vehicle.getVehicleTypeIt(),
                vehicle.getVehicleTypeEn(),
                vehicle.getBrand(),
                vehicle.getModel(),
                vehicle.getBasePrice(),
                vehicle.getImg(),
                vehicle.getVehicleVariations() != null
                        ? vehicle.getVehicleVariations().stream()
                                .map(this::toVariationDTO)
                                .toList()
                        : List.of());
    }

    private VehicleVariationDTO toVariationDTO(VehicleVariation var) {
        return new VehicleVariationDTO(
                var.getId(),
                var.getCc(),
                var.getImmatricolationMonth(),
                var.getImmatricolationYear(),
                var.getFuelSystemIt(),
                var.getFuelSystemEn());
    }

    // READ - un veicolo per ID
    public VehicleDTO getVehicleById(Integer id) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vehicle not found"));
        return toDTO(vehicle);
    }

    // CREATE
    public VehicleDTO createVehicle(VehicleDTO dto) {
        Vehicle vehicle = toEntity(dto);
        Vehicle saved = vehicleRepository.save(vehicle);
        return toDTO(saved);
    }

    // UPDATE
    public VehicleDTO updateVehicle(Integer id, VehicleDTO dto) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vehicle not found"));

        vehicle.setVehicleTypeIt(dto.vehicleTypeIt());
        vehicle.setVehicleTypeEn(dto.vehicleTypeEn());
        vehicle.setBrand(dto.brand());
        vehicle.setModel(dto.model());
        vehicle.setBasePrice(dto.basePrice());
        vehicle.setImg(dto.img());

        Vehicle updated = vehicleRepository.save(vehicle);
        return toDTO(updated);
    }

    // DELETE
    public void deleteVehicle(Integer id) {
        if (!vehicleRepository.existsById(id)) {
            throw new RuntimeException("Vehicle not found");
        }
        vehicleRepository.deleteById(id);
    }

    // // SEARCH - esempio: cerca per brand
    // public List<VehicleDTO> findByBrand(String brand) {
    // return vehicleRepository.findByBrand(brand).stream()
    // .map(this::toDTO)
    // .toList();
    // }

}
