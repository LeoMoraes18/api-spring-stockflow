package br.com.leonardo.stockflow.service;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.leonardo.stockflow.domain.ItemPedido;
import br.com.leonardo.stockflow.domain.Pedido;
import br.com.leonardo.stockflow.domain.StatusPedido;
import br.com.leonardo.stockflow.exception.RecursoNaoEncontradoException;
import br.com.leonardo.stockflow.exception.TransicaoInvalidaException;
import br.com.leonardo.stockflow.repository.PedidoRepository;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final EstoqueService estoqueService;

    public PedidoService(PedidoRepository pedidoRepository, EstoqueService estoqueService) {
        this.pedidoRepository = pedidoRepository;
        this.estoqueService = estoqueService;
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
    public void confirmar(UUID pedidoId) {

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
        pedidoRepository.save(pedido);
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
