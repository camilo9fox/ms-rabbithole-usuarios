package com.rabbithole.usuarios.controller;

import com.rabbithole.usuarios.dto.TokenValidationResponseDTO;
import com.rabbithole.usuarios.dto.UsuarioDTO;
import com.rabbithole.usuarios.dto.UsuarioRequestDTO;
import com.rabbithole.usuarios.model.Usuario;
import com.rabbithole.usuarios.service.MSALService;
import com.rabbithole.usuarios.service.UsuarioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/usuarios")
@CrossOrigin(origins = "*")
public class UsuarioController {

    private static final Logger logger = LoggerFactory.getLogger(UsuarioController.class);

    private final UsuarioService usuarioService;
    private final MSALService msalService;

    public UsuarioController(UsuarioService usuarioService, MSALService msalService) {
        this.usuarioService = usuarioService;
        this.msalService = msalService;
    }

    /**
     * Obtiene todos los usuarios
     * 
     * @return Lista de usuarios
     */
    @GetMapping
    public ResponseEntity<List<UsuarioDTO>> getAllUsuarios() {
        return ResponseEntity.ok(usuarioService.findAll());
    }

    /**
     * Obtiene un usuario por su ID
     * 
     * @param id ID del usuario
     * @return Usuario encontrado
     */
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioDTO> getUsuarioById(@PathVariable Long id) {
        Optional<Usuario> usuario = usuarioService.findById(id);
        return usuario.map(u -> ResponseEntity.ok(convertToDTO(u)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Obtiene un usuario por su OID de MSAL
     * 
     * @param oid OID del usuario
     * @return Usuario encontrado
     */
    @GetMapping("/oid/{oid}")
    public ResponseEntity<UsuarioDTO> getUsuarioByOid(@PathVariable String oid) {
        Optional<Usuario> usuario = usuarioService.findByOid(oid);
        return usuario.map(u -> ResponseEntity.ok(convertToDTO(u)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Crea un nuevo usuario
     * 
     * @param usuarioRequestDTO Datos del usuario a crear
     * @return Usuario creado
     */
    @PostMapping
    public ResponseEntity<Object> createUsuario(@Valid @RequestBody UsuarioRequestDTO usuarioRequestDTO) {
        try {
            UsuarioDTO createdUsuario = usuarioService.create(usuarioRequestDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdUsuario);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Actualiza un usuario existente
     * 
     * @param id                ID del usuario a actualizar
     * @param usuarioRequestDTO Datos actualizados del usuario
     * @return Usuario actualizado
     */
    @PutMapping("/{id}")
    public ResponseEntity<Object> updateUsuario(@PathVariable Long id,
            @Valid @RequestBody UsuarioRequestDTO usuarioRequestDTO) {
        try {
            UsuarioDTO updatedUsuario = usuarioService.update(id, usuarioRequestDTO);
            return ResponseEntity.ok(updatedUsuario);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Elimina un usuario por su ID
     * 
     * @param id ID del usuario a eliminar
     * @return Respuesta sin contenido
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteUsuario(@PathVariable Long id) {
        try {
            usuarioService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Valida un token JWT de MSAL y extrae la información del usuario
     * 
     * @param token Token JWT de MSAL en el cuerpo de la solicitud
     * @return Respuesta con la validación del token y datos del usuario
     */
    private static final String TOKEN_FIELD_NAME = "token";

    @PostMapping("/validate-token")
    public ResponseEntity<TokenValidationResponseDTO> validateToken(@RequestBody String rawToken) {
        try {
            logger.info("Recibida solicitud para validar token");
            
            // Procesar y limpiar el token recibido
            String processedToken = processRawToken(rawToken);
            
            // Validar el token procesado
            TokenValidationResponseDTO response = msalService.validateToken(processedToken);
            logger.info("Token validado correctamente");
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception e) {
            logger.error("Error al validar el token: {}", e.getMessage(), e);
            // Usar el método estático para crear una respuesta inválida
            TokenValidationResponseDTO errorResponse = TokenValidationResponseDTO.invalid(
                    "Error al validar el token: " + e.getMessage());
            // Devolver 200 OK con respuesta de error para facilitar depuración
            // En producción, esto debería ser un 401 UNAUTHORIZED
            return ResponseEntity.status(HttpStatus.OK).body(errorResponse);
        }
    }
    
    /**
     * Procesa un token en bruto para extraerlo de un objeto JSON o eliminar comillas
     * 
     * @param rawToken Token en bruto recibido en la solicitud
     * @return Token procesado listo para validación
     */
    private String processRawToken(String rawToken) {
        String processedToken = rawToken;
        
        // Verificar si el token está en formato JSON y extraerlo si es necesario
        if (processedToken.startsWith("{") && processedToken.contains(TOKEN_FIELD_NAME)) {
            try {
                // Intentar extraer el token de un objeto JSON
                ObjectMapper mapper = new ObjectMapper();
                JsonNode jsonNode = mapper.readTree(processedToken);
                if (jsonNode.has(TOKEN_FIELD_NAME)) {
                    processedToken = jsonNode.get(TOKEN_FIELD_NAME).asText();
                    logger.info("Token extraído de objeto JSON");
                }
            } catch (Exception e) {
                logger.warn("No se pudo parsear el JSON: {}", e.getMessage());
                // Continuar con el token original
            }
        }
        
        // Eliminar comillas si el token está entre comillas
        processedToken = processedToken.trim();
        if (processedToken.startsWith("\"") && processedToken.endsWith("\"")) {
            processedToken = processedToken.substring(1, processedToken.length() - 1);
            logger.info("Se eliminaron comillas del token");
        }
        
        return processedToken;
    }

    /**
     * Verifica si un usuario es administrador según el token
     * 
     * @param token Token JWT de MSAL en el encabezado Authorization
     * @return true si el usuario es administrador, false en caso contrario
     */
    @GetMapping("/is-admin")
    public ResponseEntity<Boolean> isAdmin(@RequestHeader("Authorization") String token) {
        boolean isAdmin = msalService.isAdmin(token);
        return ResponseEntity.ok(isAdmin);
    }

    /**
     * Verifica si existe un usuario con el email proporcionado
     * 
     * @param email Email a verificar
     * @return true si existe un usuario con ese email, false en caso contrario
     */
    @GetMapping("/exists/email/{email}")
    public ResponseEntity<Boolean> existsByEmail(@PathVariable String email) {
        boolean exists = usuarioService.findByEmail(email).isPresent();
        return ResponseEntity.ok(exists);
    }

    /**
     * Convierte una entidad Usuario a DTO
     * 
     * @param usuario Entidad Usuario
     * @return DTO del usuario
     */
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
