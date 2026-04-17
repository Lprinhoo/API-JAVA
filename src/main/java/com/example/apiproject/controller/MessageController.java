package com.example.apiproject.controller;

import com.example.apiproject.model.Message;
import com.example.apiproject.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/")
public class MessageController {

    @Autowired
    private MessageRepository messageRepository;

    @GetMapping
    public Map<String, String> healthCheck() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "online");
        response.put("message", "API Spring Boot no Railway conectada ao PostgreSQL!");
        return response;
    }

    @GetMapping("api/messages")
    public List<Message> getMessages() {
        return messageRepository.findAll();
    }

    @PostMapping("api/messages")
    public Message addMessage(@RequestBody Message message) {
        return messageRepository.save(message);
    }
}
