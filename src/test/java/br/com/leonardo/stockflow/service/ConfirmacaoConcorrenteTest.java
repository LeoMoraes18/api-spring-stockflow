package br.com.leonardo.stockflow.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import br.com.leonardo.stockflow.TestcontainersConfig;
import br.com.leonardo.stockflow.domain.Cliente;
import br.com.leonardo.stockflow.domain.ItemPedido;
import br.com.leonardo.stockflow.domain.Pedido;
import br.com.leonardo.stockflow.domain.Produto;
import br.com.leonardo.stockflow.domain.StatusPedido;
import br.com.leonardo.stockflow.repository.ClienteRepository;
import br.com.leonardo.stockflow.repository.MovimentoEstoqueRepository;
import br.com.leonardo.stockflow.repository.PedidoRepository;
import br.com.leonardo.stockflow.repository.ProdutoRepository;
import br.com.leonardo.stockflow.repository.SaldoEstoqueRepository;

/**
 * NAO use @Transactional nesta classe. O teste precisa COMMITAR o cenario
 * para que as outras threads enxerguem os dados - uma transacao de teste
 * ficaria isolada e as threads nao veriam nada. Por isso a limpeza e manual.
 */

@Import(TestcontainersConfig.class)
@SpringBootTest
@DisplayName("Baixa de estoque sob concorrencia")
class ConfirmacaoConcorrenteTest {

    @Autowired
    private PedidoService pedidoService;
    @Autowired
    private EstoqueService estoqueService;
    @Autowired
    private ClienteRepository clienteRepository;
    @Autowired
    private ProdutoRepository produtoRepository;
    @Autowired
    private PedidoRepository pedidoRepository;
    @Autowired
    private SaldoEstoqueRepository saldoEstoqueRepository;
    @Autowired
    private MovimentoEstoqueRepository movimentoEstoqueRepository;

    private UUID produtoId;
    private UUID pedidoAId;
    private UUID pedidoBId;

    @BeforeEach
    void prepararCenario() {
        Cliente cliente = clienteRepository.save(new Cliente("Cliente de teste", null));
        Produto produto = produtoRepository.save(
                new Produto("Produto disputado", "UN", new BigDecimal("10.00")));
        produtoId = produto.getId();

        // UMA unica unidade em estoque para dois pedidos de uma unidade cada.
        estoqueService.registrarEntrada(produtoId, new BigDecimal("1.000"), "carga do teste");

        pedidoAId = criarPedidoDeUmaUnidade(cliente, produto);
        pedidoBId = criarPedidoDeUmaUnidade(cliente, produto);
    }

    private UUID criarPedidoDeUmaUnidade(Cliente cliente, Produto produto) {
        String numero = "PED-" + UUID.randomUUID().toString().substring(0, 8);
        Pedido pedido = new Pedido(numero, cliente);
        pedido.adicionarItem(new ItemPedido(
                produto, BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO));
        return pedidoRepository.save(pedido).getId();
    }

    @AfterEach
    void limpar() {
        movimentoEstoqueRepository.deleteAll();
        pedidoRepository.deleteAll();
        saldoEstoqueRepository.deleteAll();
        produtoRepository.deleteAll();
        clienteRepository.deleteAll();
    }

    @Test
    @DisplayName("apenas um dos dois pedidos concorrentes consome a ultima unidade")
    void apenasUmPedidoConsomeAUltimaUnidade() throws InterruptedException {

        CountDownLatch largada = new CountDownLatch(1);
        CountDownLatch chegada = new CountDownLatch(2);
        AtomicInteger sucessos = new AtomicInteger();
        List<Throwable> falhas = Collections.synchronizedList(new ArrayList<>());

        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            for (UUID pedidoId : List.of(pedidoAId, pedidoBId)) {
                executor.submit(() -> {
                    try {
                        largada.await();          // as duas esperam o sinal
                        pedidoService.confirmar(pedidoId);
                        sucessos.incrementAndGet();
                    } catch (Throwable erro) {
                        falhas.add(erro);
                    } finally {
                        chegada.countDown();
                    }
                });
            }

            largada.countDown();                  // dispara as duas juntas
            assertThat(chegada.await(20, TimeUnit.SECONDS))
                    .as("as duas threads deveriam terminar")
                    .isTrue();
        } finally {
            executor.shutdownNow();
        }

        falhas.forEach(e -> System.out.println(
                ">> thread perdedora: " + e.getClass().getSimpleName() + " - " + e.getMessage()));

        assertThat(sucessos.get()).as("exatamente um pedido deve ser confirmado").isEqualTo(1);
        assertThat(falhas).as("o outro deve falhar").hasSize(1);

        assertThat(estoqueService.consultarSaldo(produtoId))
                .as("saldo nunca pode ficar negativo")
                .isEqualByComparingTo("0.000");

        long confirmados = List.of(pedidoAId, pedidoBId).stream()
                .map(id -> pedidoRepository.findById(id).orElseThrow())
                .filter(p -> p.getStatus() == StatusPedido.CONFIRMADO)
                .count();
        assertThat(confirmados).as("so um pedido pode ter mudado de status").isEqualTo(1);
    }
}
