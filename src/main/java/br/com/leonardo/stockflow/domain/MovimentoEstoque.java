package br.com.leonardo.stockflow.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "movimento_estoque")
public class MovimentoEstoque {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_pedido_id")
    private ItemPedido itemPedido;

    @Column(name = "unidade_medida", nullable = false, length = 10)
    private String unidadeMedida;

    @Column(name = "quantidade", nullable = false, precision = 15, scale = 3)
    private BigDecimal quantidade;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_movimento", nullable = false, length = 10)
    private TipoMovimento tipoMovimento;

    @Column(name = "observacao", columnDefinition = "text")
    private String observacao;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "created_by")
    private UUID createdBy;

    protected MovimentoEstoque() {
    }

    public MovimentoEstoque(Produto produto,
                            ItemPedido itemPedido,
                            BigDecimal quantidade,
                            TipoMovimento tipoMovimento,
                            String observacao) {
        this.id = UUID.randomUUID();
        this.produto = produto;
        this.itemPedido = itemPedido;
        this.unidadeMedida = produto.getUnidadeMedida();
        this.quantidade = quantidade;
        this.tipoMovimento = tipoMovimento;
        this.observacao = observacao;
        this.createdAt = Instant.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MovimentoEstoque outro)) {
            return false;
        }
        return id != null && id.equals(outro.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
