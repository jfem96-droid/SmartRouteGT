package com.smartroutegt.backend.controller;

import com.smartroutegt.backend.entity.User;
import com.smartroutegt.backend.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/passenger/profile")
public class PassengerProfileController {
 private final UserRepository users;
 private final PasswordEncoder encoder;
 public PassengerProfileController(UserRepository users, PasswordEncoder encoder) { this.users=users; this.encoder=encoder; }
 private User current(Authentication auth) {
  if(auth==null || !auth.isAuthenticated()) return null;
  return users.findByEmailIgnoreCase(auth.getName()).filter(u -> u.isActivo() && u.getRole()==User.Role.PASSENGER).orElse(null);
 }
 private Map<String,Object> view(User u) { return Map.of("nombre",u.getNombre(),"email",u.getEmail(),"telefono",u.getTelefono()==null?"":u.getTelefono()); }
 @GetMapping public ResponseEntity<?> get(Authentication auth) { User u=current(auth); return u==null?ResponseEntity.status(403).build():ResponseEntity.ok(view(u)); }
 public record UpdateRequest(String nombre,String email,String telefono,String passwordActual) {}
 @PutMapping @Transactional public ResponseEntity<?> update(Authentication auth,@RequestBody UpdateRequest r) {
  User u=current(auth); if(u==null)return ResponseEntity.status(403).build();
  if(r.passwordActual()==null || !encoder.matches(r.passwordActual(),u.getPassword()))return ResponseEntity.status(403).body("Contraseña actual incorrecta");
  if(r.nombre()==null||r.nombre().isBlank()||r.nombre().length()>150)return ResponseEntity.badRequest().body("Nombre inválido");
  if(r.email()==null||!r.email().trim().matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$"))return ResponseEntity.badRequest().body("Correo inválido");
  String email=r.email().trim().toLowerCase();
  if(!u.getEmail().equalsIgnoreCase(email)&&users.existsByEmailIgnoreCase(email))return ResponseEntity.badRequest().body("Correo ya registrado");
  if(r.telefono()!=null && r.telefono().length()>30)return ResponseEntity.badRequest().body("Teléfono demasiado largo");
  u.setNombre(r.nombre().trim());u.setEmail(email);u.setTelefono(r.telefono()==null?"":r.telefono().trim());users.save(u);
  return ResponseEntity.ok(view(u));
 }
 public record PasswordRequest(String actual,String nueva) {}
 @PutMapping("/password") @Transactional public ResponseEntity<?> password(Authentication auth,@RequestBody PasswordRequest r) {
  User u=current(auth);if(u==null)return ResponseEntity.status(403).build();
  if(r.actual()==null||!encoder.matches(r.actual(),u.getPassword()))return ResponseEntity.status(403).body("Contraseña actual incorrecta");
  if(r.nueva()==null||r.nueva().length()<8||r.nueva().length()>128)return ResponseEntity.badRequest().body("La nueva contraseña debe tener entre 8 y 128 caracteres");
  u.setPassword(encoder.encode(r.nueva()));users.save(u);return ResponseEntity.ok(Map.of("mensaje","Contraseña actualizada"));
 }
}
