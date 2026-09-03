package br.com.leonardo.stockflow.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record CriarProdutoRequest(

    @NotBlank(message = "nome é obrigatorio")
    @Size(max = 180, message = "nome deve ter no máximo 180 caractes")
    String nome,

    String descricao,

    @NotBlank(message = "unidade de medida é obrigatoria")
    @Size(max = 10, message = "unidade de medida deve ter no máximo 10 caracteres")
    String unidadeMedida,

    @NotNull(message = "preço de venda é obrigatório")
    @PositiveOrZero(message = "preço de venda não pode ser negativo")
    BigDecimal precoVenda,

    @PositiveOrZero(message = "preço de compra não pode ser negativo")
    BigDecimal precoCompra
) {

}
