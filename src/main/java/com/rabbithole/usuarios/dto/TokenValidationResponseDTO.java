package com.rabbithole.usuarios.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TokenValidationResponseDTO {
    private boolean valid;
    private boolean isAdmin;
    private UsuarioDTO usuario;
    private String message;
    
    public static TokenValidationResponseDTO invalid(String message) {
        return new TokenValidationResponseDTO(false, false, null, message);
    }
    
    public static TokenValidationResponseDTO valid(UsuarioDTO usuario, boolean isAdmin) {
        return new TokenValidationResponseDTO(true, isAdmin, usuario, "Token válido");
    }
}
