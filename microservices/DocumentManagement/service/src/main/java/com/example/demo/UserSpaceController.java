package com.example.demo.controllers;

import com.example.demo.services.DocumentService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.CrossOrigin;

// @CrossOrigin(origins = "http://localhost:5173")

@Controller
public class UserSpaceController {

    private final DocumentService documentService;

    public UserSpaceController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @GetMapping("/userspace")
    public String userSpace(Model model) {
        Long currentUserId = 1L; // TODO: récupérer l’ID depuis la session/login
        model.addAttribute("documents", documentService.getUserDocuments(currentUserId));
        return "redirect:/userspace.html"; // static/userspace.html
    }
}
