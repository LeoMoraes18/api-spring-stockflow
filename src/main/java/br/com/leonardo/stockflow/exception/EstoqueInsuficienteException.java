package br.com.leonardo.stockflow.exception;

import java.math.BigDecimal;
import java.util.UUID;

public class EstoqueInsuficienteException extends RuntimeException {

    private final UUID produtoId;
    private final BigDecimal disponivel;
    private final BigDecimal solicitado;

    public EstoqueInsuficienteException(UUID produtoId, BigDecimal disponivel, BigDecimal solicitado) {
        super("Estoque insuficiente para o produto %s: disponivel %s, solicitado %s"
                .formatted(produtoId, disponivel, solicitado));
        this.produtoId = produtoId;
        this.disponivel = disponivel;
        this.solicitado = solicitado;
    }

    public UUID getProdutoId() {
        return produtoId;
    }

    public BigDecimal getDisponivel() {
        return disponivel;
    }

    public BigDecimal getSolicitado() {
        return solicitado;
    }
}
