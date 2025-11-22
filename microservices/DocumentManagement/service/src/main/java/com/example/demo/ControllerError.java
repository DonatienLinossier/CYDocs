package com.example.demo.controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
public class ControllerError implements ErrorController {

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
                response.put("path", request.getRequestURI());
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
