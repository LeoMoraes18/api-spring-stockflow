package br.com.leonardo.stockflow.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record RegistrarEntradaRequest(

    @NotNull(message = "A quantidade é obrigatorio")
    @Positive(message = "A quantidade tem que ser maior que 0")
    BigDecimal quantidade,

    String observacao
) {
    
}
