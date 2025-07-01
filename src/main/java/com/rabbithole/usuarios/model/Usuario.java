package com.rabbithole.usuarios.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "usuarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String email;
    
    @Column(nullable = false)
    private String nombre;
    
    private String apellido;
    
    @Column(nullable = false, unique = true)
    private String oid;
    
    private String telefono;
    private String direccion;
    private String ciudad;
    private String estado;
    private String pais;
    
    @Column(name = "codigo_postal")
    private String codigoPostal;
}
