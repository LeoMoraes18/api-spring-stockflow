package br.com.leonardo.stockflow.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record SaldoResponse(
    UUID produtoId,
    BigDecimal quantidade
) {
    
}
