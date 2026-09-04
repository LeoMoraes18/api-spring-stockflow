package br.com.leonardo.stockflow.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import br.com.leonardo.stockflow.dto.RegistrarEntradaRequest;
import br.com.leonardo.stockflow.dto.SaldoResponse;
import br.com.leonardo.stockflow.service.EstoqueService;
import jakarta.validation.Valid;

@RestController
public class EstoqueController {

    private final EstoqueService estoqueService;

    public EstoqueController(EstoqueService estoqueService) {
        this.estoqueService = estoqueService;
    }

    @PostMapping("/produtos/{produtoId}/estoque/entradas")
    public ResponseEntity<Void> registrarEntrada(
            @PathVariable UUID produtoId,
            @Valid @RequestBody RegistrarEntradaRequest request) {
        estoqueService.registrarEntrada(produtoId, request.quantidade(), request.observacao());
        return ResponseEntity.noContent().build(); // 204
    }

    @GetMapping("/produtos/{produtoId}/estoque")
    public SaldoResponse consultar(@PathVariable UUID produtoId) {
        return new SaldoResponse(produtoId, estoqueService.consultarSaldo(produtoId));
    }
}
