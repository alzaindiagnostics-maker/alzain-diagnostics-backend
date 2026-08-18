package com.alzain.service;

import com.alzain.entity.Booking;
import com.alzain.entity.Notification;
import com.alzain.entity.Review;
import com.alzain.repository.NotificationRepository;
import com.resend.Resend;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailNotificationService {

    private final NotificationRepository notificationRepository;

    @Value("${resend.api.key:${RESEND_API_KEY:}}")
    private String resendApiKey;

    @Value("${resend.from.email:${RESEND_FROM_EMAIL:onboarding@resend.dev}}")
    private String resendFromEmail;

    @Value("${admin.notification.email:${ADMIN_EMAIL:alzaindiagnostics@gmail.com}}")
    private String adminNotificationEmail;

    @Value("${app.admin.frontend.url:http://localhost:5174}")
    private String adminFrontendUrl;

    /**
     * Send email notification to ADMIN when a customer submits a new booking.
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void sendAdminNewBookingNotification(Booking booking) {
        if (booking == null) return;

        if (notificationRepository.existsByBookingIdAndNotificationTypeAndStatus(booking.getId(), "ADMIN_NEW_BOOKING", "SENT")) {
            log.info("Admin notification already SENT for booking ID: {}. Skipping duplicate.", booking.getBookingId());
            return;
        }

        String recipient = adminNotificationEmail;
        String subject = "New Appointment Booking - AL-ZAIN DIAGNOSTICS [" + booking.getBookingId() + "]";
        String htmlContent = buildAdminBookingHtml(booking);

        sendEmailAndSaveLog(booking, recipient, "ADMIN_NEW_BOOKING", subject, htmlContent);
    }

    /**
     * Send confirmation email to CUSTOMER when admin sets status to CONFIRMED.
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void sendCustomerBookingConfirmedNotification(Booking booking) {
        if (booking == null || booking.getEmail() == null || booking.getEmail().trim().isEmpty()) {
            log.info("No customer email provided for booking {}. Skipping confirmation email.", booking != null ? booking.getBookingId() : "null");
            return;
        }

        if (notificationRepository.existsByBookingIdAndNotificationTypeAndStatus(booking.getId(), "CUSTOMER_BOOKING_CONFIRMED", "SENT")) {
            log.info("Customer confirmation already SENT for booking ID: {}. Skipping duplicate.", booking.getBookingId());
            return;
        }

        String recipient = booking.getEmail().trim();
        String subject = "Appointment Confirmed - AL-ZAIN DIAGNOSTICS [" + booking.getBookingId() + "]";
        String htmlContent = buildCustomerConfirmedHtml(booking);

        sendEmailAndSaveLog(booking, recipient, "CUSTOMER_BOOKING_CONFIRMED", subject, htmlContent);
    }

    /**
     * Send cancellation email to CUSTOMER when admin sets status to CANCELLED.
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void sendCustomerBookingCancelledNotification(Booking booking) {
        if (booking == null || booking.getEmail() == null || booking.getEmail().trim().isEmpty()) {
            log.info("No customer email provided for booking {}. Skipping cancellation email.", booking != null ? booking.getBookingId() : "null");
            return;
        }

        if (notificationRepository.existsByBookingIdAndNotificationTypeAndStatus(booking.getId(), "CUSTOMER_BOOKING_CANCELLED", "SENT")) {
            log.info("Customer cancellation notification already SENT for booking ID: {}. Skipping duplicate.", booking.getBookingId());
            return;
        }

        String recipient = booking.getEmail().trim();
        String subject = "Appointment Update - AL-ZAIN DIAGNOSTICS [" + booking.getBookingId() + "]";
        String htmlContent = buildCustomerCancelledHtml(booking);

        sendEmailAndSaveLog(booking, recipient, "CUSTOMER_BOOKING_CANCELLED", subject, htmlContent);
    }

    /**
     * Send email notification to ADMIN when a customer submits a new review.
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void sendNewReviewNotification(Review review) {
        if (review == null) return;

        String recipient = adminNotificationEmail;
        String subject = "New Customer Review - AL-ZAIN DIAGNOSTICS";
        String htmlContent = buildReviewNotificationHtml(review);

        sendDirectHtmlEmail(recipient, subject, htmlContent);
    }

    /**
     * Send website contact/enquiry email to ADMIN.
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void sendContactEnquiryNotification(String name, String phone, String email, String service, String messageText) {
        String recipient = adminNotificationEmail;
        String subject = "New Website Enquiry - AL-ZAIN DIAGNOSTICS";
        String htmlContent = buildContactEnquiryHtml(name, phone, email, service, messageText);

        sendDirectHtmlEmail(recipient, subject, htmlContent);
    }

    /**
     * Send admin password reset link.
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void sendPasswordResetNotification(String recipientEmail, String resetLink) {
        String subject = "AL-ZAIN DIAGNOSTICS - Reset Your Admin Password";
        String htmlContent = buildPasswordResetHtml(resetLink);

        sendDirectHtmlEmail(recipientEmail, subject, htmlContent);
    }

    /**
     * Retry a failed notification by notification ID.
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public boolean retryNotification(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId).orElse(null);
        if (notification == null) return false;

        Booking booking = notification.getBooking();
        String subject;
        String htmlContent;

        switch (notification.getNotificationType()) {
            case "ADMIN_NEW_BOOKING":
                subject = "New Appointment Booking - AL-ZAIN DIAGNOSTICS [" + (booking != null ? booking.getBookingId() : "") + "]";
                htmlContent = booking != null ? buildAdminBookingHtml(booking) : "";
                break;
            case "CUSTOMER_BOOKING_CONFIRMED":
                subject = "Appointment Confirmed - AL-ZAIN DIAGNOSTICS [" + (booking != null ? booking.getBookingId() : "") + "]";
                htmlContent = booking != null ? buildCustomerConfirmedHtml(booking) : "";
                break;
            case "CUSTOMER_BOOKING_CANCELLED":
                subject = "Appointment Update - AL-ZAIN DIAGNOSTICS [" + (booking != null ? booking.getBookingId() : "") + "]";
                htmlContent = booking != null ? buildCustomerCancelledHtml(booking) : "";
                break;
            default:
                log.warn("Unknown notification type for retry: {}", notification.getNotificationType());
                return false;
        }

        return attemptEmailSendAndUpdate(notification, subject, htmlContent);
    }

    private void sendEmailAndSaveLog(Booking booking, String recipient, String notificationType, String subject, String htmlContent) {
        Notification notification = Notification.builder()
                .booking(booking)
                .recipient(recipient)
                .notificationType(notificationType)
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .build();

        notification = notificationRepository.save(notification);
        attemptEmailSendAndUpdate(notification, subject, htmlContent);
    }

    private String[] sendDirectHtmlEmail(String recipient, String subject, String htmlContent) {
        if (resendApiKey == null || resendApiKey.trim().isEmpty() || resendApiKey.startsWith("re_dummy")) {
            String reason = "Resend API key is not configured (check RESEND_API_KEY environment variable).";
            log.warn("{} Skipping Resend HTTP email dispatch for [{}] to {}", reason, subject, recipient);
            return new String[]{null, reason};
        }
        try {
            Resend resend = new Resend(resendApiKey.trim());

            String fromAddress = resendFromEmail != null && !resendFromEmail.trim().isEmpty()
                    ? resendFromEmail.trim()
                    : "onboarding@resend.dev";

            CreateEmailOptions params = CreateEmailOptions.builder()
                    .from(fromAddress)
                    .to(recipient.trim())
                    .subject(subject)
                    .html(htmlContent)
                    .build();

            CreateEmailResponse response = resend.emails().send(params);
            String resendId = response != null ? response.getId() : null;

            log.info("Resend HTTP Email SUCCESS: Sent [{}] to {}. Resend ID: {}", subject, recipient, resendId);
            return new String[]{resendId, null};
        } catch (Exception e) {
            log.error("Resend HTTP Email FAILURE for [{}] to {}: {}", subject, recipient, e.getMessage());
            return new String[]{null, e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()};
        }
    }

    private boolean attemptEmailSendAndUpdate(Notification notification, String subject, String htmlContent) {
        String[] result = sendDirectHtmlEmail(notification.getRecipient(), subject, htmlContent);
        String resendId = result[0];
        String failureReason = result[1];

        if (failureReason == null) {
            notification.setStatus("SENT");
            notification.setSentAt(LocalDateTime.now());
            notification.setResendEmailId(resendId);
            notification.setFailureReason(null);
            notificationRepository.save(notification);
            log.info("Resend Success: Notification [{}] sent to {}. ID: {}", notification.getNotificationType(), notification.getRecipient(), resendId);
            return true;
        } else {
            log.error("Resend Failed! Recipient={}, Type={}, Reason={}", notification.getRecipient(), notification.getNotificationType(), failureReason);
            notification.setStatus("FAILED");
            notification.setFailureReason(failureReason);
            notificationRepository.save(notification);
            return false;
        }
    }

    // ==========================================
    // HTML EMAIL TEMPLATES
    // ==========================================

    private String buildPasswordResetHtml(String resetLink) {
        return "<!DOCTYPE html><html><head><meta charset='UTF-8'></head>"
                + "<body style='font-family: Arial, sans-serif; background-color: #f4f6f9; margin: 0; padding: 20px; color: #333;'>"
                + "<div style='max-width: 600px; margin: 0 auto; background: #ffffff; border-radius: 8px; overflow: hidden; border: 1px solid #e2e8f0;'>"
                + "<div style='background-color: #0A192F; color: #ffffff; padding: 20px; text-align: center;'>"
                + "<h1 style='margin: 0; font-size: 20px; text-transform: uppercase;'>AL-ZAIN DIAGNOSTICS</h1>"
                + "<p style='margin: 5px 0 0 0; font-size: 12px; color: #94a3b8;'>ADMIN PORTAL - PASSWORD RESET</p>"
                + "</div>"
                + "<div style='padding: 24px;'>"
                + "<h2 style='color: #0F172A; font-size: 18px; margin-top: 0;'>Password Reset Request</h2>"
                + "<p style='color: #475569; font-size: 14px;'>We received a request to reset your AL-ZAIN DIAGNOSTICS Admin account password.</p>"
                + "<div style='margin: 24px 0; text-align: center;'>"
                + "<a href='" + resetLink + "' style='background-color: #1E3A8A; color: #ffffff; text-decoration: none; padding: 14px 28px; border-radius: 6px; font-weight: bold; font-size: 15px; display: inline-block;'>RESET PASSWORD</a>"
                + "</div>"
                + "<p style='color: #64748b; font-size: 13px;'>This link will expire in <strong>30 minutes</strong>.</p>"
                + "<p style='color: #94a3b8; font-size: 12px; margin-top: 16px;'>If the button above does not work, copy and paste this link into your browser:</p>"
                + "<p style='font-size: 12px; word-break: break-all;'><a href='" + resetLink + "' style='color: #1E3A8A;'>" + resetLink + "</a></p>"
                + "<p style='color: #64748b; font-size: 12px; margin-top: 20px;'>If you did not request this password reset, you can safely ignore this email.</p>"
                + "</div>"
                + "<div style='background-color: #f1f5f9; padding: 12px 20px; text-align: center; font-size: 12px; color: #64748b;'>"
                + "AL-ZAIN DIAGNOSTICS &bull; Rajampet Road, Near V.M. Hospital, Pullampet &bull; +91 8374874335"
                + "</div>"
                + "</div></body></html>";
    }

    private String buildContactEnquiryHtml(String name, String phone, String email, String service, String messageText) {
        return "<!DOCTYPE html><html><head><meta charset='UTF-8'></head>"
                + "<body style='font-family: Arial, sans-serif; background-color: #f4f6f9; margin: 0; padding: 20px; color: #333;'>"
                + "<div style='max-width: 600px; margin: 0 auto; background: #ffffff; border-radius: 8px; overflow: hidden; border: 1px solid #e2e8f0;'>"
                + "<div style='background-color: #0A192F; color: #ffffff; padding: 20px; text-align: center;'>"
                + "<h1 style='margin: 0; font-size: 20px; text-transform: uppercase;'>AL-ZAIN DIAGNOSTICS</h1>"
                + "<p style='margin: 5px 0 0 0; font-size: 12px; color: #38bdf8;'>NEW WEBSITE ENQUIRY</p>"
                + "</div>"
                + "<div style='padding: 24px;'>"
                + "<h2 style='color: #0F172A; font-size: 18px; margin-top: 0;'>Website Contact Enquiry</h2>"
                + "<table style='width: 100%; border-collapse: collapse; margin-top: 16px; font-size: 14px;'>"
                + "<tr style='background: #f8fafc;'><td style='padding: 10px; border: 1px solid #e2e8f0; font-weight: bold; width: 35%;'>Customer Name</td><td style='padding: 10px; border: 1px solid #e2e8f0;'>" + (name != null ? name : "N/A") + "</td></tr>"
                + "<tr><td style='padding: 10px; border: 1px solid #e2e8f0; font-weight: bold;'>Phone Number</td><td style='padding: 10px; border: 1px solid #e2e8f0;'><a href='tel:" + phone + "'>" + (phone != null ? phone : "N/A") + "</a></td></tr>"
                + "<tr style='background: #f8fafc;'><td style='padding: 10px; border: 1px solid #e2e8f0; font-weight: bold;'>Email Address</td><td style='padding: 10px; border: 1px solid #e2e8f0;'>" + (email != null ? email : "N/A") + "</td></tr>"
                + "<tr><td style='padding: 10px; border: 1px solid #e2e8f0; font-weight: bold;'>Requested Test / Service</td><td style='padding: 10px; border: 1px solid #e2e8f0; color: #059669; font-weight: bold;'>" + (service != null ? service : "General Enquiry") + "</td></tr>"
                + "<tr style='background: #f8fafc;'><td style='padding: 10px; border: 1px solid #e2e8f0; font-weight: bold;'>Customer Message</td><td style='padding: 10px; border: 1px solid #e2e8f0;'>" + (messageText != null ? messageText : "None") + "</td></tr>"
                + "</table>"
                + "</div>"
                + "<div style='background-color: #f1f5f9; padding: 12px 20px; text-align: center; font-size: 12px; color: #64748b;'>"
                + "AL-ZAIN DIAGNOSTICS &bull; Rajampet Road, Near V.M. Hospital, Pullampet &bull; +91 8374874335"
                + "</div>"
                + "</div></body></html>";
    }

    private String buildReviewNotificationHtml(Review review) {
        String stars = "★".repeat(review.getRating()) + "☆".repeat(5 - review.getRating());
        return "<!DOCTYPE html><html><head><meta charset='UTF-8'></head>"
                + "<body style='font-family: Arial, sans-serif; background-color: #f4f6f9; margin: 0; padding: 20px; color: #333;'>"
                + "<div style='max-width: 600px; margin: 0 auto; background: #ffffff; border-radius: 8px; overflow: hidden; border: 1px solid #e2e8f0;'>"
                + "<div style='background-color: #0A192F; color: #ffffff; padding: 20px; text-align: center;'>"
                + "<h1 style='margin: 0; font-size: 20px; text-transform: uppercase;'>AL-ZAIN DIAGNOSTICS</h1>"
                + "<p style='margin: 5px 0 0 0; font-size: 12px; color: #f59e0b;'>NEW CUSTOMER REVIEW SUBMITTED</p>"
                + "</div>"
                + "<div style='padding: 24px;'>"
                + "<h2 style='color: #0F172A; font-size: 18px; margin-top: 0;'>New Review Pending Moderation</h2>"
                + "<p style='color: #475569; font-size: 14px;'>A new patient review has been submitted and is currently <strong>PENDING</strong> approval.</p>"
                + "<div style='background: #fffbe6; border-left: 4px solid #f59e0b; padding: 16px; margin: 16px 0; border-radius: 4px;'>"
                + "<p style='margin: 0 0 8px 0; font-size: 14px;'><strong>Customer Name:</strong> " + review.getCustomerName() + "</p>"
                + "<p style='margin: 0 0 8px 0; font-size: 14px;'><strong>Rating:</strong> <span style='color: #f59e0b; font-size: 16px;'>" + stars + " (" + review.getRating() + "/5)</span></p>"
                + "<p style='margin: 0 0 8px 0; font-size: 14px;'><strong>Review Text:</strong></p>"
                + "<p style='margin: 0; font-size: 14px; font-style: italic; color: #334155;'>\"" + review.getReviewText() + "\"</p>"
                + "<p style='margin: 12px 0 0 0; font-size: 12px; color: #64748b;'><strong>Status:</strong> PENDING</p>"
                + "</div>"
                + "<div style='margin-top: 24px; text-align: center;'>"
                + "<a href='" + adminFrontendUrl + "/reviews' style='background-color: #1E3A8A; color: #ffffff; text-decoration: none; padding: 12px 24px; border-radius: 6px; font-weight: bold; display: inline-block;'>Moderate in Admin Portal</a>"
                + "</div>"
                + "</div>"
                + "<div style='background-color: #f1f5f9; padding: 12px 20px; text-align: center; font-size: 12px; color: #64748b;'>"
                + "AL-ZAIN DIAGNOSTICS &bull; Rajampet Road, Near V.M. Hospital, Pullampet &bull; +91 8374874335"
                + "</div>"
                + "</div></body></html>";
    }

    private String buildAdminBookingHtml(Booking booking) {
        String collectionType = Boolean.TRUE.equals(booking.getIsHomeCollection()) ? "Home Sample Collection" : "Lab Visit";
        String address = booking.getAddress() != null ? booking.getAddress() : "N/A";
        String city = booking.getCity() != null ? booking.getCity() : "Pullampet";
        String pincode = booking.getPincode() != null ? booking.getPincode() : "";
        String fullAddress = Boolean.TRUE.equals(booking.getIsHomeCollection()) ? address + ", " + city + " " + pincode : "At Laboratory (Near V.M. Hospital, Pullampet)";
        String customerMsg = booking.getMessage() != null && !booking.getMessage().trim().isEmpty() ? booking.getMessage() : "None";

        return "<!DOCTYPE html><html><head><meta charset='UTF-8'></head>"
                + "<body style='font-family: Arial, sans-serif; background-color: #f4f6f9; margin: 0; padding: 20px; color: #333;'>"
                + "<div style='max-width: 600px; margin: 0 auto; background: #ffffff; border-radius: 8px; overflow: hidden; border: 1px solid #e2e8f0;'>"
                + "<div style='background-color: #0A192F; color: #ffffff; padding: 20px; text-align: center;'>"
                + "<h1 style='margin: 0; font-size: 20px; text-transform: uppercase;'>AL-ZAIN DIAGNOSTICS</h1>"
                + "<p style='margin: 5px 0 0 0; font-size: 12px; color: #94a3b8;'>ADMIN NOTIFICATION - NEW APPOINTMENT</p>"
                + "</div>"
                + "<div style='padding: 24px;'>"
                + "<h2 style='color: #0F172A; font-size: 18px; margin-top: 0;'>New Appointment Received</h2>"
                + "<p style='color: #475569; font-size: 14px;'>A new patient booking request has been submitted and is currently <strong>PENDING</strong> review.</p>"
                + "<table style='width: 100%; border-collapse: collapse; margin-top: 16px; font-size: 14px;'>"
                + "<tr style='background: #f8fafc;'><td style='padding: 10px; border: 1px solid #e2e8f0; font-weight: bold; width: 35%;'>Booking ID</td><td style='padding: 10px; border: 1px solid #e2e8f0; color: #1e3a8a; font-weight: bold;'>" + booking.getBookingId() + "</td></tr>"
                + "<tr><td style='padding: 10px; border: 1px solid #e2e8f0; font-weight: bold;'>Customer Name</td><td style='padding: 10px; border: 1px solid #e2e8f0;'>" + booking.getCustomerName() + "</td></tr>"
                + "<tr style='background: #f8fafc;'><td style='padding: 10px; border: 1px solid #e2e8f0; font-weight: bold;'>Phone Number</td><td style='padding: 10px; border: 1px solid #e2e8f0;'><a href='tel:" + booking.getPhone() + "'>" + booking.getPhone() + "</a></td></tr>"
                + "<tr><td style='padding: 10px; border: 1px solid #e2e8f0; font-weight: bold;'>Email Address</td><td style='padding: 10px; border: 1px solid #e2e8f0;'>" + (booking.getEmail() != null ? booking.getEmail() : "Not provided") + "</td></tr>"
                + "<tr style='background: #f8fafc;'><td style='padding: 10px; border: 1px solid #e2e8f0; font-weight: bold;'>Package / Test</td><td style='padding: 10px; border: 1px solid #e2e8f0; font-weight: bold; color: #059669;'>" + booking.getPackageName() + "</td></tr>"
                + "<tr><td style='padding: 10px; border: 1px solid #e2e8f0; font-weight: bold;'>Preferred Date & Time</td><td style='padding: 10px; border: 1px solid #e2e8f0;'>" + booking.getPreferredDate() + " (" + booking.getPreferredTime() + ")</td></tr>"
                + "<tr style='background: #f8fafc;'><td style='padding: 10px; border: 1px solid #e2e8f0; font-weight: bold;'>Collection Type</td><td style='padding: 10px; border: 1px solid #e2e8f0;'>" + collectionType + "</td></tr>"
                + "<tr><td style='padding: 10px; border: 1px solid #e2e8f0; font-weight: bold;'>Location Address</td><td style='padding: 10px; border: 1px solid #e2e8f0;'>" + fullAddress + "</td></tr>"
                + "<tr style='background: #f8fafc;'><td style='padding: 10px; border: 1px solid #e2e8f0; font-weight: bold;'>Customer Message</td><td style='padding: 10px; border: 1px solid #e2e8f0;'>" + customerMsg + "</td></tr>"
                + "<tr><td style='padding: 10px; border: 1px solid #e2e8f0; font-weight: bold;'>Current Status</td><td style='padding: 10px; border: 1px solid #e2e8f0;'><span style='background: #fef3c7; color: #92400e; padding: 4px 8px; border-radius: 4px; font-weight: bold;'>PENDING</span></td></tr>"
                + "</table>"
                + "<div style='margin-top: 24px; text-align: center;'>"
                + "<a href='" + adminFrontendUrl + "/bookings/" + booking.getId() + "' style='background-color: #1E3A8A; color: #ffffff; text-decoration: none; padding: 12px 24px; border-radius: 6px; font-weight: bold; display: inline-block;'>Open Admin Panel</a>"
                + "</div>"
                + "</div>"
                + "<div style='background-color: #f1f5f9; padding: 12px 20px; text-align: center; font-size: 12px; color: #64748b;'>"
                + "AL-ZAIN DIAGNOSTICS &bull; Rajampet Road, Near V.M. Hospital, Pullampet &bull; +91 8374874335"
                + "</div>"
                + "</div></body></html>";
    }

    private String buildCustomerConfirmedHtml(Booking booking) {
        String collectionType = Boolean.TRUE.equals(booking.getIsHomeCollection()) ? "Home Sample Collection" : "Lab Visit";
        String fullAddress = Boolean.TRUE.equals(booking.getIsHomeCollection()) ? (booking.getAddress() + ", " + (booking.getCity() != null ? booking.getCity() : "Pullampet")) : "Near V.M. Hospital, Pullampet";

        return "<!DOCTYPE html><html><head><meta charset='UTF-8'></head>"
                + "<body style='font-family: Arial, sans-serif; background-color: #f4f6f9; margin: 0; padding: 20px; color: #333;'>"
                + "<div style='max-width: 600px; margin: 0 auto; background: #ffffff; border-radius: 8px; overflow: hidden; border: 1px solid #e2e8f0;'>"
                + "<div style='background-color: #0A192F; color: #ffffff; padding: 20px; text-align: center;'>"
                + "<h1 style='margin: 0; font-size: 20px; text-transform: uppercase;'>AL-ZAIN DIAGNOSTICS</h1>"
                + "<p style='margin: 5px 0 0 0; font-size: 12px; color: #38bdf8;'>APPOINTMENT CONFIRMED</p>"
                + "</div>"
                + "<div style='padding: 24px;'>"
                + "<h2 style='color: #059669; font-size: 18px; margin-top: 0;'>Your Appointment is Confirmed!</h2>"
                + "<p style='color: #475569; font-size: 14px;'>Dear <strong>" + booking.getCustomerName() + "</strong>,</p>"
                + "<p style='color: #475569; font-size: 14px;'>We are pleased to inform you that your diagnostic appointment request has been <strong>CONFIRMED</strong> by our laboratory medical team.</p>"
                + "<div style='background: #f0fdf4; border-left: 4px solid #059669; padding: 16px; margin: 16px 0; border-radius: 4px;'>"
                + "<p style='margin: 0 0 8px 0; font-size: 14px;'><strong>Booking ID:</strong> <span style='color: #1e3a8a;'>" + booking.getBookingId() + "</span></p>"
                + "<p style='margin: 0 0 8px 0; font-size: 14px;'><strong>Test / Package:</strong> " + booking.getPackageName() + "</p>"
                + "<p style='margin: 0 0 8px 0; font-size: 14px;'><strong>Scheduled Date:</strong> " + booking.getPreferredDate() + "</p>"
                + "<p style='margin: 0 0 8px 0; font-size: 14px;'><strong>Time Slot:</strong> " + booking.getPreferredTime() + "</p>"
                + "<p style='margin: 0 0 8px 0; font-size: 14px;'><strong>Collection Mode:</strong> " + collectionType + "</p>"
                + "<p style='margin: 0; font-size: 14px;'><strong>Address:</strong> " + fullAddress + "</p>"
                + "</div>"
                + "<p style='color: #475569; font-size: 14px;'>Our phlebotomist / technician will be ready as scheduled. If 10-12 hours fasting is required for your test package, please follow the fasting instructions.</p>"
                + "<p style='color: #475569; font-size: 14px;'>Thank you for trusting <strong>AL-ZAIN DIAGNOSTICS</strong> with your healthcare needs.</p>"
                + "</div>"
                + "<div style='background-color: #f1f5f9; padding: 12px 20px; text-align: center; font-size: 12px; color: #64748b;'>"
                + "For queries or rescheduling, call us at <strong>+91 8374874335</strong> / <strong>+91 9949963552</strong>"
                + "</div>"
                + "</div></body></html>";
    }

    private String buildCustomerCancelledHtml(Booking booking) {
        return "<!DOCTYPE html><html><head><meta charset='UTF-8'></head>"
                + "<body style='font-family: Arial, sans-serif; background-color: #f4f6f9; margin: 0; padding: 20px; color: #333;'>"
                + "<div style='max-width: 600px; margin: 0 auto; background: #ffffff; border-radius: 8px; overflow: hidden; border: 1px solid #e2e8f0;'>"
                + "<div style='background-color: #0A192F; color: #ffffff; padding: 20px; text-align: center;'>"
                + "<h1 style='margin: 0; font-size: 20px; text-transform: uppercase;'>AL-ZAIN DIAGNOSTICS</h1>"
                + "<p style='margin: 5px 0 0 0; font-size: 12px; color: #f87171;'>APPOINTMENT UPDATE</p>"
                + "</div>"
                + "<div style='padding: 24px;'>"
                + "<h2 style='color: #dc2626; font-size: 18px; margin-top: 0;'>Appointment Request Cancelled</h2>"
                + "<p style='color: #475569; font-size: 14px;'>Dear <strong>" + booking.getCustomerName() + "</strong>,</p>"
                + "<p style='color: #475569; font-size: 14px;'>Your appointment request <strong>" + booking.getBookingId() + "</strong> for package <strong>" + booking.getPackageName() + "</strong> scheduled on " + booking.getPreferredDate() + " has been <strong>CANCELLED</strong>.</p>"
                + "<p style='color: #475569; font-size: 14px;'>If you believe this cancellation was made in error or wish to reschedule for a different time slot, please contact our help desk directly.</p>"
                + "</div>"
                + "<div style='background-color: #f1f5f9; padding: 12px 20px; text-align: center; font-size: 12px; color: #64748b;'>"
                + "Call AL-ZAIN DIAGNOSTICS: <strong>+91 8374874335</strong> / <strong>+91 9949963552</strong>"
                + "</div>"
                + "</div></body></html>";
    }
}
