package com.rabbithole.usuarios.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioRequestDTO {
    
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El formato del email no es válido")
    private String email;
    
    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;
    
    private String apellido;
    
    @NotBlank(message = "El OID es obligatorio")
    private String oid;
    
    private String telefono;
    private String direccion;
    private String ciudad;
    private String estado;
    private String pais;
    private String codigoPostal;
}
