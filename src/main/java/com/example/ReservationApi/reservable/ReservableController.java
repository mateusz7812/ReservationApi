package com.example.ReservationApi.reservable;

import com.example.ReservationApi.reservable.types.Seat;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/reservable")
public class ReservableController {
    private final ReservableRepository reservableRepository;

    public ReservableController(ReservableRepository reservableRepository) {
        this.reservableRepository = reservableRepository;
    }

    @PostMapping
    public void addReservable(@RequestBody List<Map<String, String>> objects) {
        for(Map<String, String> jsonObject
                :objects){
            if(jsonObject.containsKey("type")){
                if(jsonObject.get("type").equals("Seat")){
                    Reservable reservable = new Seat(jsonObject.get("name"));
                    reservableRepository.save(reservable);
                }
            }
        }
    }
}
