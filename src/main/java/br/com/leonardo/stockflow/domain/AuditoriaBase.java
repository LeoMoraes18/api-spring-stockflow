package br.com.leonardo.stockflow.domain;


import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@MappedSuperclass
public abstract class AuditoriaBase {
    
    @Column(name="created_at", nullable=false)
    private Instant createdAt;

    @Column(name="created_by")
    private UUID createdBy;

    @Column(name="updated_at")
    private Instant updatedAt;

    @Column(name="updated_by")
    private UUID updatedBy;

    @Column(name="deleted_at")
    private Instant deletedAt;

    @Column(name="deleted_by")
    private UUID deletedBy;
}
