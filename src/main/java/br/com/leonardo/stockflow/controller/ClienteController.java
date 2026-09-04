package br.com.leonardo.stockflow.controller;

import java.net.URI;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.leonardo.stockflow.dto.ClienteResponse;
import br.com.leonardo.stockflow.dto.CriarClienteRequest;
import br.com.leonardo.stockflow.service.ClienteService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/clientes")
public class ClienteController {
    
    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @PostMapping
    public ResponseEntity<ClienteResponse> criar(@Valid @RequestBody CriarClienteRequest request) {
        ClienteResponse criado = clienteService.criar(request);

        return ResponseEntity
                .created(URI.create("/clientes/" + criado.id()))
                .body(criado);
    }

    @GetMapping("/{id}")
    public ClienteResponse buscar(@PathVariable UUID id) {
        return clienteService.buscaPorId(id);
    }
    
    @GetMapping
    public Page<ClienteResponse> listar(
            @PageableDefault(size = 20, sort = "nome") Pageable pageable) {
        return clienteService.listar(pageable);
    }
    
    
}
