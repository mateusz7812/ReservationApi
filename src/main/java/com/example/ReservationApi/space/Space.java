package com.example.ReservationApi.space;

import com.example.ReservationApi.event.Event;

import javax.persistence.*;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "spaces")
public class Space {
    @Id
    @GeneratedValue
    private UUID id;

    @OneToMany(mappedBy = "space", fetch=FetchType.EAGER)
    private List<Place> places;

    @OneToMany(mappedBy = "space")
    private List<Event> events;

    public Space(){
        places = new ArrayList<>();
        events = new ArrayList<>();
    }

    public List<Place> getPlaces() {
        return places;
    }

    public List<Event> getEvents() {
        return events;
    }

    public Place[] getFreePlaces(Event event) {
        List<Place> freePlaces = new ArrayList<>();
        for (Place place: getPlaces()) {
            if(!place.reservedFor(event))
                freePlaces.add(place);
        }
        return freePlaces.toArray(new Place[0]);
    }
}
