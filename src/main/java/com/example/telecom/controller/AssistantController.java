package com.example.telecom.controller;

import com.example.telecom.dto.AssistantChatRequestDTO;
import com.example.telecom.dto.AssistantChatResponseDTO;
import com.example.telecom.service.AssistantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/assistant")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AssistantController {

    private final AssistantService assistantService;

    @PostMapping("/chat")
    public ResponseEntity<AssistantChatResponseDTO> chat(@RequestBody AssistantChatRequestDTO request) {
        return ResponseEntity.ok(assistantService.chat(request));
    }
}
