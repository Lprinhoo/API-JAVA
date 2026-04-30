package com.example.apiproject.controller;

import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/")
public class ApiController {

    @GetMapping
    public Map<String, String> healthCheck() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "online");
        response.put("message", "API Spring Boot no Railway funcionando!");
        return response;
    }
}
