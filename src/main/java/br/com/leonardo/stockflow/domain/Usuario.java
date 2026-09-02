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
@Table(name = "usuario")
public class Usuario {
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "nome", nullable = false, length = 120)
    private String nome;

    @Column(name = "email", nullable = false, unique = true, length = 180)
    private String email;

    @Column(name = "senha_hash", nullable = false, length = 60)
    private String senhaHash;

    @Column(name = "ultimo_login", nullable = true)
    private Instant ultimoLogin;

    @Column(name = "ativo", nullable = false)
    private Boolean ativo;

    @Column(name = "created_at", nullable =  false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    protected Usuario() {}

    public Usuario(String nome, String email, String senhaHash){
        this.id = UUID.randomUUID();
        this.nome = nome;
        this.email = email;
        this.senhaHash = senhaHash;
        setCreatedAt(Instant.now());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Usuario outro)) {
            return false;
        }
        return id != null && id.equals(outro.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
