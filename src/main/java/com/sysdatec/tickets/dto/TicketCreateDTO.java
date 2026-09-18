package com.sysdatec.tickets.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketCreateDTO {

    @NotBlank(message = "El nombre del cliente es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder los 100 caracteres")
    private String customerName;

    @NotBlank(message = "El texto de la solicitud es obligatorio")
    private String requestText;

    private String attachmentUrl;
}