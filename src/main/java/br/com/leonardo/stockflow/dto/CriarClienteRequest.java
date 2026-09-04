package br.com.leonardo.stockflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record  CriarClienteRequest(

    @NotBlank(message = "nome é obrigatorio")
    @Size(max = 180, message = "nome deve ter no máximo 180 caracteres")
    String nome,

    @Size(max = 14, message = "documento deve ter no máximo 14 caracteres")
    String documento
) {
    
}
