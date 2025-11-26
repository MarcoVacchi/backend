package org.lessons.vehicles.java.vehicles.model;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "vehicles")
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "This field cannot be blank, null or empty, and must be min 3 char and max 100 char")
    @Size(min = 3, max = 100)
    @Column(name = "vehicle_type", nullable = false)
    private String vehicleType;

    @NotBlank(message = "This field cannot be blank, null or empty, and must be  min 3 char and max 100 char")
    @Size(min = 3, max = 100)
    private String brand;

    @NotBlank(message = "This field cannot be blank, null or empty, and must be min 3 char and max 100 char")
    @Size(min = 3, max = 100)
    private String model;

    @NotNull(message = "The capacity must be declared, and must be min 1")
    @Min(value = 1, message = "The capacity must be 1 or more")
    @Column(name = "base_price", nullable = false)
    private BigDecimal basePrice;

    @NotBlank(message = "This field cannot be blank, null or empty, and must be min 3 char and max 100 char")
    @Size(min = 3, max = 100)
    private String img;

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getVehicleType() {
        return this.vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public String getBrand() {
        return this.brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return this.model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public BigDecimal getBasePrice() {
        return this.basePrice;
    }

    public void setBasePrice(BigDecimal basePrice) {
        this.basePrice = basePrice;
    }

    public String getImg() {
        return this.img;
    }

    public void setImg(String img) {
        this.img = img;
    }

}