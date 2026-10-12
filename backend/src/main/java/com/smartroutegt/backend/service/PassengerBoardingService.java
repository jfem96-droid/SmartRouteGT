package com.smartroutegt.backend.service;
import com.smartroutegt.backend.entity.*;
import com.smartroutegt.backend.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.security.SecureRandom;
@Service
public class PassengerBoardingService {
 private final UserRepository users; private final TripRepository trips; private final BusLocationRepository locations;
 private final PassengerBoardingRepository boardings; private final TripBoardingCodeRepository codes;
 private final SecureRandom random=new SecureRandom();
 public PassengerBoardingService(UserRepository u,TripRepository t,BusLocationRepository l,PassengerBoardingRepository b,TripBoardingCodeRepository c){users=u;trips=t;locations=l;boardings=b;codes=c;}
 public User user(){String email=SecurityContextHolder.getContext().getAuthentication().getName();return users.findByEmailIgnoreCase(email).orElseThrow(()->new ResponseStatusException(HttpStatus.UNAUTHORIZED));}
 private ResponseStatusException reject(String reason){return new ResponseStatusException(HttpStatus.FORBIDDEN,reason);}
 private double distance(double a,double b,double c,double d){double r=6371000,da=Math.toRadians(c-a),db=Math.toRadians(d-b);double x=Math.sin(da/2)*Math.sin(da/2)+Math.cos(Math.toRadians(a))*Math.cos(Math.toRadians(c))*Math.sin(db/2)*Math.sin(db/2);return 2*r*Math.atan2(Math.sqrt(x),Math.sqrt(1-x));}
 private BusLocation busPosition(Trip trip){BusLocation l=locations.findFirstByTripIdOrderByFechaHoraDesc(trip.getId()).orElseThrow(()->reject("Sin GPS del bus"));if(l.getFechaHora()==null||l.getFechaHora().isBefore(LocalDateTime.now().minusSeconds(60)))throw reject("GPS del bus desactualizado");return l;}
 private void coords(Double lat,Double lon){if(lat==null||lon==null||!Double.isFinite(lat)||!Double.isFinite(lon)||lat < -90||lat > 90||lon < -180||lon > 180)throw reject("Ubicacion GPS invalida");}
 @Transactional public String code(Long tripId){User actor=user();Trip t=trips.findById(tripId).orElseThrow(()->reject("Viaje inexistente"));if(!t.isActivo())throw reject("Viaje finalizado");if(actor.getRole()!=User.Role.ADMIN && (actor.getRole()!=User.Role.DRIVER || actor.getDriver()==null || !actor.getDriver().getId().equals(t.getDriver().getId())))throw reject("Sin permiso sobre el viaje");TripBoardingCode c=codes.findById(tripId).orElse(null);if(c==null){c=new TripBoardingCode();c.tripId=tripId;c.code=String.format("%08d",random.nextInt(100000000));codes.save(c);}return c.code;}
 @Transactional public PassengerBoarding join(String code,Double lat,Double lon){coords(lat,lon);User u=user();if(u.getRole()!=User.Role.PASSENGER)throw reject("Solo pasajeros");TripBoardingCode c=codes.findByCode(code==null?"":code.trim()).orElseThrow(()->reject("Codigo invalido"));Trip t=trips.findById(c.tripId).orElseThrow(()->reject("Viaje inexistente"));if(!t.isActivo())throw reject("Viaje finalizado");if(distance(lat,lon,busPosition(t).getLatitud(),busPosition(t).getLongitud())>30)throw reject("Debes estar cerca del bus para abordar");PassengerBoarding old=boardings.findFirstByPassengerIdAndSeparatedAtIsNullOrderByBoardedAtDesc(u.getId()).orElse(null);if(old!=null){if(old.trip.getId().equals(t.getId()) && old.separatedAt==null)return old;throw reject("Ya tienes un abordaje registrado");}PassengerBoarding x=new PassengerBoarding();x.passenger=u;x.trip=t;x.lastLocationAt=LocalDateTime.now();return boardings.save(x);}
 public java.util.List<java.util.Map<String,Object>> nearby(Double lat,Double lon){
  coords(lat,lon); if(user().getRole()!=User.Role.PASSENGER)throw reject("Solo pasajeros");
  java.util.List<java.util.Map<String,Object>> result=new java.util.ArrayList<>();
  for(Trip t:trips.findAll()){
   if(!t.isActivo()||t.getBus()==null||t.getRoute()==null)continue;
   try {BusLocation gps=busPosition(t); double meters=distance(lat,lon,gps.getLatitud(),gps.getLongitud());
    if(meters<=30){java.util.Map<String,Object> item=new java.util.LinkedHashMap<>();item.put("tripId",t.getId());item.put("bus",t.getBus().getCodigo());item.put("route",t.getRoute().getNombre());item.put("distanceMeters",(int)Math.round(meters));result.add(item);}
   }catch(ResponseStatusException ignored){}
  }
  return result;
 }
 @Transactional public PassengerBoarding joinTrip(Long tripId,Double lat,Double lon){
  coords(lat,lon); User u=user(); if(u.getRole()!=User.Role.PASSENGER)throw reject("Solo pasajeros");
  Trip t=trips.findById(tripId).orElseThrow(()->reject("Viaje inexistente"));
  if(!t.isActivo())throw reject("Viaje no activo");
  BusLocation gps=busPosition(t);
  if(distance(lat,lon,gps.getLatitud(),gps.getLongitud())>30)throw reject("Debes estar a 30 metros o menos del bus seleccionado");
  PassengerBoarding old=boardings.findFirstByPassengerIdAndSeparatedAtIsNullOrderByBoardedAtDesc(u.getId()).orElse(null);
  if(old!=null){if(old.trip.getId().equals(tripId)&&old.separatedAt==null)return old;throw reject("Ya tienes un abordaje registrado");}
  PassengerBoarding x=new PassengerBoarding();x.passenger=u;x.trip=t;x.lastLocationAt=LocalDateTime.now();return boardings.save(x);
 }

