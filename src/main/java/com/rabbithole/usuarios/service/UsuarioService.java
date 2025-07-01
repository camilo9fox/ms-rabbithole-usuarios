package com.rabbithole.usuarios.service;

import com.rabbithole.usuarios.dto.UsuarioDTO;
import com.rabbithole.usuarios.dto.UsuarioRequestDTO;
import com.rabbithole.usuarios.model.Usuario;
import com.rabbithole.usuarios.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * Obtiene todos los usuarios
     * 
     * @return Lista de usuarios
     */
    public List<UsuarioDTO> findAll() {
        return usuarioRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Busca un usuario por su ID
     * 
     * @param id ID del usuario
     * @return Usuario encontrado o vacío
     */
    public Optional<Usuario> findById(Long id) {
        return usuarioRepository.findById(id);
    }

    /**
     * Busca un usuario por su OID de MSAL
     * 
     * @param oid OID del usuario
     * @return Usuario encontrado o vacío
     */
    public Optional<Usuario> findByOid(String oid) {
        return usuarioRepository.findByOid(oid);
    }

    /**
     * Busca un usuario por su email
     * 
     * @param email Email del usuario
     * @return Usuario encontrado o vacío
     */
    public Optional<Usuario> findByEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    /**
     * Guarda un usuario
     * 
     * @param usuario Usuario a guardar
     * @return Usuario guardado
     */
    public Usuario save(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }

    /**
     * Crea un nuevo usuario
     * 
     * @param usuarioRequestDTO Datos del usuario a crear
     * @return Usuario creado
     * @throws IllegalArgumentException si el email o el OID ya existen
     */
    public UsuarioDTO create(UsuarioRequestDTO usuarioRequestDTO) {
        // Verificar si el email ya existe
        if (usuarioRepository.existsByEmail(usuarioRequestDTO.getEmail())) {
            throw new IllegalArgumentException("El email ya está registrado");
        }

        // Verificar si el OID ya existe
        if (usuarioRepository.existsByOid(usuarioRequestDTO.getOid())) {
            throw new IllegalArgumentException("El OID ya está registrado");
        }

        // Crear nuevo usuario
        Usuario usuario = new Usuario();
        usuario.setEmail(usuarioRequestDTO.getEmail());
        usuario.setNombre(usuarioRequestDTO.getNombre());
        usuario.setApellido(usuarioRequestDTO.getApellido());
        usuario.setOid(usuarioRequestDTO.getOid());
        usuario.setTelefono(usuarioRequestDTO.getTelefono());
        usuario.setDireccion(usuarioRequestDTO.getDireccion());
        usuario.setCiudad(usuarioRequestDTO.getCiudad());
        usuario.setEstado(usuarioRequestDTO.getEstado());
        usuario.setPais(usuarioRequestDTO.getPais());
        usuario.setCodigoPostal(usuarioRequestDTO.getCodigoPostal());

        return convertToDTO(usuarioRepository.save(usuario));
    }

    /**
     * Actualiza un usuario existente
     * 
     * @param id ID del usuario a actualizar
     * @param usuarioRequestDTO Datos actualizados del usuario
     * @return Usuario actualizado
     * @throws IllegalArgumentException si el usuario no existe o si el email ya está en uso por otro usuario
     */
    public UsuarioDTO update(Long id, UsuarioRequestDTO usuarioRequestDTO) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + id));

        // Verificar si el email ya está en uso por otro usuario
        if (!usuario.getEmail().equals(usuarioRequestDTO.getEmail()) && 
                usuarioRepository.existsByEmail(usuarioRequestDTO.getEmail())) {
            throw new IllegalArgumentException("El email ya está registrado por otro usuario");
        }

        // Actualizar datos del usuario
        usuario.setEmail(usuarioRequestDTO.getEmail());
        usuario.setNombre(usuarioRequestDTO.getNombre());
        usuario.setApellido(usuarioRequestDTO.getApellido());
        usuario.setTelefono(usuarioRequestDTO.getTelefono());
        usuario.setDireccion(usuarioRequestDTO.getDireccion());
        usuario.setCiudad(usuarioRequestDTO.getCiudad());
        usuario.setEstado(usuarioRequestDTO.getEstado());
        usuario.setPais(usuarioRequestDTO.getPais());
        usuario.setCodigoPostal(usuarioRequestDTO.getCodigoPostal());

        return convertToDTO(usuarioRepository.save(usuario));
    }

    /**
     * Elimina un usuario por su ID
     * 
     * @param id ID del usuario a eliminar
     * @throws IllegalArgumentException si el usuario no existe
     */
    public void delete(Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new IllegalArgumentException("Usuario no encontrado con ID: " + id);
        }
        usuarioRepository.deleteById(id);
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
