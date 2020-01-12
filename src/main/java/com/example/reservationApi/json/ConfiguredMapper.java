package com.example.reservationApi.json;

import com.example.reservationApi.reservable.Reservable;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class ConfiguredMapper extends com.fasterxml.jackson.databind.ObjectMapper {
    public ConfiguredMapper(){
        super();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(Reservable.class, new ReservableDeserializer());
        this.registerModule(module);
    }
}
