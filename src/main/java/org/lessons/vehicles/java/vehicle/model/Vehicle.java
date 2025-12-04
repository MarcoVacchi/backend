package org.lessons.vehicles.java.vehicle.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.lessons.vehicles.java.quoted.model.Quoted;
import org.lessons.vehicles.java.vehicleVariation.model.VehicleVariation;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
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

    // sara un array
    @NotBlank(message = "This field cannot be blank, null or empty, and must be min 3 char and max 100 char")
    @Size(min = 3, max = 100)
    @Column(name = "vehicle_type_it", nullable = false)
    private String vehicleTypeIt;

    @NotBlank(message = "This field cannot be blank, null or empty, and must be min 3 char and max 100 char")
    @Size(min = 3, max = 100)
    @Column(name = "vehicle_type_en", nullable = false)
    private String vehicleTypeEn;

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
    @Size(min = 3, max = 1000)
    @Column(name = "img", length = 1000, nullable = false)
    private String img;

    // Tabelle collegate

    @OneToMany(mappedBy = "vehicle", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<VehicleVariation> vehicleVariations = new ArrayList<>();

    @OneToMany
    private List<Quoted> quoted = new ArrayList<>();

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getVehicleTypeIt() {
        return this.vehicleTypeIt;
    }

    public void setVehicleTypeIt(String vehicleTypeIt) {
        this.vehicleTypeIt = vehicleTypeIt;
    }

    public String getVehicleTypeEn() {
        return this.vehicleTypeEn;
    }

    public void setVehicleTypeEn(String vehicleTypeEn) {
        this.vehicleTypeEn = vehicleTypeEn;
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

    public List<VehicleVariation> getVehicleVariations() {
        return this.vehicleVariations;
    }

    public void setVehicleVariations(List<VehicleVariation> vehicleVariations) {
        this.vehicleVariations = vehicleVariations;
    }

}