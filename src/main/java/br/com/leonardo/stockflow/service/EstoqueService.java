package br.com.leonardo.stockflow.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.leonardo.stockflow.domain.ItemPedido;
import br.com.leonardo.stockflow.domain.MovimentoEstoque;
import br.com.leonardo.stockflow.domain.Produto;
import br.com.leonardo.stockflow.domain.SaldoEstoque;
import br.com.leonardo.stockflow.domain.TipoMovimento;
import br.com.leonardo.stockflow.exception.EstoqueInsuficienteException;
import br.com.leonardo.stockflow.exception.RecursoNaoEncontradoException;
import br.com.leonardo.stockflow.repository.MovimentoEstoqueRepository;
import br.com.leonardo.stockflow.repository.ProdutoRepository;
import br.com.leonardo.stockflow.repository.SaldoEstoqueRepository;

@Service
public class EstoqueService {

    private final ProdutoRepository produtoRepository;
    private final SaldoEstoqueRepository saldoEstoqueRepository;
    private final MovimentoEstoqueRepository movimentoEstoqueRepository;

    // Construtor unico: o Spring injeta sozinho, sem @Autowired.
    public EstoqueService(ProdutoRepository produtoRepository,
                          SaldoEstoqueRepository saldoEstoqueRepository,
                          MovimentoEstoqueRepository movimentoEstoqueRepository) {
        this.produtoRepository = produtoRepository;
        this.saldoEstoqueRepository = saldoEstoqueRepository;
        this.movimentoEstoqueRepository = movimentoEstoqueRepository;
    }

    /**
     * Reposicao ou ajuste positivo de estoque. Nao vem de venda, por isso
     * o movimento e gravado sem item_pedido.
     *
     * Tudo acontece em UMA transacao: ou saldo e movimento sao gravados
     * juntos, ou nada e gravado.
     */
    @Transactional
    public void registrarEntrada(UUID produtoId, BigDecimal quantidade, String observacao) {

        if (quantidade == null || quantidade.signum() <= 0) {
            throw new IllegalArgumentException("Quantidade de entrada deve ser maior que zero");
        }

        Produto produto = produtoRepository.findById(produtoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto", produtoId));

        // Primeira entrada de um produto ainda nao tem linha de saldo.
        SaldoEstoque saldo = saldoEstoqueRepository.findById(produtoId)
                .orElseGet(() -> new SaldoEstoque(produto, BigDecimal.ZERO));

        saldo.setQuantidade(saldo.getQuantidade().add(quantidade));
        saldo.setUpdatedAt(Instant.now());
        saldoEstoqueRepository.save(saldo);

        MovimentoEstoque movimento = new MovimentoEstoque(
                produto,
                null,                      // entrada nao vem de item de pedido
                quantidade,
                TipoMovimento.ENTRADA,
                observacao);
        movimentoEstoqueRepository.save(movimento);
    }

    @Transactional(readOnly = true)
    public BigDecimal consultarSaldo(UUID produtoId) {
        return saldoEstoqueRepository.findById(produtoId)
                .map(SaldoEstoque::getQuantidade)
                .orElse(BigDecimal.ZERO);
    }

    @Transactional
    public void registrarSaida(ItemPedido item){

        if (item.getQuantidade() == null || item.getQuantidade().signum() <= 0) {
            throw new IllegalArgumentException("Quantidade de saída deve ser maior que zero");
        }
        
        SaldoEstoque saldo = saldoEstoqueRepository.findById(item.getProduto().getId())
                .orElseThrow(() -> new EstoqueInsuficienteException(item.getProduto().getId(), BigDecimal.ZERO, item.getQuantidade()));

        if (saldo.getQuantidade().compareTo(item.getQuantidade()) < 0) {
            throw new EstoqueInsuficienteException(item.getProduto().getId(), saldo.getQuantidade(), item.getQuantidade());
        }
        
        saldo.setQuantidade(saldo.getQuantidade().subtract(item.getQuantidade()));
        saldo.setUpdatedAt(Instant.now());
        saldoEstoqueRepository.save(saldo);

        MovimentoEstoque movimentoEstoque = new MovimentoEstoque(
                item.getProduto(),
                item,
                item.getQuantidade(),
                TipoMovimento.SAIDA,
                "Saida com base no pedido numero: " + item.getPedido().getNumeroPedido());
        
        movimentoEstoqueRepository.save(movimentoEstoque);
        
    }
}
