package com.example.demo.controller;

import com.cyFramework.core.Message;
import com.example.demo.model.User;
import com.example.demo.service.PasswordResetService;
import com.example.demo.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

// Imports statiques pour Mockito et MockMvc (rend le code plus lisible)
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class) 
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc; // Simule le navigateur/Postman

    @MockBean
    private UserService userService; // On simule le UserService

    @MockBean
    private PasswordResetService passwordResetService; // On simule le PasswordResetService

    @Autowired
    private ObjectMapper objectMapper; // Pour convertir les objets Java en JSON

    // --- TEST 1 : INSCRIPTION (Register) ---
    @Test
    void registerUser_ShouldReturn201_WhenSuccess() throws Exception {
        User inputUser = new User();
        inputUser.setEmail("new@test.com");
        inputUser.setPassword("123456");

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setEmail("new@test.com");

        // Quand le contrôleur appelle le service, on retourne un utilisateur avec un ID
        when(userService.registerUser(any(User.class))).thenReturn(savedUser);

        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inputUser)))
                .andExpect(status().isCreated()) // Vérifie le code 201 Created
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("new@test.com"));
    }

    // --- TEST 2 : LOGIN ---
    @Test
    void login_ShouldReturnToken_WhenSuccess() throws Exception {
        User loginRequest = new User();
        loginRequest.setEmail("younes@test.com");
        loginRequest.setPassword("secret");

        User dbUser = new User();
        dbUser.setId(10L);
        dbUser.setFirstName("Younes");
        dbUser.setLastName("Sabri");

        // On simule un login réussi
        when(userService.login("younes@test.com", "secret")).thenReturn("fake-jwt-token");
        when(userService.getUserByEmail("younes@test.com")).thenReturn(dbUser);

        mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk()) // Vérifie le code 200 OK
                .andExpect(jsonPath("$.token").value("fake-jwt-token"))
                .andExpect(jsonPath("$.firstName").value("Younes"));
    }

    @Test
    void login_ShouldReturn401_WhenBadCredentials() throws Exception {
        User loginRequest = new User();
        loginRequest.setEmail("bad@test.com");
        loginRequest.setPassword("wrong");

        // On simule une erreur de login
        when(userService.login(anyString(), anyString()))
                .thenThrow(new RuntimeException("Email ou mot de passe incorrect !"));

        mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized()); // Vérifie le code 401
    }

    // --- TEST 3 : ENVOI MESSAGE ACTEUR (Important pour ton fix) ---
    @Test
    void sendActorMessage_ShouldUseCorrectActorName() throws Exception {
        // Ce test vérifie que tu utilises bien "user-management" et plus "UserService"
        
        // On simule l'appel (ne rien faire)
        doNothing().when(userService).envoyerMessage(anyString(), any(Message.class));

        mockMvc.perform(post("/api/users/actor-test")
                .param("action", "TEST_PING"))
                .andExpect(status().isOk())
                .andExpect(content().string("Message envoyé à user-management : TEST_PING"));

        // VÉRIFICATION CRUCIALE : On vérifie que le contrôleur a bien appelé avec "user-management"
        verify(userService, times(1)).envoyerMessage(eq("user-management"), any(Message.class));
    }

    // --- TEST 4 : DELETE ---
    @Test
    void deleteUser_ShouldReturn200_WhenDeleted() throws Exception {
        when(userService.deleteUser(123L)).thenReturn(true);

        mockMvc.perform(delete("/api/users/delete/123"))
                .andExpect(status().isOk())
                .andExpect(content().string("Utilisateur supprimé !"));
    }

    @Test
    void deleteUser_ShouldReturn404_WhenNotFound() throws Exception {
        when(userService.deleteUser(999L)).thenReturn(false);

        mockMvc.perform(delete("/api/users/delete/999"))
                .andExpect(status().isNotFound());
    }
}