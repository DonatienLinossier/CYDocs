package com.example.demo;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class Controller implements ErrorController {

    @GetMapping("/addUser")
    public Map<String, Object> addUser() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "user added!");
        response.put("status", 200);
        return response;
    }

    @GetMapping("/deleteUser")
    public Map<String, Object> deleteUser() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "user deleted!");
        response.put("status", 200);
        return response;
    }

    @RequestMapping("/error")
    public Map<String, Object> handleError(HttpServletRequest request) {
        Object statusObj = request.getAttribute("jakarta.servlet.error.status_code");
        Map<String, Object> response = new HashMap<>();

        if (statusObj != null) {
            int statusCode = Integer.parseInt(statusObj.toString());
            if (statusCode == HttpStatus.NOT_FOUND.value()) {
                response.put("status", 404);
                response.put("error", "Not Found");
                response.put("message", "The requested path does not exist");
            } else {
                response.put("status", statusCode);
                response.put("error", "Error");
                response.put("message", "Something went wrong");
            }
        } else {
            response.put("status", 500);
            response.put("error", "Internal Server Error");
            response.put("message", "Unknown error");
        }

        String path = request.getRequestURI();
        response.put("path", path);
        
        return response;
    }

    public String getErrorPath() {
        return "/error";
    }
}
