package br.com.leonardo.stockflow.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.leonardo.stockflow.domain.SaldoEstoque;

public interface SaldoEstoqueRepository extends JpaRepository<SaldoEstoque, UUID>{
    
}
