package com.ticketbooking.service;

import com.ticketbooking.entity.Booking;
import com.ticketbooking.entity.BookingSeat;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender javaMailSender;
    private final QRCodeService qrCodeService;

    @Value("${app.mail.from}")
    private String fromEmail;

    public void sendBookingConfirmation(Booking booking) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(booking.getCustomer().getEmail());
            helper.setSubject("Booking Confirmation - " + booking.getEvent().getTitle());

            String seatNumbers = booking.getSeats().stream()
                    .map(bs -> bs.getShowSeat().getVenueSeat().getSeatLabel())
                    .collect(Collectors.joining(", "));

            String qrContent = String.format(
                    "Booking Ref: %s\nCustomer: %s %s\nEvent: %s\nVenue: %s\nDate: %s\nTime: %s\nSeats: %s",
                    booking.getBookingReference(),
                    booking.getCustomer().getFirstName(), booking.getCustomer().getLastName(),
                    booking.getEvent().getTitle(),
                    booking.getEvent().getVenue().getName(),
                    booking.getEvent().getEventDate().toString(),
                    booking.getEvent().getEventTime().toString(),
                    seatNumbers
            );

            byte[] qrCodeImage = qrCodeService.generateQRCode(qrContent);

            String htmlContent = String.format(
                    "<html><body>" +
                    "<h2>Your Booking is Confirmed!</h2>" +
                    "<p>Dear %s,</p>" +
                    "<p>Thank you for booking with us. Here are your booking details:</p>" +
                    "<ul>" +
                    "<li><b>Booking Reference:</b> %s</li>" +
                    "<li><b>Event:</b> %s</li>" +
                    "<li><b>Venue:</b> %s</li>" +
                    "<li><b>Date & Time:</b> %s at %s</li>" +
                    "<li><b>Seats:</b> %s</li>" +
                    "<li><b>Total Amount:</b> $%s</li>" +
                    "</ul>" +
                    "<p>Please present the QR code below at the venue entrance:</p>" +
                    "<div><img src='cid:qrcode' alt='Booking QR Code' /></div>" +
                    "<br><p>Best regards,<br>Ticket Booking System Team</p>" +
                    "</body></html>",
                    booking.getCustomer().getFirstName(),
                    booking.getBookingReference(),
                    booking.getEvent().getTitle(),
                    booking.getEvent().getVenue().getName(),
                    booking.getEvent().getEventDate().toString(),
                    booking.getEvent().getEventTime().toString(),
                    seatNumbers,
                    booking.getTotalAmount().toString()
            );

            helper.setText(htmlContent, true);
            helper.addInline("qrcode", new ByteArrayResource(qrCodeImage), "image/png");

            javaMailSender.send(message);
            log.info("Booking confirmation email sent to {} for booking {}", booking.getCustomer().getEmail(), booking.getBookingReference());

        } catch (Exception e) {
            log.error("Failed to send booking confirmation email to {}", booking.getCustomer().getEmail(), e);
            log.info("--- START EMAIL DUMP FOR LOCAL TESTING ---");
            log.info("To: {}", booking.getCustomer().getEmail());
            log.info("Subject: Booking Confirmation - {}", booking.getEvent().getTitle());
            log.info("Booking Reference: {}", booking.getBookingReference());
            log.info("--- END EMAIL DUMP ---");
        }
    }

    public void sendWaitlistOfferEmail(com.ticketbooking.entity.WaitlistOffer offer, String customerEmail, String customerName) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(customerEmail);
            helper.setSubject("Seat Available - " + offer.getWaitlist().getEvent().getTitle());

            String offerLink = "http://localhost:5173/offers/" + offer.getToken(); // Assuming React dev server port

            String htmlContent = String.format(
                    "<html><body>" +
                    "<h2>Great news! Seats are available!</h2>" +
                    "<p>Dear %s,</p>" +
                    "<p>Seats in the <b>%s</b> category for <b>%s</b> have just become available.</p>" +
                    "<p>You have 10 minutes to claim your tickets.</p>" +
                    "<p><b>Expiry Time:</b> %s</p>" +
                    "<p>Please click the link below to accept the offer and complete your booking:</p>" +
                    "<a href='%s' style='display:inline-block;padding:10px 20px;background-color:#4f46e5;color:white;text-decoration:none;border-radius:5px;'>Accept Offer & Book</a>" +
                    "<br><br><p>Best regards,<br>Ticket Booking System Team</p>" +
                    "</body></html>",
                    customerName,
                    offer.getWaitlist().getSeatCategory().name(),
                    offer.getWaitlist().getEvent().getTitle(),
                    offer.getExpiryTime().toString(),
                    offerLink
            );

            helper.setText(htmlContent, true);

            javaMailSender.send(message);
            log.info("Waitlist offer email sent to {} for event {}", customerEmail, offer.getWaitlist().getEvent().getTitle());

        } catch (Exception e) {
            log.error("Failed to send waitlist offer email to {}", customerEmail, e);
            log.info("--- START EMAIL DUMP FOR LOCAL TESTING ---");
            log.info("To: {}", customerEmail);
            log.info("Subject: Seat Available - {}", offer.getWaitlist().getEvent().getTitle());
            log.info("Offer Link: http://localhost:5173/offers/{}", offer.getToken());
            log.info("--- END EMAIL DUMP ---");
        }
    }
    public void sendHelpQueryEmail(String customerEmail, String query) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo("support@yourdomain.com");
            helper.setReplyTo(customerEmail);
            helper.setSubject("New Help Centre Query from " + customerEmail);

            String htmlContent = String.format(
                    "<html><body>" +
                    "<h2>New Help Centre Query</h2>" +
                    "<p><b>From:</b> %s</p>" +
                    "<p><b>Query:</b></p>" +
                    "<p>%s</p>" +
                    "</body></html>",
                    customerEmail,
                    query.replace("\n", "<br>")
            );

            helper.setText(htmlContent, true);

            javaMailSender.send(message);
            log.info("Help query email sent to support@yourdomain.com from {}", customerEmail);

        } catch (Exception e) {
            log.error("Failed to send help query email from {}", customerEmail, e);
            log.info("--- START EMAIL DUMP FOR LOCAL TESTING ---");
            log.info("To: support@yourdomain.com");
            log.info("Reply-To: {}", customerEmail);
            log.info("Subject: New Help Centre Query from {}", customerEmail);
            log.info("Query: {}", query);
            log.info("--- END EMAIL DUMP ---");
        }
    }
}
