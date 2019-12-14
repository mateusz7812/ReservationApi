package com.example.reservationApi.reservable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ReservableService {
    private final ReservableRepository reservableRepository;

    @Autowired
    public ReservableService(ReservableRepository reservableRepository) {
        this.reservableRepository = reservableRepository;
    }

    public Reservable findById(UUID id) {
        return reservableRepository.findById(id).orElse(null);
    }

    public List<Reservable> findAll() {
        return reservableRepository.findAll();
    }

    public Reservable save(Reservable reservable) {
        return reservableRepository.save(reservable);

    }

    public boolean checkIfContains(Reservable container, Reservable contained) {
        container = findById(container.getId());
        contained = findById(contained.getId());
        return container.contains(contained);
    }

    public boolean takenForEvent(UUID reservableId, UUID eventId) {
        Reservable reservable = findById(reservableId);
        return reservable.takenForEvent(eventId);
    }

    public void delete(Reservable reservable) {
        reservableRepository.delete(reservable);
    }

    public Reservable update(Reservable reservable) {
        return reservableRepository.save(reservable);
    }
}
