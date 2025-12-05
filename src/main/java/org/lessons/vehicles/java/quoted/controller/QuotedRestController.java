package org.lessons.vehicles.java.quoted.controller;

import java.util.List;

import org.lessons.vehicles.java.quoted.dto.QuotedDTO;
import org.lessons.vehicles.java.quoted.service.QuotedService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/quotation")
public class QuotedRestController {

    @Autowired
    private QuotedService quotedService;

    @GetMapping
    public ResponseEntity<List<QuotedDTO>> getAllQuoted() {
        List<QuotedDTO> list = quotedService.getAllQuoted();
        return new ResponseEntity<>(list, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<QuotedDTO> createQuoted(@RequestBody QuotedDTO quotedDTO) {
        QuotedDTO createdQuoted = quotedService.createQuoted(quotedDTO);
        return new ResponseEntity<>(createdQuoted, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<QuotedDTO> updateQuoted(@PathVariable Integer id, @RequestBody QuotedDTO quotedDTO) {
        QuotedDTO updatedQuoted = quotedService.updateQuoted(id, quotedDTO);
        return new ResponseEntity<>(updatedQuoted, HttpStatus.OK);
    }

}