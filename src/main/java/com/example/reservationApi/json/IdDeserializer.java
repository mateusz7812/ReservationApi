package com.example.reservationApi.json;

import com.example.reservationApi.account.AccountService;
import com.example.reservationApi.event.EventService;
import com.example.reservationApi.reservable.ReservableService;
import com.example.reservationApi.reservation.ReservationService;
import com.fasterxml.jackson.annotation.ObjectIdGenerator;
import com.fasterxml.jackson.annotation.ObjectIdResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.UUID;

@Component
public class IdDeserializer implements ObjectIdResolver {
    private static ReservableService reservableService;
    private static AccountService accountService;
    private static EventService eventService;
    private static ReservationService reservationService;

    public IdDeserializer(){
        Assert.notNull(reservableService, "repository is null");
    }

    @Autowired
    public IdDeserializer(ReservableService reservableService, AccountService accountService, EventService eventService, ReservationService reservationService){
        IdDeserializer.reservableService = reservableService;
        IdDeserializer.accountService = accountService;
        IdDeserializer.eventService = eventService;
        IdDeserializer.reservationService = reservationService;
    }

    @Override
    public void bindItem(
    final ObjectIdGenerator.IdKey id,
    final Object pojo) {
    }

    @Override
    public Object resolveId(final ObjectIdGenerator.IdKey id) {
        String name = id.scope.getSimpleName();
        UUID objectId = UUID.fromString(String.valueOf(id.key));
        Object object;
        if("Account".equals(name)){
            object = accountService.findById(objectId);
        } else if("Event".equals(name)){
            object = eventService.findById(objectId);
        } else if("Reservation".equals(name)){
            object = reservationService.findById(objectId);
        } else if("Reservable".equals(name)){
            object = reservableService.findById(objectId);
        } else {
            object = null;
        }
        return object;
    }

    @Override
    public ObjectIdResolver newForDeserialization(final Object context) {
        return this;
    }

    @Override
    public boolean canUseFor(final ObjectIdResolver resolverType) {
        return false;
    }

}
