package org.lessons.vehicles.java.optionals.model;

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
@Table(name = "optionals")
public class Optionals {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "This field cannot be blank, null or empty, and must be  min 3 char and max 100 char")
    @Size(min = 3, max = 100)
    @Column(name = "name_it", nullable = false)
    private String nameIt;

    @NotBlank(message = "This field cannot be blank, null or empty, and must be  min 3 char and max 100 char")
    @Size(min = 3, max = 100)
    @Column(name = "name_En", nullable = false)
    private String nameEn;

    @NotNull(message = "The capacity must be declared, and must be min 1")
    @Min(value = 1, message = "The capacity must be 1 or more")
    private BigDecimal price;

    // sara un array
    @NotBlank(message = "This field cannot be blank, null or empty, and must be min 3 char and max 100 char")
    @Size(min = 3, max = 100)
    @Column(name = "vehicle_type_it", nullable = false)
    private String vehicleTypeIt;

    // sara un array
    @NotBlank(message = "This field cannot be blank, null or empty, and must be min 3 char and max 100 char")
    @Size(min = 3, max = 100)
    @Column(name = "vehicle_type_En", nullable = false)
    private String vehicleTypeEn;

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNameIt() {
        return this.nameIt;
    }

    public void setNameIt(String nameIt) {
        this.nameIt = nameIt;
    }

    public String getNameEn() {
        return this.nameEn;
    }

    public void setNameEn(String nameEn) {
        this.nameEn = nameEn;
    }

    public BigDecimal getPrice() {
        return this.price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
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

}
