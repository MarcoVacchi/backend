package org.lessons.vehicles.java.quoted.model;

import java.math.BigDecimal;

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
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;
}