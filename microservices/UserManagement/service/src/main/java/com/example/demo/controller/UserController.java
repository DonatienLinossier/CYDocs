package com.example.demo.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;
import com.example.demo.model.User;
import com.example.demo.security.LoginRequired;
import com.example.demo.service.PasswordResetService;
import com.example.demo.service.UserService;

@CrossOrigin(origins = "http://localhost:5173") 
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private PasswordResetService passwordResetService;

    @Autowired
    private UserService userService;


    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        try {
            User registered = userService.registerUser(user);
            return ResponseEntity.status(HttpStatus.CREATED).body(registered);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @LoginRequired
    @GetMapping("/{email}")
    public ResponseEntity<?> getUserByEmail(@PathVariable String email) {
        User user = userService.getUserByEmail(email);
        if (user != null) {
            return ResponseEntity.ok(user);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                 .body("Utilisateur introuvable pour : " + email);
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestParam String email) {
        boolean sent = passwordResetService.sendResetToken(email);
        if (sent) {
            return ResponseEntity.ok("Email de réinitialisation envoyé à " + email);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                 .body("Aucun utilisateur trouvé avec cette email.");
        }
    }


    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestParam String token,
                                                @RequestParam String newPassword) {
        boolean success = passwordResetService.resetPassword(token, newPassword);
        if (success) {
            return ResponseEntity.ok("Mot de passe réinitialisé avec succès.");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                 .body("Token invalide ou expiré.");
        }
    }

    
   @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User user) {
        try {
            String token = userService.login(user.getEmail(), user.getPassword());
            return ResponseEntity.ok(Map.of(
                "message", "Connexion réussie !",
                "token", token
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        }
    }

    @LoginRequired
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestParam String token) {
        userService.logout(token);
        return ResponseEntity.ok("Déconnexion réussie !");
    }

    @LoginRequired
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        boolean deleted = userService.deleteUser(id);
        if (deleted) {
            return ResponseEntity.ok("Utilisateur supprimé !");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body("Utilisateur introuvable : " + id);
        }
    }

}
