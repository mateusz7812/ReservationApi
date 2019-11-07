package com.example.ReservationApi.reservable;

import com.example.ReservationApi.reservable.types.Seat;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reservable")
public class ReservableController {
    private final ReservableRepository reservableRepository;
    private ObjectMapper mapper = new ObjectMapper();

    public ReservableController(ReservableRepository reservableRepository) {
        this.reservableRepository = reservableRepository;
    }

    @GetMapping
    public List<Map<String, String>> getAllReservableObjects() throws JSONException {
        List<Reservable> reservableList = reservableRepository.findAll();
        List<Map<String, String>> jsonObjectList = new ArrayList<>();
        for(Reservable reservable: reservableList){
            Map<String, String> jsonObject = mapper.convertValue(reservable, new TypeReference<Map<String,String>>(){});
            jsonObject.put("type", "Seat");
            jsonObjectList.add(jsonObject);
        }
        return jsonObjectList;
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
