package br.com.leonardo.stockflow.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record CriarPedidoRequest(

    @NotNull(message = "cliente é obrigatorio")
    UUID clienteId,

    @NotEmpty(message = "pedido deve ter ao menos um item")
    @Valid
    List<ItemPedidoRequest> itens
) {
    public record ItemPedidoRequest(

        @NotNull(message = "produto é obrigatório")
        UUID produtoId,

        @NotNull(message = "quantidade é obrigatória")
        @Positive(message = "quantidade deve ser maior que zero")
        BigDecimal quantidade,

        @PositiveOrZero(message = "desconto não poder ser negativo")
        BigDecimal valorDesconto,

        @PositiveOrZero(message = "acréscimo não pode ser negativo")
        BigDecimal valorAcrescimo
    ) {

    }
}
