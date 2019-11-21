package com.example.reservationApi.json;

import com.example.reservationApi.reservable.Reservable;
import com.example.reservationApi.reservable.ReservableService;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class ReservableDeserializer extends JsonDeserializer {
    private static ReservableService reservableService;

    public ReservableDeserializer(){}

    @Autowired
    public ReservableDeserializer(ReservableService reservableService){
        ReservableDeserializer.reservableService = reservableService;
    }

    @Override
    public Object deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException{
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);
        String text = node.toString();
        if(text.startsWith("[")){
            ObjectMapper objectMapper = new ObjectMapper();
            String[] strings = objectMapper.readValue(text, String[].class);
            List<Reservable> reservables = new ArrayList<>();
            for (String stringId :
                    strings) {
                UUID uuid = UUID.fromString(stringId);
                Reservable reservable = reservableService.findById(uuid);
                reservables.add(reservable);
            }
            return reservables;
        }
        UUID uuid = UUID.fromString(node.asText());
        return reservableService.findById(uuid);
    }
}
