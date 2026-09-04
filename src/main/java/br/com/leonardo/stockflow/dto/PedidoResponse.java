package br.com.leonardo.stockflow.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import br.com.leonardo.stockflow.domain.StatusPedido;

public record PedidoResponse(
    UUID id,
    String numeroPedido,
    UUID clienteId,
    String clienteNome,
    Instant data,
    StatusPedido status,
    BigDecimal valorTotal,
    List<ItemResponse> itens
) {
    public record ItemResponse(
        UUID id,
        UUID produtoId,
        String produtoNome,
        String unidadeMedida,
        BigDecimal quantidade,
        BigDecimal valorUnitario,
        BigDecimal valorDesconto,
        BigDecimal valorAcrescimo,
        BigDecimal valorTotal
    ) {
    }

}