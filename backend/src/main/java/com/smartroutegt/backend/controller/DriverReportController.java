package com.smartroutegt.backend.controller;
import com.smartroutegt.backend.entity.*;
import com.smartroutegt.backend.repository.*;
import com.smartroutegt.backend.service.ResourceAuthorizationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController @RequestMapping("/api/driver-reports")
public class DriverReportController {
    private final DriverReportRepository reports;
    private final UserRepository users;
    private final TripRepository trips;
    private final ResourceAuthorizationService authorization;
    public DriverReportController(DriverReportRepository reports,UserRepository users,TripRepository trips,ResourceAuthorizationService authorization){
        this.reports=reports;this.users=users;this.trips=trips;this.authorization=authorization;
    }
    public record CreateRequest(Long tripId,DriverReport.Type type,DriverReport.Severity severity,String description,Double latitude,Double longitude){}
    public record StatusRequest(DriverReport.Status status,String resolution){}
    private User currentUser(){
        var auth=SecurityContextHolder.getContext().getAuthentication();
        if(auth==null)return null;
        return users.findByEmailIgnoreCase(auth.getName()).orElse(null);
    }
    @PostMapping
    public ResponseEntity<?> create(@RequestBody CreateRequest request){
        User user=currentUser();
        if(user==null||user.getRole()!=User.Role.DRIVER||user.getDriver()==null)return ResponseEntity.status(403).build();
        if(request==null||request.type()==null||request.severity()==null||request.description()==null||request.description().trim().length()<10||request.description().length()>2000)
            return ResponseEntity.badRequest().body("Tipo, gravedad y descripción (10-2000 caracteres) son obligatorios");
        if((request.latitude()==null)!=(request.longitude()==null))return ResponseEntity.badRequest().body("Coordenadas incompletas");
        if(request.latitude()!=null&&(!Double.isFinite(request.latitude())||!Double.isFinite(request.longitude())||Math.abs(request.latitude())>90||Math.abs(request.longitude())>180))
            return ResponseEntity.badRequest().body("Coordenadas inválidas");
        Trip trip=null;
        if(request.tripId()!=null){
            trip=trips.findById(request.tripId()).orElse(null);
            if(trip==null||trip.getDriver()==null||!trip.getDriver().getId().equals(user.getDriver().getId()))return ResponseEntity.status(403).build();
        }
        DriverReport report=new DriverReport();
        report.setDriver(user.getDriver());report.setTrip(trip);
        if(trip!=null){report.setBus(trip.getBus());report.setRoute(trip.getRoute());}
        report.setType(request.type());report.setSeverity(request.severity());report.setDescription(request.description().trim());
        report.setLatitude(request.latitude());report.setLongitude(request.longitude());
        return ResponseEntity.ok(reports.save(report));
    }
    @GetMapping("/mine")
    public ResponseEntity<?> mine(){
        User user=currentUser();
        if(user==null||user.getRole()!=User.Role.DRIVER||user.getDriver()==null)return ResponseEntity.status(403).build();
        return ResponseEntity.ok(reports.findByDriverIdOrderByCreatedAtDesc(user.getDriver().getId()));
    }
    @GetMapping
    public List<DriverReport> all(){return reports.findAllByOrderByCreatedAtDesc();}
    @PutMapping("/{id}/status")
    public ResponseEntity<?> status(@PathVariable Long id,@RequestBody StatusRequest request){
        if(!authorization.isAdmin())return ResponseEntity.status(403).build();
        DriverReport report=reports.findById(id).orElse(null);
        if(report==null)return ResponseEntity.notFound().build();
        if(request==null||request.status()==null||request.status()==DriverReport.Status.PENDING||report.getStatus()==DriverReport.Status.RESOLVED)
            return ResponseEntity.badRequest().body("Transición de estado inválida");
        if(request.status()==DriverReport.Status.RESOLVED&&(request.resolution()==null||request.resolution().isBlank()))
            return ResponseEntity.badRequest().body("Se requiere resolución");
        if(request.resolution()!=null&&request.resolution().length()>2000)return ResponseEntity.badRequest().body("Resolución demasiado larga");
        report.setStatus(request.status());report.setResolution(request.resolution());report.setUpdatedAt(LocalDateTime.now());
        return ResponseEntity.ok(reports.save(report));
    }
}
