package com.ticketbooking.controller;

import com.ticketbooking.dto.response.ApiResponse;
import com.ticketbooking.dto.HelpQueryRequest;
import com.ticketbooking.service.EmailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/help")
@RequiredArgsConstructor
public class HelpController {

    private final EmailService emailService;

    @PostMapping("/query")
    public ResponseEntity<ApiResponse<String>> submitQuery(@Valid @RequestBody HelpQueryRequest request) {
        emailService.sendHelpQueryEmail(request.getEmail(), request.getQuery());
        return ResponseEntity.ok(ApiResponse.success("Query submitted successfully. We will get back to you soon.", null));
    }
}
