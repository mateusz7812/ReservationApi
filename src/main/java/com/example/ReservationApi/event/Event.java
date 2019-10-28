package com.example.ReservationApi.event;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.UUID;

@Entity
@Table(name="events")
public class Event {
    @Id
    @GeneratedValue
    private UUID id;

    @NotNull
    private UUID accountId;

    @NotBlank
    private String name;

    @NotBlank
    private String type;

    public Event(){
        super();
    }

    public Event(UUID id, UUID accountId, String name, String type){
        super();

        this.id = id;
        this.accountId = accountId;
        this.name = name;
        this.type = type;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
