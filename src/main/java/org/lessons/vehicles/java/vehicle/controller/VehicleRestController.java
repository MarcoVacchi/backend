package org.lessons.vehicles.java.vehicle.controller;

import java.util.List;

import org.lessons.vehicles.java.vehicle.dto.VehicleDTO;
import org.lessons.vehicles.java.vehicle.service.VehicleService;
//import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
//import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.PutMapping;
//import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

//import jakarta.validation.Valid;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/vehicle")
public class VehicleRestController {

    private final VehicleService vehicleService;

    public VehicleRestController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    // GET
    @GetMapping
    public ResponseEntity<List<VehicleDTO>> getAllVehicles() {
        List<VehicleDTO> vehicles = vehicleService.getAllvehicle();
        return ResponseEntity.ok(vehicles);
    }

    // GET
    @GetMapping("/{id}")
    public ResponseEntity<VehicleDTO> getVehicleById(@PathVariable Integer id) {
        try {
            VehicleDTO vehicle = vehicleService.getVehicleById(id);
            return ResponseEntity.ok(vehicle);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // // POST
    // @PostMapping
    // public ResponseEntity<VehicleDTO> createVehicle(@Valid @RequestBody
    // VehicleDTO vehicleDTO) {
    // try {
    // VehicleDTO created = vehicleService.createVehicle(vehicleDTO);
    // return ResponseEntity.status(HttpStatus.CREATED).body(created);
    // } catch (Exception e) {
    // return ResponseEntity.badRequest().build();
    // }
    // }

    // // PUT
    // @PutMapping("/{id}")
    // public ResponseEntity<VehicleDTO> updateVehicle(
    // @PathVariable Integer id,
    // @Valid @RequestBody VehicleDTO vehicleDTO) {
    // try {
    // VehicleDTO updated = vehicleService.updateVehicle(id, vehicleDTO);
    // return ResponseEntity.ok(updated);
    // } catch (RuntimeException e) {
    // return ResponseEntity.notFound().build();
    // }
    // }

    // // DELETE
    // @DeleteMapping("/{id}")
    // public ResponseEntity<Void> deleteVehicle(@PathVariable Integer id) {
    // try {
    // vehicleService.deleteVehicle(id);
    // return ResponseEntity.noContent().build();
    // } catch (RuntimeException e) {
    // return ResponseEntity.notFound().build();
    // }
    // }
}