package com.sysdatec.tickets.mapper;

import com.sysdatec.tickets.dto.TicketCreateDTO;
import com.sysdatec.tickets.dto.TicketResponseDTO;
import com.sysdatec.tickets.model.Ticket;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class TicketMapper {

    // Transformación DTO -> Entidad mediante una función pura
    public Ticket toEntity(TicketCreateDTO dto) {
        return Optional.ofNullable(dto)
                .map(d -> {
                    Ticket ticket = new Ticket();
                    ticket.setCustomerName(d.getCustomerName());
                    ticket.setRequestText(d.getRequestText());
                    ticket.setAttachmentUrl(d.getAttachmentUrl());
                    return ticket;
                })
                .orElseThrow(() -> new IllegalArgumentException("El DTO de creación no puede ser nulo"));
    }

    // Transformación Entidad -> DTO mapeando directamente en el constructor del DTO
    public TicketResponseDTO toDto(Ticket entity) {
        return Optional.ofNullable(entity)
                .map(e -> new TicketResponseDTO(
                        e.getId(),
                        e.getCustomerName(),
                        e.getRequestText(),
                        e.getAttachmentUrl(),
                        e.getCategory(),
                        e.getPriority(),
                        e.getStatus(),
                        e.getSummary(),
                        e.getAssignedTo(),
                        e.getCreatedAt(),
                        e.getUpdatedAt(),
                        e.getComments()
                ))
                .orElse(null);
    }
}