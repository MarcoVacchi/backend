package org.lessons.vehicles.java.quoted.model;

import java.math.BigDecimal;
import java.util.List;
import org.lessons.vehicles.java.optionals.model.Optionals;
import org.lessons.vehicles.java.vehicle.model.Vehicle;
import org.lessons.vehicles.java.vehicleVariation.model.VehicleVariation;

import jakarta.persistence.*;

@Entity
@Table(name = "quoted")
public class Quoted {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "final_price", nullable = false)
    private BigDecimal finalPrice;

    @ManyToOne
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    @ManyToOne
    @JoinColumn(name = "vehicle_variation_id")
    private VehicleVariation vehicleVariation;

    @ManyToMany
    @JoinTable(name = "quoted_optionals", joinColumns = @JoinColumn(name = "quoted_id"), inverseJoinColumns = @JoinColumn(name = "optional_id"))
    private List<Optionals> optionals;

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public BigDecimal getFinalPrice() {
        return this.finalPrice;
    }

    public void setFinalPrice(BigDecimal finalPrice) {
        this.finalPrice = finalPrice;
    }

    public Vehicle getVehicle() {
        return this.vehicle;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    public VehicleVariation getVehicleVariation() {
        return this.vehicleVariation;
    }

    public void setVehicleVariation(VehicleVariation vehicleVariation) {
        this.vehicleVariation = vehicleVariation;
    }

    public List<Optionals> getOptionals() {
        return this.optionals;
    }

    public void setOptionals(List<Optionals> optionals) {
        this.optionals = optionals;
    }
}