package com.smartroutegt.backend.controller;
import com.smartroutegt.backend.entity.PassengerBoarding;
import com.smartroutegt.backend.service.PassengerBoardingService;
import org.springframework.web.bind.annotation.*;
import java.util.*;
@RestController @RequestMapping("/api/boarding")
public class PassengerBoardingController {
 private final PassengerBoardingService service;
 public PassengerBoardingController(PassengerBoardingService s){service=s;}
 public record Position(Double latitud,Double longitud){}
 public record JoinRequest(String code,Double latitud,Double longitud){}
 public record JoinTripRequest(Long tripId,Double latitud,Double longitud){}
 private Map<String,Object> view(PassengerBoarding b){if(b==null)return Map.of("active",false);Map<String,Object> m=new LinkedHashMap<>();m.put("active",b.separatedAt==null);m.put("tripId",b.trip.getId());m.put("bus",b.trip.getBus().getCodigo());m.put("route",b.trip.getRoute().getNombre());m.put("separatedAt",b.separatedAt==null?"":b.separatedAt.toString());return m;}
 @GetMapping("/me") public Map<String,Object> me(){return view(service.current());}
 @PostMapping("/join") public Map<String,Object> join(@RequestBody JoinRequest r){return view(service.join(r.code(),r.latitud(),r.longitud()));}
 @PostMapping("/join-trip") public Map<String,Object> joinTrip(@RequestBody JoinTripRequest r){return view(service.joinTrip(r.tripId(),r.latitud(),r.longitud()));}
 @PostMapping("/join-nearby") public Map<String,Object> joinNearby(@RequestBody JoinTripRequest r){return view(service.joinTrip(r.tripId(),r.latitud(),r.longitud()));}
 @PostMapping("/nearby") public List<Map<String,Object>> nearby(@RequestBody Position p){return service.nearby(p.latitud(),p.longitud());}
 @PostMapping("/location") public Map<String,Object> location(@RequestBody Position p){return view(service.update(p.latitud(),p.longitud()));}

 @PostMapping("/leave")
 public Map<String,Object> leave(){
  return view(service.leave());
 }

 @GetMapping("/trip/{tripId}/code") public Map<String,String> code(@PathVariable Long tripId){return Map.of("code",service.code(tripId));}
}
