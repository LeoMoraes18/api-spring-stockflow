package br.com.leonardo.stockflow.domain;

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
@Table(name = "cliente")
public class Cliente extends AuditoriaBase{
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "nome", nullable = false, length = 180)
    private String nome;

    @Column(name = "documento", length = 14)
    private String documento;

    protected Cliente() {}

    public Cliente(String nome, String documento) {
        this.id = UUID.randomUUID();
        this.nome = nome;
        this.documento = documento;

        setCreatedAt(Instant.now());
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Cliente outro)) {
            return false;
        }
        return id != null && id.equals(outro.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
