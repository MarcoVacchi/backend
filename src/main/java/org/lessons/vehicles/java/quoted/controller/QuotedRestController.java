package org.lessons.vehicles.java.quoted.controller;

import java.util.List;

import org.lessons.vehicles.java.quoted.dto.QuotedDTO;
import org.lessons.vehicles.java.quoted.service.QuotedService;
import org.lessons.vehicles.java.pdf.service.PdfService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/quotation")
public class QuotedRestController {

    @Autowired
    private QuotedService quotedService;

    @Autowired
    private PdfService pdfService;

    @GetMapping
    public ResponseEntity<List<QuotedDTO>> getAllQuoted() {
        List<QuotedDTO> list = quotedService.getAllQuoted();
        return new ResponseEntity<>(list, HttpStatus.OK);
    }

    @GetMapping("/search")
    public ResponseEntity<List<QuotedDTO>> getQuotedByEmail(@RequestParam("mail") String mail) {

        if (mail == null || mail.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        List<QuotedDTO> quotations = quotedService.getQuotedByUserMail(mail);

        return ResponseEntity.ok(quotations);
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> downloadQuotedPdf(@PathVariable Integer id) {

        // 1. Recupera il DTO
        QuotedDTO quoted = quotedService.getQuotedById(id);

        // 2. Genera il PDF
        byte[] pdfBytes = pdfService.generateQuotedPdf(quoted);

        // Pulizia nome file: sostituiamo gli spazi con underscore per evitare errori nei browser
        String safeUserName = quoted.userName() != null ? quoted.userName().replace(" ", "_") : "Cliente";
        String filename = String.format("preventivo_%s_%d.pdf", safeUserName, id);

        // 3. Imposta gli Headers corretti
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        
        // USA "inline" se vuoi che il PDF si apra nel browser.
        // USA "attachment" se vuoi che venga scaricato forzatamente.
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=" + filename);
        
        headers.setContentLength(pdfBytes.length);

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
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