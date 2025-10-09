package com.example.demo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Controller {

    @GetMapping("/createDoc")
    public String createDoc() {
        return "Document created!";
    }

    @GetMapping("/deleteDoc")
    public String deleteDoc() {
        return "Document deleted!";
    }

    @GetMapping("/getDoc")
    public String getDoc() {
        return "get Doc!";
    }

    @GetMapping("/updateDoc")
    public String updateDoc() {
        return "Document updated!";
    }
}
