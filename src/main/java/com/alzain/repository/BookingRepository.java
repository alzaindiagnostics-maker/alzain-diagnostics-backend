package com.alzain.repository;

import com.alzain.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    Optional<Booking> findByBookingId(String bookingId);
    boolean existsByBookingId(String bookingId);
    List<Booking> findByStatus(String status);
    List<Booking> findAllByOrderByCreatedAtDesc();
    long countByStatus(String status);
}
