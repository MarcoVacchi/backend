package org.lessons.vehicles.java.optionals.repository;

import org.lessons.vehicles.java.optionals.model.Optionals;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OptionalsRepository extends JpaRepository<Optionals, Integer> {

}
