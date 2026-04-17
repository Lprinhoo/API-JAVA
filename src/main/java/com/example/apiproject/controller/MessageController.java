package com.example.apiproject.controller;

import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/")
public class MessageController {

    private List<Map<String, String>> messages = new ArrayList<>();

    @GetMapping
    public Map<String, String> healthCheck() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "online");
        response.put("message", "API Spring Boot no Railway funcionando!");
        return response;
    }

    @GetMapping("api/messages")
    public List<Map<String, String>> getMessages() {
        return messages;
    }

    @PostMapping("api/messages")
    public Map<String, String> addMessage(@RequestBody Map<String, String> payload) {
        messages.add(payload);
        return payload;
    }
}
