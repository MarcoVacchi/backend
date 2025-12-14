package org.lessons.vehicles.java.quoted.repository;

import java.util.List;

import org.lessons.vehicles.java.quoted.model.Quoted;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuotedRepository extends JpaRepository<Quoted, Integer> {
    List<Quoted> findByUserEmail(String email);
}
