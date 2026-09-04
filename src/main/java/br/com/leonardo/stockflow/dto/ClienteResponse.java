package br.com.leonardo.stockflow.dto;

import java.util.UUID;

public record ClienteResponse(
    UUID id,
    String nome,
    String documento
) {

}