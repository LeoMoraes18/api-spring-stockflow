package br.com.leonardo.stockflow.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.leonardo.stockflow.domain.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, UUID>{
    
}