 @Transactional
 public PassengerBoarding update(Double lat, Double lon) {
  coords(lat, lon);
  User u = user();

  PassengerBoarding x = boardings
    .findFirstByPassengerIdAndSeparatedAtIsNullOrderByBoardedAtDesc(u.getId())
    .orElse(null);

  if (x == null) {
   return boardings
     .findFirstByPassengerIdAndClosedFalseOrderByBoardedAtDesc(u.getId())
     .orElseThrow(() -> reject("Sin abordaje"));
  }

  LocalDateTime now = LocalDateTime.now();

  if (!x.trip.isActivo()) {
   x.separatedAt = x.trip.getFechaFin() == null
     ? now : x.trip.getFechaFin();
   return boardings.save(x);
  }

  BusLocation bus = busPosition(x.trip);
  boolean far = distance(
    lat, lon, bus.getLatitud(), bus.getLongitud()
  ) > 100;

  if (far) {
   if (x.consecutiveFar == 0 ||
       x.lastLocationAt == null ||
       !x.lastLocationAt.isAfter(now.minusSeconds(10))) {
    x.consecutiveFar++;
    x.lastLocationAt = now;
   }

   if (x.consecutiveFar >= 2) {
    x.separatedAt = now;
   }
  } else {
   x.consecutiveFar = 0;
   x.lastLocationAt = now;
  }

  return boardings.save(x);
 }

 @Transactional public PassengerBoarding leave(){
  User u=user();
  if(u.getRole()!=User.Role.PASSENGER)throw reject("Solo pasajeros");
  PassengerBoarding x=boardings
    .findFirstByPassengerIdAndSeparatedAtIsNullOrderByBoardedAtDesc(u.getId())
    .orElseThrow(()->reject("No tienes un abordaje activo"));
  if(x.separatedAt!=null)throw reject("El descenso ya fue registrado");
  x.separatedAt=LocalDateTime.now();
  x.consecutiveFar=0;
  return boardings.save(x);
 }


 @Transactional
 public PassengerBoarding validateReport(Long tripId, Double lat, Double lon) {
  coords(lat, lon);
  User u = user();

  PassengerBoarding x = boardings
    .findFirstByPassengerIdAndTripIdOrderByBoardedAtDesc(u.getId(), tripId)
    .orElseThrow(() -> reject("No abordaste este viaje"));

  if (x.separatedAt == null) {
   PassengerBoarding active = boardings
     .findFirstByPassengerIdAndSeparatedAtIsNullOrderByBoardedAtDesc(u.getId())
     .orElse(null);

   if (active != null && active.id.equals(x.id)) {
    x = update(lat, lon);
   }
  }

  if (x.separatedAt != null &&
      LocalDateTime.now().isAfter(x.separatedAt.plusMinutes(5))) {
   throw reject("Plazo de reporte vencido");
  }

  if (x.separatedAt == null && x.consecutiveFar > 0) {
   throw reject("Confirmando separacion GPS; reintenta");
  }

  return x;
 }

 @Transactional
 public PassengerBoarding current() {
  User u = user();

  PassengerBoarding active = boardings
    .findFirstByPassengerIdAndSeparatedAtIsNullOrderByBoardedAtDesc(u.getId())
    .orElse(null);

  if (active != null) {
   if (active.trip.isActivo()) return active;

   active.separatedAt = active.trip.getFechaFin() == null
     ? LocalDateTime.now() : active.trip.getFechaFin();
   boardings.save(active);
  }

  PassengerBoarding recent = boardings
    .findFirstByPassengerIdAndClosedFalseOrderByBoardedAtDesc(u.getId())
    .orElse(null);

  if (recent == null) return null;

  if (recent.separatedAt != null &&
      LocalDateTime.now().isAfter(recent.separatedAt.plusMinutes(5))) {
   recent.closed = true;
   boardings.save(recent);
   return null;
  }

  return recent;
 }
}
