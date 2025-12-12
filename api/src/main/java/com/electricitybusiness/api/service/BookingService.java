package com.electricitybusiness.api.service;

import com.electricitybusiness.api.dto.booking.BookingStatusDTO;
import com.electricitybusiness.api.exception.ConflictException;
import com.electricitybusiness.api.model.*;
import com.electricitybusiness.api.repository.BookingRepository;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.*;
import java.util.*;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BookingService {
    private final BookingRepository bookingRepository;
    private final BookingSchedulerService bookingSchedulerService;

    /**
     * Récupère toutes les réservations.
     * @return Une liste de toutes les réservations
     */
    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    /**
     * Récupère une réservation par son ID.
     * @param id L'identifiant de la réservation à récupérer
     * @return Un Optional contenant la réservation si trouvée, sinon vide
     */
    public Optional<Booking> getBookingById(Long id) {
        return bookingRepository.findById(id);
    }


    /**
     * Crée une nouvelle réservation.
     * @param booking La réservation à enregistrer
     * @return La réservation enregistrée
     */
    public Booking saveBooking(Booking booking) {
        if (booking.getStartingDate().isAfter(booking.getEndingDate())) {
            throw new IllegalArgumentException("La date de début de réservation ne peut pas être après la date de fin.");
        }

        if (booking.getStartingDate().isBefore(LocalDateTime.now().minusMinutes(5))) {
            throw new IllegalArgumentException("La date de début de réservation ne peut pas être dans le passé.");
        }

        List<Booking> overlappingBookings = bookingRepository.findOverlappingBookings(
                booking.getTerminal(),
                booking.getStartingDate(),
                booking.getEndingDate()
        );

        if (!overlappingBookings.isEmpty()) {
            throw new ConflictException("Le terminal est déjà réservé pour la période spécifiée.");
        }

        LocalDateTime startingDateTime = booking.getStartingDate();
        ZoneId userTimeZone = ZoneId.of("Europe/Paris");
        ZonedDateTime zonedBookingStart = startingDateTime.atZone(userTimeZone);
        Instant bookingStartInstant = zonedBookingStart.toInstant();
        Instant now = Instant.now();
        Duration thirtyMinutes = Duration.ofMinutes(30);
        Duration timeUntilBooking = Duration.between(now, bookingStartInstant);

        if (timeUntilBooking.isNegative() || timeUntilBooking.compareTo(thirtyMinutes) < 0) {
            booking.setStatusBooking(BookingStatus.ACCEPTEE);
        } else {
            booking.setStatusBooking(BookingStatus.EN_ATTENTE);
        }

        Booking savedBooking = bookingRepository.save(booking);

        if (savedBooking.getStatusBooking() == BookingStatus.EN_ATTENTE) {
            bookingSchedulerService.scheduleAutoValidationTask(savedBooking.getPublicId(), bookingStartInstant);
        }

        bookingSchedulerService.scheduleBookingTasks(savedBooking);

        return savedBooking;
    }


    /**
     * Met à jour une réservation existante.
     * @param id L'identifiant de la réservation à mettre à jour
     * @param booking La réservation avec les nouvelles informations
     * @return La réservation mise à jour
     */
    public Booking updateBooking(Long id, Booking booking) {
        booking.setIdBooking(id);
        return bookingRepository.save(booking);
    }

    /**
     * Supprime une réservation.
     * @param id L'identifiant de la réservation à supprimer
     */
    public void deleteBookingById(Long id) {
        bookingRepository.deleteById(id);
    }

    /**
     * Vérifie si une réservation existe par son ID.
     * @param id L'identifiant de la réservation à vérifier
     * @return true si la réservation existe, false sinon
     */
    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        return bookingRepository.existsById(id);
    }

    /**
     * Récupère les réservations par utilisateur.
     * @param user L'utilisateur associé aux réservations
     * @return Une liste de réservations correspondant à l'utilisateur
     */
    @Transactional(readOnly = true)
    public List<Booking> findByUser(User user) {
        return bookingRepository.findByUser(user);
    }

    /**
     * Récupère les réservations par borne.
     * @param terminal La borne associée aux réservations
     * @return Une liste de réservations correspondant à la borne
     */
    @Transactional(readOnly = true)
    public List<Booking> findByTerminal(Terminal terminal) {
        return bookingRepository.findByTerminal(terminal);
    }

    /**
     * Récupère les réservations par statut.
     * @param status Le statut des réservations à récupérer
     * @return Une liste de réservations correspondant au statut
     */
    @Transactional(readOnly = true)
    public List<Booking> findByStatusBooking(BookingStatus status) {
        return bookingRepository.findByStatusBooking(status);
    }

    /**
     * Récupère les réservations par utilisateur et statut.
     * @param user L'utilisateur associé aux réservations
     * @param status Le statut des réservations à récupérer
     * @return Une liste de réservations correspondant à l'utilisateur et au statut
     */
    @Transactional(readOnly = true)
    public List<Booking> findByUserAndStatusBooking(User user, BookingStatus status) {
        return bookingRepository.findByUserAndStatusBooking(user, status);
    }

    /**
     * Récupère les réservations par borne et statut.
     * @param terminal La borne associée aux réservations
     * @param status Le statut des réservations à récupérer
     * @return Une liste de réservations correspondant à la borne et au statut
     */
    @Transactional(readOnly = true)
    public List<Booking> findByTerminalAndStatusBooking(Terminal terminal, BookingStatus status) {
        return bookingRepository.findByTerminalAndStatusBooking(terminal, status);
    }

    // méthode user

    /**
     * Récupère les réservations par utilisateur.
     * @param user L'utilisateur associé aux réservations
     * @return Une liste de réservations correspondant à l'utilisateur
     */
    @Transactional(readOnly = true)
    public List<Booking> getBookingsByUserClient(
            User user,
            LocalDateTime startingDate,
            LocalDateTime endingDate,
            String orderBooking,
            BookingStatus statusBooking
    ) {
        if (orderBooking != null && !orderBooking.equalsIgnoreCase("ASC") && !orderBooking.equalsIgnoreCase("DESC")) {
            orderBooking = "ASC";
        }
        return bookingRepository.findBookingsByUserMyBookings(user, startingDate, endingDate, orderBooking, statusBooking);
    }

    @Transactional(readOnly = true)
    public List<Booking> getBookingsByUserOwner(User user) { return bookingRepository.findBookingsByUserOwner(user); }

    /**
     * Récupère les réservations par utilisateur et statut.
     * @param user L'utilisateur associé aux réservations
     * @param status Le statut des réservations à récupérer
     * @return Une liste de réservations correspondant à l'utilisateur et au statut
     */
    @Transactional(readOnly = true)
    public List<Booking> getBookingsByUserAndStatusBooking(User user, BookingStatus status) { return bookingRepository.findBookingsByUserAndStatusBooking(user, status); }

    /**
     * Supprime une voiture.
     * @param publicId L'identifiant de la voiture à supprimer
     */
    public void deleteBookingByPublicId(UUID publicId) {
        bookingRepository.deleteBookingByPublicId(publicId);
    }

    /**
     * Vérifie si une voiture existe.
     * @param publicId L'identifiant de la voiture à vérifier
     * @return true si la voiture existe, sinon false
     */
    @Transactional(readOnly = true)
    public boolean existsByPublicId(UUID publicId) {
        return bookingRepository.findByPublicId(publicId).isPresent();
    }

    /**
     * Met à jour une voiture existant.
     * @param publicId L'identifiant de la voiture à mettre à jour
     * @param booking La voiture avec les nouvelles informations
     * @return La voiture mis à jour
     */
    public Booking updateBooking(UUID publicId, Booking booking) {
        Booking existing = bookingRepository.findByPublicId(publicId)
                .orElseThrow(() -> new IllegalArgumentException("Booking with publicId not found: " + publicId));

        booking.setIdBooking(existing.getIdBooking());
        booking.setPublicId(existing.getPublicId());

        User existingUser = existing.getUser();
        if (booking.getUser() == null) {
            booking.setUser(existingUser);
        }
        return bookingRepository.save(booking);
    }

    public Booking updateBookingStatus(UUID publicId, BookingStatusDTO dto) {
        Booking existing = bookingRepository.findByPublicId(publicId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found: " + publicId));
        existing.setStatusBooking(dto.getStatusBooking());
        return bookingRepository.saveAndFlush(existing);
    }

    // Création de pdf de la réservation

    /**
     * Génère un PDF de la réservation.
     * @param publicId L'identifiant public de la réservation
     * @return Un tableau d'octets représentant le PDF généré
     * @throws Exception En cas d'erreur lors de la génération du PDF
     */
    public byte[] generateBookingPdf(UUID publicId) throws Exception {
        Booking booking = bookingRepository.findByPublicId(publicId)
                .orElseThrow(() -> new IllegalArgumentException("Booking with publicId not found: " + publicId));

        // Création du document PDF
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(out);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc);

        // Charger les polices
        PdfFont titleFont = PdfFontFactory.createFont(com.itextpdf.io.font.constants.StandardFonts.HELVETICA_BOLD);
        PdfFont headerFont = PdfFontFactory.createFont(com.itextpdf.io.font.constants.StandardFonts.HELVETICA_BOLD);
        PdfFont normalFont = PdfFontFactory.createFont(com.itextpdf.io.font.constants.StandardFonts.HELVETICA);
        PdfFont smallFont = PdfFontFactory.createFont(com.itextpdf.io.font.constants.StandardFonts.HELVETICA);

        // Titre centré
        Paragraph title = new Paragraph("FACTURE - RÉSERVATION")
                .setFont(titleFont)
                .setFontSize(18)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20);
        document.add(title);

        // Tableau d'informations (2 colonnes)
        Table infoTable = new Table(new float[]{1, 1});
        infoTable.setWidth(UnitValue.createPercentValue(100));

        // Cellule fournisseur
        com.itextpdf.layout.element.Cell fournisseurCell = new com.itextpdf.layout.element.Cell()
                .setBorder(null)
                .add(new Paragraph("Fournisseur :").setFont(headerFont).setFontSize(13))
                .add(new Paragraph(
                        booking.getTerminal().getPlace().getUser().getSurnameUser() + " " +
                                booking.getTerminal().getPlace().getUser().getFirstName())
                        .setFont(normalFont))
                .add(new Paragraph("Email : " +
                        booking.getTerminal().getPlace().getUser().getEmailUser())
                        .setFont(smallFont).setFontSize(9));

        // Cellule client
        com.itextpdf.layout.element.Cell clientCell = new com.itextpdf.layout.element.Cell()
                .setBorder(null)
                .add(new Paragraph("Client :").setFont(headerFont).setFontSize(13))
                .add(new Paragraph(
                        booking.getUser().getSurnameUser() + " " +
                                booking.getUser().getFirstName())
                        .setFont(normalFont))
                .add(new Paragraph("Email : " + booking.getUser().getEmailUser())
                        .setFont(smallFont).setFontSize(9));

        infoTable.addCell(fournisseurCell);
        infoTable.addCell(clientCell);
        document.add(infoTable);

        // Tableau des détails (2 colonnes)
        Table detailsTable = new Table(new float[]{1, 2});
        detailsTable.setWidth(UnitValue.createPercentValue(100));

        // Ajouter les lignes de détails
        detailsTable.addCell(createCell("Numéro de facture :", headerFont, 13));
        detailsTable.addCell(createCell(booking.getPublicId().toString(), normalFont, 11));

        detailsTable.addCell(createCell("Date de paiement :", headerFont, 13));
        detailsTable.addCell(createCell(String.valueOf(booking.getPaymentDate()), normalFont, 11));

        detailsTable.addCell(createCell("Début de location :", headerFont, 13));
        detailsTable.addCell(createCell(String.valueOf(booking.getStartingDate()), normalFont, 11));

        detailsTable.addCell(createCell("Fin de location :", headerFont, 13));
        detailsTable.addCell(createCell(String.valueOf(booking.getEndingDate()), normalFont, 11));

        document.add(detailsTable);

        // Tableau des prix
        Table priceTable = new Table(new float[]{2, 1});
        priceTable.setWidth(UnitValue.createPercentValue(100));

        // En-têtes
        priceTable.addHeaderCell(createCell("Description", headerFont, 13).setTextAlignment(TextAlignment.CENTER));
        priceTable.addHeaderCell(createCell("Montant (€)", headerFont, 13).setTextAlignment(TextAlignment.CENTER));

        // Lignes de prix
        priceTable.addCell(createCell("Location de borne", normalFont, 11));
        priceTable.addCell(createCell(String.format("%.2f", booking.getTerminal().getPrice()), normalFont, 11));

        // Option si présente
        if (booking.getOption() != null) {
            priceTable.addCell(createCell("Option : " + booking.getOption().getNameOption(), normalFont, 11));
            priceTable.addCell(createCell(String.format("%.2f", booking.getOption().getPriceOption()), normalFont, 11));
        }

        // Ligne de total
        com.itextpdf.layout.element.Cell totalLabelCell = createCell("Total TTC", headerFont, 13)
                .setTextAlignment(TextAlignment.RIGHT)
                .setBorderTop(new SolidBorder(ColorConstants.BLACK,2f));
        com.itextpdf.layout.element.Cell totalValueCell = createCell(String.format("%.2f", booking.getTotalAmount()), headerFont, 13)
                .setTextAlignment(TextAlignment.RIGHT)
                .setBorderTop(new SolidBorder(ColorConstants.BLACK,2f));

        priceTable.addCell(totalLabelCell);
        priceTable.addCell(totalValueCell);

        document.add(priceTable);

        document.close();

        return out.toByteArray();
    }

    /**
     * Crée une cellule de tableau avec du texte formaté.
     * @param text Le texte à afficher dans la cellule
     * @param font La police à utiliser pour le texte
     * @param fontSize La taille de la police
     * @return La cellule formatée
     */
    private com.itextpdf.layout.element.Cell createCell(String text, PdfFont font, float fontSize) {
        return new com.itextpdf.layout.element.Cell()
                .setBorder(null)
                .setPadding(5)
                .add(new Paragraph(text).setFont(font).setFontSize(fontSize));
    }

    /**
     * Génère un fichier Excel des réservations pour un utilisateur donné.
     * @param user L'utilisateur dont les réservations doivent être exportées
     * @return Un tableau d'octets représentant le fichier Excel généré
     * @throws Exception En cas d'erreur lors de la génération du fichier Excel
     */
    public byte[] generateBookingExcel(User user) throws Exception {
        List<Booking> bookings = bookingRepository.findByUser(user);

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Bookings");

        // Titre des colonnes
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Date de début");
        headerRow.createCell(1).setCellValue("Date de fin");
        headerRow.createCell(2).setCellValue("Borne");
        headerRow.createCell(3).setCellValue("Lieu");
        headerRow.createCell(4).setCellValue("Ville");
        headerRow.createCell(5).setCellValue("Utilisateur");
        headerRow.createCell(6).setCellValue("Statut");
        headerRow.createCell(7).setCellValue("Montant total (€)");
        headerRow.createCell(8).setCellValue("Status réservation");

        // Style pour les dates
        CreationHelper createHelper = workbook.getCreationHelper();
        CellStyle dateTimeStyle = workbook.createCellStyle();
        short dateFormat = createHelper.createDataFormat().getFormat("yyyy-mm-dd hh:mm");
        dateTimeStyle.setDataFormat(dateFormat);

        // Données par ligne
        int rowNum = 1;
        for (Booking booking : bookings) {
            Row row = sheet.createRow(rowNum++);

            // Starting date
            if (booking.getStartingDate() != null) {
                org.apache.poi.ss.usermodel.Cell startCell = row.createCell(0);
                Date startDate = Date.from(
                        booking.getStartingDate().atZone(java.time.ZoneId.systemDefault()).toInstant()
                );
                startCell.setCellValue(startDate);
                startCell.setCellStyle(dateTimeStyle);
            }

            // Ending date
            if (booking.getEndingDate() != null) {
                org.apache.poi.ss.usermodel.Cell endCell = row.createCell(1);
                Date endDate = Date.from(
                        booking.getEndingDate().atZone(java.time.ZoneId.systemDefault()).toInstant()
                );
                endCell.setCellValue(endDate);
                endCell.setCellStyle(dateTimeStyle);
            }

            row.createCell(2).setCellValue(booking.getTerminal().getNameTerminal());
            row.createCell(3).setCellValue(booking.getTerminal().getPlace().getAddress().getAddress());
            row.createCell(4).setCellValue(booking.getTerminal().getPlace().getAddress().getCity());
            row.createCell(5).setCellValue(booking.getTerminal().getPlace().getUser().getSurnameUser() + " " +
                    booking.getTerminal().getPlace().getUser().getFirstName());
            row.createCell(6).setCellValue(booking.getStatusBooking().toString());
            row.createCell(7).setCellValue(booking.getTotalAmount().doubleValue());
            row.createCell(8).setCellValue(booking.getStatusBooking().toString());
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return outputStream.toByteArray();
    }

    /**
     * Récupère tous les statuts de réservation disponibles.
     * @return Une liste de tous les statuts de réservation
     */
    public List<BookingStatus> getAllBookingStatus() {
        return Arrays.asList(BookingStatus.values());
    }
}
