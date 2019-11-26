package com.example.reservationApi.reservation;

import com.example.reservationApi.reservable.ReservableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservableService reservableService;

    @Autowired
    public ReservationService(ReservationRepository reservationRepository, ReservableService reservableService) {
        this.reservationRepository = reservationRepository;
        this.reservableService = reservableService;
    }

    public Reservation findById(UUID id){
        return reservationRepository.findById(id).orElseThrow();
    }

    public Reservation save(Reservation reservation){
        return reservationRepository.save(reservation);
    }

    public boolean isCorrect(Reservation reservation) {
        return reservableService.checkIfContains(reservation.getEvent().getReservable(), reservation.getReservable());
    }

    public boolean reservationIsTaken(Reservation reservation) {
        return reservableService.takenForEvent(reservation.getReservable().getId(), reservation.getEvent().getId());
    }

    public void delete(Reservation reservation) {
        reservationRepository.delete(reservation);
    }

    public Reservation update(Reservation reservation) {
        return reservationRepository.save(reservation);
    }

    public List<Reservation> findAll() {
        return reservationRepository.findAll();
    }
}
