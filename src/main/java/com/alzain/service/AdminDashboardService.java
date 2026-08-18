package com.alzain.service;

import com.alzain.dto.BookingResponseDTO;
import com.alzain.dto.DashboardStatsDTO;
import com.alzain.entity.Booking;
import com.alzain.repository.BookingRepository;
import com.alzain.repository.PackageRepository;
import com.alzain.repository.TestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminDashboardService {

    @Autowired
    private PackageRepository packageRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private TestRepository testRepository;

    public DashboardStatsDTO getDashboardStats() {
        long totalPackages = packageRepository.count();
        long activePackages = packageRepository.countByActiveTrue();
        long totalBookings = bookingRepository.count();
        long pendingBookings = bookingRepository.countByStatus("PENDING");
        long confirmedBookings = bookingRepository.countByStatus("CONFIRMED");
        long completedBookings = bookingRepository.countByStatus("COMPLETED");
        long totalTests = testRepository.count();

        List<Booking> allBookingsDesc = bookingRepository.findAllByOrderByCreatedAtDesc();
        List<BookingResponseDTO> recentBookings = allBookingsDesc.stream()
                .limit(5)
                .map(this::mapToBookingDTO)
                .collect(Collectors.toList());

        return DashboardStatsDTO.builder()
                .totalPackages(totalPackages)
                .activePackages(activePackages)
                .totalBookings(totalBookings)
                .pendingBookings(pendingBookings)
                .confirmedBookings(confirmedBookings)
                .completedBookings(completedBookings)
                .totalTests(totalTests)
                .recentBookings(recentBookings)
                .build();
    }

    private BookingResponseDTO mapToBookingDTO(Booking booking) {
        return BookingResponseDTO.builder()
                .id(booking.getId())
                .bookingId(booking.getBookingId())
                .customerName(booking.getCustomerName())
                .phone(booking.getPhone())
                .email(booking.getEmail())
                .packageName(booking.getPackageName())
                .preferredDate(booking.getPreferredDate())
                .preferredTime(booking.getPreferredTime())
                .isHomeCollection(booking.getIsHomeCollection())
                .address(booking.getAddress())
                .status(booking.getStatus())
                .message(booking.getMessage())
                .createdAt(booking.getCreatedAt())
                .build();
    }
}
