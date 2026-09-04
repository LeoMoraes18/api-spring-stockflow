package br.com.leonardo.stockflow.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.leonardo.stockflow.dto.CriarPedidoRequest;
import br.com.leonardo.stockflow.dto.PedidoResponse;
import br.com.leonardo.stockflow.service.PedidoService;
import jakarta.validation.Valid;

import java.net.URI;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;



@RestController
@RequestMapping("/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @PostMapping
    public ResponseEntity<PedidoResponse> criar(@Valid @RequestBody CriarPedidoRequest request) {
        PedidoResponse criado = pedidoService.criar(request);        
        return ResponseEntity
                .created(URI.create("/pedidos/" + criado.id()))
                .body(criado);
    }

    @GetMapping("/{id}")
    public PedidoResponse busca(@PathVariable UUID id) {
        return pedidoService.buscarPorId(id);
    }

    @PostMapping("/{id}/confirmar")
    public PedidoResponse confirmar(@PathVariable UUID id) {
        return pedidoService.confirmar(id);
    }
    
    
    
}
