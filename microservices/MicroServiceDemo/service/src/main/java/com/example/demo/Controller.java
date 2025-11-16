package com.example.demo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
public class Controller {

    @GetMapping("/hello")
    public String sayHello() {
        return "👋 Hello, Microservice World!";
    }
}
