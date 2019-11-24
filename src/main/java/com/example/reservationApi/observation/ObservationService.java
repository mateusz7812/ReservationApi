package com.example.reservationApi.observation;

import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ObservationService {
    static private Map<UUID, List<Observer>> observers = new HashMap<>();

    static List<Observer> getObserversByObservedId(UUID itemId){
        if(observers.containsKey(itemId))
            return observers.get(itemId);
        return new ArrayList<>();
    }

    public static void updateAllObserversByObserverdId(UUID itemId, String data){
        List<Observer> observers = getObserversByObservedId(itemId);
        for (Observer observer:
             observers) {
            observer.update(data);
        }
    }

    static void addObserver(UUID itemId, Observer observer){
        if(!observers.containsKey(itemId))
            observers.put(itemId, new ArrayList<>());
        observers.get(itemId).add(observer);
    }
}
