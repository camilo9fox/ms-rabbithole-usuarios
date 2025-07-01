package com.rabbithole.usuarios.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioDTO {
    private Long id;
    private String email;
    private String nombre;
    private String apellido;
    private String oid;
    private String telefono;
    private String direccion;
    private String ciudad;
    private String estado;
    private String pais;
    private String codigoPostal;
    private boolean isAdmin;
}
