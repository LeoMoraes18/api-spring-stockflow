package br.com.leonardo.stockflow.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "produto")
public class Produto extends AuditoriaBase {
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "nome", nullable = false, length = 180)
    private String nome;

    @Column(name = "descricao", columnDefinition = "text")
    private String descricao;

    @Column(name = "unidade_medida", nullable = false, length = 10)
    private String unidadeMedida;

    @Column(name = "preco_venda", nullable = false, precision = 15, scale = 2)
    private BigDecimal precoVenda;

    @Column(name = "preco_compra", precision = 15, scale = 2)
    private BigDecimal precoCompra;

    protected Produto() {}

    public Produto(String nome, String unidadeMedida, BigDecimal precoVenda) {
        this.id = UUID.randomUUID();
        this.nome = nome;
        this.unidadeMedida = unidadeMedida;
        this.precoVenda = precoVenda;
        setCreatedAt(Instant.now());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Produto outro)) {
            return false;
        }
        return id != null && id.equals(outro.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
