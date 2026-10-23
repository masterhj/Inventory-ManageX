package br.com.system.controllers;

import br.com.system.data.dto.request.AssistantRequestDTO;
import br.com.system.data.dto.response.AssistantResponseDTO;
import br.com.system.services.AssistantService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/assistant")
public class AssistantController {
    private final AssistantService service;

    public AssistantController(AssistantService service) {
        this.service = service;
    }

    @PostMapping("/chat")
    public ResponseEntity<AssistantResponseDTO> chat(@RequestBody @Valid AssistantRequestDTO request) {
        return ResponseEntity.ok(service.chat(request));
    }
}
