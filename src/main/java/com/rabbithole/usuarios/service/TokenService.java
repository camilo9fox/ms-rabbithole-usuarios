package com.rabbithole.usuarios.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbithole.usuarios.dto.MSALTokenDTO;
import com.rabbithole.usuarios.dto.TokenValidationResponseDTO;
import com.rabbithole.usuarios.dto.UsuarioDTO;
import com.rabbithole.usuarios.model.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Base64;

@Service
public class TokenService {

    @Autowired
    private UsuarioService usuarioService;
    
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Valida un token JWT de MSAL y extrae la información del usuario
     * 
     * @param token Token JWT de MSAL
     * @return Respuesta con la validación del token y datos del usuario
     */
    public TokenValidationResponseDTO validateToken(String token) {
        try {
            // Eliminar "Bearer " si está presente
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            
            // Dividir el token en sus partes (header, payload, signature)
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                return TokenValidationResponseDTO.invalid("Formato de token inválido");
            }
            
            // Decodificar el payload
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
            MSALTokenDTO msalToken = objectMapper.readValue(payload, MSALTokenDTO.class);
            
            // Verificar si el usuario existe en la base de datos
            Usuario usuario = usuarioService.findByOid(msalToken.getOid())
                    .orElseGet(() -> {
                        // Si no existe, crear un nuevo usuario con la información del token
                        Usuario nuevoUsuario = new Usuario();
                        nuevoUsuario.setOid(msalToken.getOid());
                        nuevoUsuario.setNombre(msalToken.getGiven_name());
                        nuevoUsuario.setApellido(msalToken.getFamily_name());
                        nuevoUsuario.setEmail(msalToken.getEmail());
                        nuevoUsuario.setCiudad(msalToken.getCity());
                        nuevoUsuario.setEstado(msalToken.getState());
                        nuevoUsuario.setPais(msalToken.getCountry());
                        nuevoUsuario.setDireccion(msalToken.getStreetAddress());
                        return usuarioService.save(nuevoUsuario);
                    });
            
            // Convertir a DTO y devolver respuesta
            UsuarioDTO usuarioDTO = convertToDTO(usuario);
            usuarioDTO.setAdmin(msalToken.isAdmin());
            
            return TokenValidationResponseDTO.valid(usuarioDTO, msalToken.isAdmin());
            
        } catch (Exception e) {
            return TokenValidationResponseDTO.invalid("Error al validar el token: " + e.getMessage());
        }
    }
    
    /**
     * Verifica si un usuario es administrador según el token
     * 
     * @param token Token JWT de MSAL
     * @return true si el usuario es administrador, false en caso contrario
     */
    public boolean isAdmin(String token) {
        try {
            // Eliminar "Bearer " si está presente
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            
            // Dividir el token en sus partes (header, payload, signature)
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                return false;
            }
            
            // Decodificar el payload
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
            MSALTokenDTO msalToken = objectMapper.readValue(payload, MSALTokenDTO.class);
            
            return msalToken.isAdmin();
            
        } catch (Exception e) {
            return false;
        }
    }
    
    private UsuarioDTO convertToDTO(Usuario usuario) {
        UsuarioDTO dto = new UsuarioDTO();
        dto.setId(usuario.getId());
        dto.setOid(usuario.getOid());
        dto.setNombre(usuario.getNombre());
        dto.setApellido(usuario.getApellido());
        dto.setEmail(usuario.getEmail());
        dto.setTelefono(usuario.getTelefono());
        dto.setDireccion(usuario.getDireccion());
        dto.setCiudad(usuario.getCiudad());
        dto.setEstado(usuario.getEstado());
        dto.setPais(usuario.getPais());
        dto.setCodigoPostal(usuario.getCodigoPostal());
        return dto;
    }
}
