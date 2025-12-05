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

    public List<Optionals> getOptionals() {
        return this.optionals;
    }

    public void setOptionals(List<Optionals> optionals) {
        this.optionals = optionals;
    }
}