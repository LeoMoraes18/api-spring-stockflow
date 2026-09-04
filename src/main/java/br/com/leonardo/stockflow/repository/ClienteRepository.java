package br.com.leonardo.stockflow.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.leonardo.stockflow.domain.Cliente;


public interface ClienteRepository extends JpaRepository<Cliente, UUID> {

    boolean existsByDocumentoAndDeletedAtIsNull(String documento);
}