package com.sysdatec.tickets.service;

import com.sysdatec.tickets.dto.TicketCreateDTO;
import com.sysdatec.tickets.dto.TicketResponseDTO;
import com.sysdatec.tickets.mapper.TicketMapper;
import com.sysdatec.tickets.model.Comment;
import com.sysdatec.tickets.model.Ticket;
import com.sysdatec.tickets.repository.TicketRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final AiClassificationService aiClassificationService;
    private final TicketMapper ticketMapper;

    public TicketService(TicketRepository ticketRepository,
                         AiClassificationService aiClassificationService,
                         TicketMapper ticketMapper) {
        this.ticketRepository = ticketRepository;
        this.aiClassificationService = aiClassificationService;
        this.ticketMapper = ticketMapper;
    }

    // Pipeline funcional usando Stream API
    public List<TicketResponseDTO> findAll() {
        return ticketRepository.findAll().stream()
                .map(ticketMapper::toDto)
                .collect(Collectors.toList());
    }

    // Flujo funcional declarativo para la creación
    public TicketResponseDTO createTicket(TicketCreateDTO createDTO) {
        return Optional.ofNullable(createDTO)
                .map(ticketMapper::toEntity)
                .map(this::enrichWithAiData) // Enriquecemos la entidad con datos de la IA
                .map(ticketRepository::save)  // Persistimos
                .map(ticketMapper::toDto)    // Convertimos a DTO de salida
                .orElseThrow(() -> new IllegalArgumentException("Datos del ticket inválidos"));
    }

    // Función auxiliar pura para enriquecer la entidad
    private Ticket enrichWithAiData(Ticket ticket) {
        var aiResult = aiClassificationService.classifyAndSummarize(ticket.getRequestText());
        ticket.setStatus("NUEVO");
        ticket.setCategory(String.valueOf(aiResult.getOrDefault("category", "Soporte General")));
        ticket.setPriority(String.valueOf(aiResult.getOrDefault("priority", "MEDIA")));
        ticket.setSummary(String.valueOf(aiResult.getOrDefault("summary", "Sin resumen")));
        return ticket;
    }

    // Buscar por ID
    public TicketResponseDTO findById(UUID id) {
        return ticketRepository.findById(id)
                .map(ticketMapper::toDto)
                .orElseThrow(() -> new IllegalArgumentException("Ticket no encontrado con ID: " + id));
    }

    // Actualizar Estado
    public TicketResponseDTO updateStatus(UUID id, String status) {
        return ticketRepository.findById(id)
                .map(ticket -> {
                    ticket.setStatus(status);
                    return ticketRepository.save(ticket);
                })
                .map(ticketMapper::toDto)
                .orElseThrow(() -> new IllegalArgumentException("Ticket no encontrado con ID: " + id));
    }

    // Asignar Responsable (assignedTo)
    public TicketResponseDTO updateAssignedTo(UUID id, String assignedTo) {
        return ticketRepository.findById(id)
                .map(ticket -> {
                    ticket.setAssignedTo(assignedTo);
                    return ticketRepository.save(ticket);
                })
                .map(ticketMapper::toDto)
                .orElseThrow(() -> new IllegalArgumentException("Ticket no encontrado con ID: " + id));
    }

    // Agregar Comentario
    public TicketResponseDTO addComment(UUID id, Comment comment) {
        return ticketRepository.findById(id)
                .map(ticket -> {
                    comment.setTicket(ticket);
                    ticket.getComments().add(comment);
                    return ticketRepository.save(ticket);
                })
                .map(ticketMapper::toDto)
                .orElseThrow(() -> new IllegalArgumentException("Ticket no encontrado con ID: " + id));
    }

}