package br.com.leonardo.stockflow.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "saldo_estoque")
public class SaldoEstoque {

    @Id
    @Column(name = "produto_id", nullable = false, updatable = false)
    private UUID produtoId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    @Column(name = "quantidade", nullable = false, precision = 15, scale = 3)
    private BigDecimal quantidade;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected SaldoEstoque() {
    }

    public SaldoEstoque(Produto produto, BigDecimal quantidadeInicial) {
        this.produto = produto;
        this.quantidade = quantidadeInicial;
        this.updatedAt = Instant.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SaldoEstoque outro)) {
            return false;
        }
        return produtoId != null && produtoId.equals(outro.produtoId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(produtoId);
    }
}
