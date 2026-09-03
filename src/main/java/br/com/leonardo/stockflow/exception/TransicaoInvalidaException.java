package br.com.leonardo.stockflow.exception;

import br.com.leonardo.stockflow.domain.StatusPedido;

public class TransicaoInvalidaException extends RuntimeException {

    private final StatusPedido atual;
    private final StatusPedido pretendido;

    public TransicaoInvalidaException(StatusPedido atual, StatusPedido pretendido) {
        super("Transicao invalida: pedido em %s nao pode ir para %s".formatted(atual, pretendido));
        this.atual = atual;
        this.pretendido = pretendido;
    }

    public StatusPedido getAtual() {
        return atual;
    }

    public StatusPedido getPretendido() {
        return pretendido;
    }
}
