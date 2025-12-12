
package org.lessons.vehicles.java.pdf.service;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import org.lessons.vehicles.java.quoted.dto.QuotedDTO;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class PdfService {

    // COLORI AZIENDALI ELEGANTI
    private static final DeviceRgb PRIMARY_COLOR = new DeviceRgb(41, 128, 185); // Blu professionale
    private static final DeviceRgb LIGHT_BG = new DeviceRgb(245, 247, 250);     // Grigio chiarissimo
    private static final DeviceRgb TEXT_DARK = new DeviceRgb(44, 62, 80);       // Grigio scuro per testo

    public byte[] generateQuotedPdf(QuotedDTO quotedDTO) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        try (PdfWriter writer = new PdfWriter(byteArrayOutputStream);
             PdfDocument pdf = new PdfDocument(writer);
             Document document = new Document(pdf)) {

            // Impostazioni globali font (opzionale, usa default se non impostato)
            document.setFontColor(TEXT_DARK);

            // 1. INTESTAZIONE
            addHeader(document);

            // 2. INFO CLIENTE E DATA
            addCustomerSection(document, quotedDTO);

            // 3. VEICOLO BASE
            addVehicleSection(document, quotedDTO);

            // 4. DETTAGLIO CALCOLI (LA PARTE NUOVA)
            addPriceCalculationDetails(document, quotedDTO);

            // 5. OPTIONAL
            addOptionalSection(document, quotedDTO);

            // 6. TOTALE
            addFinalTotal(document, quotedDTO);

            // 7. FOOTER
            addFooter(document);

        } catch (IOException e) {
            throw new RuntimeException("Errore generazione PDF", e);
        }

        return byteArrayOutputStream.toByteArray();
    }

    // --- METODI PRIVATI DI FORMATTAZIONE ---

    private void addHeader(Document doc) {
        Table table = new Table(UnitValue.createPercentArray(new float[]{1, 1})).useAllAvailableWidth();
        
        Cell logoCell = new Cell().add(new Paragraph("VEHICLE MOTORS")
                .setFontSize(22).setBold().setFontColor(PRIMARY_COLOR))
                .setBorder(Border.NO_BORDER);

        Cell titleCell = new Cell().add(new Paragraph("PREVENTIVO DI VENDITA")
                .setFontSize(18).setBold().setTextAlignment(TextAlignment.RIGHT))
                .setBorder(Border.NO_BORDER).setVerticalAlignment(VerticalAlignment.MIDDLE);

        table.addCell(logoCell);
        table.addCell(titleCell);
        
        doc.add(table);
        doc.add(new Paragraph("\n"));
        
        // Linea divisoria
        doc.add(new LineSeparator(new com.itextpdf.kernel.pdf.canvas.draw.SolidLine(1f)).setFontColor(PRIMARY_COLOR));
        doc.add(new Paragraph("\n"));
    }

    private void addCustomerSection(Document doc, QuotedDTO dto) {
        Table table = new Table(UnitValue.createPercentArray(2)).useAllAvailableWidth();

        String clientText = String.format("Cliente:\n%s %s\n%s", 
                dto.userName(), dto.userSurname(), dto.userMail());
        
        String metaText = String.format("Data Preventivo: %s\nScadenza: 30 Giorni",
                LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

        table.addCell(new Cell().add(new Paragraph(clientText)).setBorder(Border.NO_BORDER));
        table.addCell(new Cell().add(new Paragraph(metaText).setTextAlignment(TextAlignment.RIGHT)).setBorder(Border.NO_BORDER));

        doc.add(table);
        doc.add(new Paragraph("\n"));
    }

    private void addVehicleSection(Document doc, QuotedDTO dto) {
        if (dto.vehicleDTOToQuoted() == null || dto.vehicleDTOToQuoted().isEmpty()) return;

        var v = dto.vehicleDTOToQuoted().get(0);

        doc.add(new Paragraph("VEICOLO SELEZIONATO").setBold().setFontColor(PRIMARY_COLOR));
        
        Table table = new Table(UnitValue.createPercentArray(new float[]{3, 1})).useAllAvailableWidth();
        
        // Header Tabella
        table.addHeaderCell(createHeaderCell("Modello"));
        table.addHeaderCell(createHeaderCell("Prezzo Base"));

        // Riga Dati
        table.addCell(createCell(v.brand() + " " + v.model()).setBold());
        table.addCell(createCell(formatCurrency(v.basePrice())).setTextAlignment(TextAlignment.RIGHT));

        doc.add(table);
        doc.add(new Paragraph("\n"));
    }

    private void addPriceCalculationDetails(Document doc, QuotedDTO dto) {
        // Se non ci sono aggiustamenti, saltiamo
        if (dto.priceAdjustments() == null || dto.priceAdjustments().isEmpty()) return;

        doc.add(new Paragraph("DETTAGLIO CALCOLO E MAGGIORAZIONI").setBold().setFontColor(PRIMARY_COLOR));

        Table table = new Table(UnitValue.createPercentArray(new float[]{4, 1})).useAllAvailableWidth();
        
        // Stile "Zebra" o semplice lista
        for (var adj : dto.priceAdjustments()) {
            table.addCell(createCell(adj.description()).setFontSize(10).setItalic());
            table.addCell(createCell("+ " + formatCurrency(adj.amount()))
                    .setTextAlignment(TextAlignment.RIGHT).setFontSize(10));
        }

        doc.add(table);
        doc.add(new Paragraph("\n"));
    }

    private void addOptionalSection(Document doc, QuotedDTO dto) {
        if (dto.optionalDTOtoQuoted() == null || dto.optionalDTOtoQuoted().isEmpty()) return;

        doc.add(new Paragraph("OPTIONAL AGGIUNTIVI").setBold().setFontColor(PRIMARY_COLOR));

        Table table = new Table(UnitValue.createPercentArray(new float[]{4, 1})).useAllAvailableWidth();
        
        for (var opt : dto.optionalDTOtoQuoted()) {
            table.addCell(createCell(opt.type()));
            table.addCell(createCell(formatCurrency(opt.price())).setTextAlignment(TextAlignment.RIGHT));
        }

        doc.add(table);
        doc.add(new Paragraph("\n"));
    }

    private void addFinalTotal(Document doc, QuotedDTO dto) {
        Table table = new Table(UnitValue.createPercentArray(new float[]{3, 1})).useAllAvailableWidth();
        
        Cell labelCell = new Cell().add(new Paragraph("TOTALE PREVENTIVO (IVA Incl.)")
                .setFontSize(14).setBold())
                .setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT);
        
        Cell valueCell = new Cell().add(new Paragraph(formatCurrency(dto.finalPrice()))
                .setFontSize(16).setBold().setFontColor(PRIMARY_COLOR))
                .setBorder(new SolidBorder(PRIMARY_COLOR, 1))
                .setBackgroundColor(LIGHT_BG)
                .setTextAlignment(TextAlignment.CENTER);

        table.addCell(labelCell);
        table.addCell(valueCell);

        doc.add(table);
    }

    private void addFooter(Document doc) {
        Paragraph footer = new Paragraph("\n\nQuesto preventivo è indicativo. Salvo approvazione finanziaria.")
                .setFontSize(8).setFontColor(ColorConstants.GRAY).setTextAlignment(TextAlignment.CENTER);
        doc.add(footer);
    }

    // --- HELPER UTILS ---
    
    private Cell createHeaderCell(String text) {
        return new Cell().add(new Paragraph(text).setBold().setFontColor(ColorConstants.WHITE))
                .setBackgroundColor(PRIMARY_COLOR)
                .setBorder(Border.NO_BORDER)
                .setPadding(5);
    }

    private Cell createCell(String text) {
        return new Cell().add(new Paragraph(text))
                .setBorderBottom(new SolidBorder(ColorConstants.LIGHT_GRAY, 0.5f))
                .setBorderTop(Border.NO_BORDER)
                .setBorderLeft(Border.NO_BORDER)
                .setBorderRight(Border.NO_BORDER)
                .setPadding(5);
    }

    private String formatCurrency(BigDecimal price) {
        return price != null ? "€ " + price.setScale(2, BigDecimal.ROUND_HALF_UP).toString() : "€ 0.00";
    }
}