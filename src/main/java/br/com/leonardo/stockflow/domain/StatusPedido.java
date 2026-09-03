package br.com.leonardo.stockflow.domain;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public enum StatusPedido {

    ABERTO,
    CONFIRMADO,
    PAGO,
    SEPARADO,
    ENVIADO,
    ENTREGUE,
    CANCELADO;

    private static final Map<StatusPedido, Set<StatusPedido>> TRANSICOES =
            new EnumMap<>(StatusPedido.class);

    static {
        TRANSICOES.put(ABERTO,     EnumSet.of(CONFIRMADO, CANCELADO));
        TRANSICOES.put(CONFIRMADO, EnumSet.of(PAGO, CANCELADO));
        TRANSICOES.put(PAGO,       EnumSet.of(SEPARADO, CANCELADO));
        TRANSICOES.put(SEPARADO,   EnumSet.of(ENVIADO, CANCELADO));
        TRANSICOES.put(ENVIADO,    EnumSet.of(ENTREGUE));
        TRANSICOES.put(ENTREGUE,   EnumSet.noneOf(StatusPedido.class));
        TRANSICOES.put(CANCELADO,  EnumSet.noneOf(StatusPedido.class));
    }

    public Set<StatusPedido> proximosPermitidos() {
        return TRANSICOES.getOrDefault(this, EnumSet.noneOf(StatusPedido.class));
    }

    public boolean podeIrPara(StatusPedido novo) {
        return proximosPermitidos().contains(novo);
    }

    /** Status terminal: nao admite nenhuma transicao de saida. */
    public boolean isFinal() {
        return proximosPermitidos().isEmpty();
    }
}
