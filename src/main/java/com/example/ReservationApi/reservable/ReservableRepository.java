package com.example.ReservationApi.reservable;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ReservableRepository extends JpaRepository<Reservable, UUID> {
}
