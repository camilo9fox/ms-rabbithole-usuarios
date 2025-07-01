package com.rabbithole.usuarios.service;

import com.rabbithole.usuarios.dto.TokenValidationResponseDTO;
import com.rabbithole.usuarios.dto.UsuarioDTO;
import com.rabbithole.usuarios.model.Usuario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class MSALService {

    private static final Logger logger = LoggerFactory.getLogger(MSALService.class);

    // Estos valores son utilizados por AudienceValidator
    private final String clientId;
    private final String tenantId;
    private final String clientSecret;
    private final UsuarioService usuarioService;
    private final JwtDecoder jwtDecoder;
    
    public MSALService(
            @Value("${azure.activedirectory.client-id}") String clientId,
            @Value("${azure.activedirectory.tenant-id}") String tenantId,
            @Value("${azure.activedirectory.client-secret}") String clientSecret,
            UsuarioService usuarioService,
            JwtDecoder jwtDecoder) {
        this.clientId = clientId;
        this.tenantId = tenantId;
        this.clientSecret = clientSecret;
        this.usuarioService = usuarioService;
        this.jwtDecoder = jwtDecoder;
    }

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
            
            logger.info("Intentando decodificar token JWT con client-id: {} y tenant-id: {}", clientId, tenantId);
            logger.debug("Client secret está configurado: {}", clientSecret != null && !clientSecret.isEmpty() ? "Sí" : "No");
            
            // Decodificar y validar el token JWT
            Jwt jwt = jwtDecoder.decode(token);
            
            // Extraer información del token
            String oid = jwt.getClaim("oid");
            String resolvedEmail = jwt.getClaim("preferred_username");
            if (resolvedEmail == null || resolvedEmail.isEmpty()) {
                // Buscar en claims alternativos
                if (jwt.hasClaim("emails")) {
                    Object emailsClaim = jwt.getClaim("emails");
                    if (emailsClaim instanceof java.util.List<?> emailsList && !emailsList.isEmpty()) {
                        Object first = emailsList.get(0);
                        if (first != null) resolvedEmail = first.toString();
                    }
                }
                if ((resolvedEmail == null || resolvedEmail.isEmpty()) && jwt.hasClaim("email")) {
                    resolvedEmail = jwt.getClaim("email");
                }
            }
            // Si sigue siendo null, lanza excepción clara
            if (resolvedEmail == null || resolvedEmail.isEmpty()) {
                throw new IllegalArgumentException("El token MSAL no contiene ningún email válido en los claims.");
            }
            final String email = resolvedEmail;
            String name = jwt.getClaim("name");
            String givenName = jwt.getClaim("given_name");
            String familyName = jwt.getClaim("family_name");
            
            logger.info("Token decodificado correctamente. OID: {}, Email: {}", oid, email);
            
            // Verificar si el usuario existe en la base de datos
            Usuario usuario = usuarioService.findByOid(oid)
                    .orElseGet(() -> {
                        // Si no existe, crear un nuevo usuario con la información del token
                        Usuario nuevoUsuario = new Usuario();
                        nuevoUsuario.setOid(oid);
                        nuevoUsuario.setNombre(givenName != null ? givenName : name);
                        nuevoUsuario.setApellido(familyName);
                        nuevoUsuario.setEmail(email);
                        
                        // Otros campos opcionales si están disponibles en el token
                        Map<String, Object> claims = jwt.getClaims();
                        if (claims.containsKey("country")) nuevoUsuario.setPais((String) claims.get("country"));
                        if (claims.containsKey("city")) nuevoUsuario.setCiudad((String) claims.get("city"));
                        if (claims.containsKey("state")) nuevoUsuario.setEstado((String) claims.get("state"));
                        if (claims.containsKey("streetAddress")) nuevoUsuario.setDireccion((String) claims.get("streetAddress"));
                        
                        return usuarioService.save(nuevoUsuario);
                    });
            
            // Verificar rol de administrador solo por claims del token
            boolean isAdmin = hasAdminRole(jwt);
            
            // Convertir a DTO y devolver respuesta
            UsuarioDTO usuarioDTO = convertToDTO(usuario);
            // El campo admin del DTO solo se setea aquí, nunca se guarda en la base de datos
            if (usuarioDTO != null) {
                usuarioDTO.setAdmin(isAdmin);
            }
            
            return TokenValidationResponseDTO.valid(usuarioDTO, isAdmin);
            
        } catch (Exception e) {
            logger.error("Error al validar el token: {}", e.getMessage(), e);
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
            
            // Decodificar y validar el token JWT
            Jwt jwt = jwtDecoder.decode(token);
            
            return hasAdminRole(jwt);
            
        } catch (Exception e) {
            logger.error("Error al verificar rol de administrador: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Verifica si el usuario tiene rol de administrador según los claims del token
     * 
     * @param jwt Token JWT decodificado
     * @return true si el usuario es administrador, false en caso contrario
     */
    private boolean hasAdminRole(Jwt jwt) {
        // Log para depuración usando clientId y tenantId
        logger.debug("Verificando roles de administrador para token en tenant {} con audiencia {}", tenantId, clientId);
        // Verificar si el token contiene roles y si incluye el rol de administrador
        try {
            // Primero intentamos buscar en el claim "roles"
            if (jwt.hasClaim("roles")) {
                return jwt.getClaimAsStringList("roles").contains("Admin");
            }
            
            // Si no hay claim "roles", verificamos si hay un claim "jobTitle" con valor "Admin"
            if (jwt.hasClaim("jobTitle")) {
                return "Admin".equals(jwt.getClaim("jobTitle"));
            }
            
            return false;
        } catch (Exception e) {
            logger.warn("Error al verificar roles en el token: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Convierte un objeto Usuario a UsuarioDTO
     * 
     * @param usuario Objeto Usuario a convertir
     * @return UsuarioDTO con los datos del usuario
     */
    private UsuarioDTO convertToDTO(Usuario usuario) {
        if (usuario == null) {
            return null;
        }
        
        UsuarioDTO dto = new UsuarioDTO();
        dto.setId(usuario.getId());
        dto.setNombre(usuario.getNombre());
        dto.setApellido(usuario.getApellido());
        dto.setEmail(usuario.getEmail());
        dto.setOid(usuario.getOid());
        dto.setDireccion(usuario.getDireccion());
        dto.setCiudad(usuario.getCiudad());
        dto.setEstado(usuario.getEstado());
        dto.setPais(usuario.getPais());
        dto.setTelefono(usuario.getTelefono());
        dto.setCodigoPostal(usuario.getCodigoPostal());
        // El campo admin se determina por el token, no por la entidad Usuario
        dto.setAdmin(false); // Se establecerá después según el token
        
        return dto;
    }
}
