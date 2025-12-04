package org.lessons.vehicles.java.quoted.model;

import java.math.BigDecimal;
import java.util.List;
import org.lessons.vehicles.java.optionals.model.Optionals;
import org.lessons.vehicles.java.vehicle.model.Vehicle;

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
    private Vehicle vehicle;

    @OneToMany(mappedBy = "quoted", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Optionals> optionals;

    public Integer getId() {
        return id;
    }

    public BigDecimal getFinalPrice() {
        return finalPrice;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public List<Vehicle> getVehicles() {
        return (this.vehicle != null) ? List.of(this.vehicle) : List.of();
    }

    public List<Optionals> getOptionals() {
        return optionals;
    }
}