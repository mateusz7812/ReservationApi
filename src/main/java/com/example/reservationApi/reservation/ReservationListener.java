package com.example.reservationApi.reservation;

import com.example.reservationApi.observation.ReservationObservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;

@Component
public class ReservationListener {
    static ReservationObservationService reservationObservationService;

    @Autowired
    public ReservationListener(ReservationObservationService reservationObservationService){
        ReservationListener.reservationObservationService = reservationObservationService;
    }

    public ReservationListener(){
    }

    
    @PostRemove
    @PostPersist
    @PostUpdate
    private void updateObservers(Reservation reservation){
        reservationObservationService.update(reservation.getEvent().getId());
    }
}
