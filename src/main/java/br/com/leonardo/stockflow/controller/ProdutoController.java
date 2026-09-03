package br.com.leonardo.stockflow.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.leonardo.stockflow.dto.CriarProdutoRequest;
import br.com.leonardo.stockflow.dto.ProdutoResponse;
import br.com.leonardo.stockflow.service.ProdutoService;
import jakarta.validation.Valid;

import java.net.URI;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/produtos")
public class ProdutoController {
    
    private final ProdutoService produtoService;

    public ProdutoController(ProdutoService produtoService) {
        this.produtoService = produtoService;
    }

    @PostMapping
    public ResponseEntity<ProdutoResponse> criar(@Valid @RequestBody CriarProdutoRequest request) {
        ProdutoResponse criado = produtoService.criar(request);
               
        return ResponseEntity
                .created(URI.create("/produtos/" + criado.id()))
                .body(criado);
    }
    
    @GetMapping("/{id}")
    public ProdutoResponse buscar(@PathVariable UUID id) {
        return produtoService.buscaPorId(id);
    }

    @GetMapping
    public Page<ProdutoResponse> listar(
            @PageableDefault(size = 20, sort = "nome") Pageable pageable) {
        return produtoService.listar(pageable);
    }
}