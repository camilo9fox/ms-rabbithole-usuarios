package com.rabbithole.usuarios.repository;

import com.rabbithole.usuarios.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    
    Optional<Usuario> findByOid(String oid);
    
    Optional<Usuario> findByEmail(String email);
    
    boolean existsByOid(String oid);
    
    boolean existsByEmail(String email);
}
