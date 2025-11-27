package org.lessons.vehicles.java.optionals.controller;

import java.util.List;

import org.lessons.vehicles.java.optionals.dto.OptionalsDTO;
import org.lessons.vehicles.java.optionals.service.OptionalsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/optionals")
public class OptionalsRestController {

    private OptionalsService optionalsService;

    public OptionalsRestController(OptionalsService optionalsService) {
        this.optionalsService = optionalsService;
    }

    // get
    @GetMapping
    public ResponseEntity<List<OptionalsDTO>> getAllOptionals() {
        List<OptionalsDTO> optionals = optionalsService.getAllOptional();
        return ResponseEntity.ok(optionals);
    }

}
