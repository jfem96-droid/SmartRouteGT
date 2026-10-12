package com.smartroutegt.backend.controller;
import com.smartroutegt.backend.entity.*;
import com.smartroutegt.backend.repository.*;
import com.smartroutegt.backend.service.PassengerBoardingService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;
@RestController @RequestMapping("/api/passenger-feedback")
public class PassengerFeedbackController {
 private final PassengerBoardingService boardingService;
 private final PassengerFeedbackRepository repository;
 public PassengerFeedbackController(PassengerBoardingService s,PassengerFeedbackRepository r){boardingService=s;repository=r;}
 public record Request(Long tripId,String kind,Integer stars,String reason,String comment,Double latitud,Double longitud){}
 @PostMapping @Transactional public Map<String,Object> create(@RequestBody Request req){
  if(req==null||req.tripId()==null||req.kind()==null)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Viaje y tipo obligatorios");
  String kind=req.kind().trim().toUpperCase(Locale.ROOT);
  if(!kind.equals("RATING")&&!kind.equals("COMPLIMENT"))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Tipo invalido");
  if(kind.equals("RATING")&&(req.stars()==null||req.stars()<1||req.stars()>5))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Selecciona de 1 a 5 estrellas");
  String comment=req.comment()==null?"":req.comment().trim();
  String reason=req.reason()==null?"":req.reason().trim();
  if(comment.length()>1000||reason.length()>50)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Texto demasiado largo");
  if(kind.equals("COMPLIMENT")&&reason.isBlank())throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Selecciona un motivo");
  PassengerBoarding boarding=boardingService.validateReport(req.tripId(),req.latitud(),req.longitud());
  if(repository.findByBoardingIdAndKind(boarding.id,kind).isPresent())throw new ResponseStatusException(HttpStatus.CONFLICT,"Ya registraste esta evaluacion para este abordaje");
  PassengerFeedback f=new PassengerFeedback();f.boarding=boarding;f.passenger=boarding.passenger;f.trip=boarding.trip;
  f.driver=boarding.trip.getDriver();f.bus=boarding.trip.getBus();f.kind=kind;
  f.stars=kind.equals("RATING")?req.stars():null;f.reason=reason;f.comment=comment;
  try {repository.saveAndFlush(f);}catch(org.springframework.dao.DataIntegrityViolationException ex){throw new ResponseStatusException(HttpStatus.CONFLICT,"Evaluacion ya registrada");}
  return Map.of("id",f.id,"message","Evaluacion registrada");
 }
 @GetMapping public List<Map<String,Object>> list(){
  List<Map<String,Object>> out=new ArrayList<>();
  for(PassengerFeedback f:repository.findAll()){
   Map<String,Object> m=new LinkedHashMap<>();m.put("id",f.id);m.put("kind",f.kind);m.put("stars",f.stars);
   m.put("reason",f.reason);m.put("comment",f.comment);m.put("createdAt",f.createdAt.toString());
   m.put("tripId",f.trip.getId());m.put("bus",f.bus.getCodigo());m.put("driver",f.driver.getNombre());
   m.put("passenger",f.passenger.getNombre());out.add(m);
  }return out;
 }
}
