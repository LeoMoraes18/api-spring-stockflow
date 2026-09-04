package br.com.leonardo.stockflow.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.leonardo.stockflow.domain.Cliente;
import br.com.leonardo.stockflow.domain.ItemPedido;
import br.com.leonardo.stockflow.domain.Pedido;
import br.com.leonardo.stockflow.domain.Produto;
import br.com.leonardo.stockflow.domain.StatusPedido;
import br.com.leonardo.stockflow.dto.CriarPedidoRequest;
import br.com.leonardo.stockflow.dto.PedidoResponse;
import br.com.leonardo.stockflow.exception.RecursoNaoEncontradoException;
import br.com.leonardo.stockflow.exception.TransicaoInvalidaException;
import br.com.leonardo.stockflow.repository.ClienteRepository;
import br.com.leonardo.stockflow.repository.PedidoRepository;
import br.com.leonardo.stockflow.repository.ProdutoRepository;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ClienteRepository clienteRepository;
    private final ProdutoRepository produtoRepository;
    private final EstoqueService estoqueService;

    public PedidoService(
            PedidoRepository pedidoRepository,
            ClienteRepository clienteRepository,
            ProdutoRepository produtoRepository,
            EstoqueService estoqueService) {
        this.pedidoRepository = pedidoRepository;
        this.clienteRepository = clienteRepository;
        this.produtoRepository = produtoRepository;
        this.estoqueService = estoqueService;
    }

    @Transactional
    public PedidoResponse criar(CriarPedidoRequest request) {
        Cliente cliente = clienteRepository.findById(request.clienteId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente", request.clienteId()));
        
        Pedido pedido = new Pedido(gerarNumeroPedido(), cliente);

        for (CriarPedidoRequest.ItemPedidoRequest itemRequest : request.itens()) {
            Produto produto = produtoRepository.findById(itemRequest.produtoId())
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Produto", itemRequest.produtoId()));
            
            pedido.adicionarItem(new ItemPedido(
                    produto,
                    itemRequest.quantidade(),
                    itemRequest.valorDesconto(),
                    itemRequest.valorAcrescimo()));
        }

        return toResponse(pedidoRepository.save(pedido));
        
    }

    @Transactional(readOnly = true)
    public PedidoResponse buscarPorId(UUID id) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pedido", id));
        
        return toResponse(pedido);
    }

    private String gerarNumeroPedido() {
        return "PED-%08d".formatted(pedidoRepository.proximoNumero());
    }

    private PedidoResponse toResponse(Pedido pedido) {
        List<PedidoResponse.ItemResponse> itens = pedido.getItens().stream()
                .map(item -> new PedidoResponse.ItemResponse(
                        item.getId(),
                        item.getProduto().getId(),
                        item.getProduto().getNome(),
                        item.getUnidadeMedida(),
                        item.getQuantidade(),
                        item.getValorUnitario(),
                        item.getValorDesconto(),
                        item.getValorAcrescimo(),
                        item.getValorTotal()))
                .toList();

        return new PedidoResponse(
                pedido.getId(),
                pedido.getNumeroPedido(),
                pedido.getCliente().getId(),
                pedido.getCliente().getNome(),
                pedido.getData(),
                pedido.getStatus(),
                pedido.getValorTotal(),
                itens);
    }

    /**
     * ABERTO -> CONFIRMADO. E aqui que o estoque e efetivamente baixado.
     *
     * Uma unica transacao cobre a validacao, a baixa de TODOS os itens e a
     * mudanca de status: se o terceiro item nao tiver saldo, os dois primeiros
     * sao desfeitos e o pedido continua ABERTO. Nunca existe pedido
     * confirmado pela metade.
     */
    @Transactional
    public PedidoResponse confirmar(UUID pedidoId) {

        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pedido", pedidoId));

        if (!pedido.getStatus().podeIrPara(StatusPedido.CONFIRMADO)) {
            throw new TransicaoInvalidaException(pedido.getStatus(), StatusPedido.CONFIRMADO);
        }

        if (pedido.getItens().isEmpty()) {
            throw new IllegalStateException("Pedido sem itens nao pode ser confirmado");
        }

        // Chamada entre beans distintos: passa pelo proxy, entao a transacao
        // atual e propagada (REQUIRED). Nao abre transacao nova.
        for (ItemPedido item : pedido.getItens()) {
            estoqueService.registrarSaida(item);
        }

        pedido.setStatus(StatusPedido.CONFIRMADO);
        pedido.setUpdatedAt(Instant.now());

        return toResponse(pedidoRepository.save(pedido));
    }

    @Transactional
    public void mudarStatus(UUID pedidoId, StatusPedido novoStatus) {

        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pedido", pedidoId));

        if (!pedido.getStatus().podeIrPara(novoStatus)) {
            throw new TransicaoInvalidaException(pedido.getStatus(), novoStatus);
        }

        pedido.setStatus(novoStatus);
        pedido.setUpdatedAt(Instant.now());
        pedidoRepository.save(pedido);
    }
}
