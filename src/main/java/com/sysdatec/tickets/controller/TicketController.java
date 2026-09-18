package com.sysdatec.tickets.controller;

import com.sysdatec.tickets.dto.TicketCreateDTO;
import com.sysdatec.tickets.dto.TicketResponseDTO;
import com.sysdatec.tickets.model.Comment;
import com.sysdatec.tickets.service.TicketService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @GetMapping
    public ResponseEntity<List<TicketResponseDTO>> getAllTickets() {
        return ResponseEntity.ok(ticketService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TicketResponseDTO> getTicketById(@PathVariable UUID id) {
        return ResponseEntity.ok(ticketService.findById(id));
    }

    @PostMapping
    public ResponseEntity<TicketResponseDTO> createTicket(@Valid @RequestBody TicketCreateDTO createDTO) {
        TicketResponseDTO createdTicket = ticketService.createTicket(createDTO);
        return new ResponseEntity<>(createdTicket, HttpStatus.CREATED);
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<TicketResponseDTO> addComment(
            @PathVariable UUID id,
            @Valid @RequestBody Comment comment) {
        return ResponseEntity.ok(ticketService.addComment(id, comment));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<TicketResponseDTO> updateStatus(
            @PathVariable UUID id,
            @RequestBody String status) {
        return ResponseEntity.ok(ticketService.updateStatus(id, status));
    }

    @PatchMapping("/{id}/assigned-to")
    public ResponseEntity<TicketResponseDTO> updateAssignedTo(
            @PathVariable UUID id,
            @RequestBody String assignedTo) {
        return ResponseEntity.ok(ticketService.updateAssignedTo(id, assignedTo));
    }

}