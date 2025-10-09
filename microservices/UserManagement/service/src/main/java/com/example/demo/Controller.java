package com.example.demo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Controller {

    @GetMapping("/addUser")
    public String addUser() {
        return "user added!";
    }
    @GetMapping("/deleteUser")
    public String deleteUser() {
        return "user deleted!";
    }
}
