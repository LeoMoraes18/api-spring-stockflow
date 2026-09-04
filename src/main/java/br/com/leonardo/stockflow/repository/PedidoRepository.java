package br.com.leonardo.stockflow.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import br.com.leonardo.stockflow.domain.Pedido;

public interface PedidoRepository extends JpaRepository<Pedido, UUID>{
    @Query(value = "SELECT nextval('pedido_numero_seq')", nativeQuery = true)
    Long proximoNumero();
}
