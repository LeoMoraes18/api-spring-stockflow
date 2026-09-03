package br.com.leonardo.stockflow.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ProdutoResponse(
    UUID id,
    String nome,
    String descricao,
    String unidadeMedida,
    BigDecimal precoVenda,
    Instant createdAt
) {
    
}
