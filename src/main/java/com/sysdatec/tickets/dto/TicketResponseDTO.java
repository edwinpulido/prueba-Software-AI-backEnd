package com.sysdatec.tickets.dto;

import com.sysdatec.tickets.model.Comment;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketResponseDTO {

    private UUID id;
    private String customerName;
    private String requestText;
    private String attachmentUrl;
    private String category;
    private String priority;
    private String status;
    private String summary;
    private String assignedTo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<Comment> comments;
}