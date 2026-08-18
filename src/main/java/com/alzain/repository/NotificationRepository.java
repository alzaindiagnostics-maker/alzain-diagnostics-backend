package com.alzain.repository;

import com.alzain.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByBookingId(Long bookingId);

    List<Notification> findByBookingIdOrderByCreatedAtDesc(Long bookingId);

    boolean existsByBookingIdAndNotificationTypeAndStatus(Long bookingId, String notificationType, String status);
}
