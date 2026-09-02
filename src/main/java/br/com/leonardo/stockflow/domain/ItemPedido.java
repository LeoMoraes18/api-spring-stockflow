package br.com.leonardo.stockflow.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "item_pedido")
public class ItemPedido extends AuditoriaBase {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pedido_id", nullable = false)
    private Pedido pedido;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    @Column(name = "unidade_medida", nullable = false, length = 10)
    private String unidadeMedida;

    @Column(name = "quantidade", nullable = false, precision = 15, scale = 3)
    private BigDecimal quantidade;

    @Column(name = "valor_unitario", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorUnitario;

    @Column(name = "valor_desconto", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorDesconto;

    @Column(name = "valor_acrescimo", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorAcrescimo;

    @Column(name = "valor_total", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorTotal;

    protected ItemPedido() {
    }


    public ItemPedido(Produto produto,
                      BigDecimal quantidade,
                      BigDecimal valorDesconto,
                      BigDecimal valorAcrescimo) {
        this.id = UUID.randomUUID();
        this.produto = produto;
        this.unidadeMedida = produto.getUnidadeMedida();
        this.valorUnitario = produto.getPrecoVenda();
        this.quantidade = quantidade;
        this.valorDesconto = valorDesconto == null ? BigDecimal.ZERO : valorDesconto;
        this.valorAcrescimo = valorAcrescimo == null ? BigDecimal.ZERO : valorAcrescimo;
        this.valorTotal = calcularTotal();
        setCreatedAt(Instant.now());
    }

    private BigDecimal calcularTotal() {
        return quantidade.multiply(valorUnitario)
                .subtract(valorDesconto)
                .add(valorAcrescimo)
                .setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ItemPedido outro)) {
            return false;
        }
        return id != null && id.equals(outro.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
