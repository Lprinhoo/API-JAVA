package com.example.apiproject.controller;

import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private List<String> messages = new ArrayList<>();

    @GetMapping
    public List<String> getMessages() {
        return messages;
    }

    @PostMapping
    public String addMessage(@RequestBody String message) {
        messages.add(message);
        return "Mensagem adicionada com sucesso: " + message;
    }
}
