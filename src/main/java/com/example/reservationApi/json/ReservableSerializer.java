package com.example.reservationApi.json;

import com.example.reservationApi.reservable.Reservable;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.type.WritableTypeId;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;

import java.io.IOException;

import static com.fasterxml.jackson.core.JsonToken.START_OBJECT;

public class ReservableSerializer extends JsonSerializer<Reservable> {
    @Override
    public void serialize(Reservable reservable, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStringField("id", String.valueOf(reservable.getId()));
    }

    @Override
    public void serializeWithType(Reservable reservable, JsonGenerator gen, SerializerProvider serializers, TypeSerializer typeSer) throws IOException {
        WritableTypeId typeId = typeSer.typeId(reservable, START_OBJECT);
        typeSer.writeTypePrefix(gen, typeId);
        serialize(reservable, gen, serializers);
        typeSer.writeTypeSuffix(gen, typeId);
    }
}
