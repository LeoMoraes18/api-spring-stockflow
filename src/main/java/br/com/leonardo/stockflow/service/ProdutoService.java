package br.com.leonardo.stockflow.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.leonardo.stockflow.domain.Produto;
import br.com.leonardo.stockflow.dto.CriarProdutoRequest;
import br.com.leonardo.stockflow.dto.ProdutoResponse;
import br.com.leonardo.stockflow.exception.RecursoNaoEncontradoException;
import br.com.leonardo.stockflow.repository.ProdutoRepository;

@Service
public class ProdutoService {
    
    private final ProdutoRepository produtoRepository;

    public ProdutoService(ProdutoRepository produtoRepository) {
        this.produtoRepository = produtoRepository;
    }

    @Transactional
    public ProdutoResponse criar(CriarProdutoRequest request) {
        Produto produto = new Produto(
                request.nome(),
                request.unidadeMedida(),
                request.precoVenda());
        produto.setDescricao(request.descricao());
        produto.setPrecoCompra(request.precoCompra());

        return toResponse(produtoRepository.save(produto));
    }
    
    @Transactional(readOnly = true)
    public ProdutoResponse buscaPorId(UUID id) {
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto", id));

        return toResponse(produto);
    }

    @Transactional(readOnly = true)
    public Page<ProdutoResponse> listar(Pageable pageable) {
        return produtoRepository.findAll(pageable).map(this::toResponse);
    }

    private ProdutoResponse toResponse(Produto produto) {
        return new ProdutoResponse(
            produto.getId(),
            produto.getNome(),
            produto.getDescricao(),
            produto.getUnidadeMedida(),
            produto.getPrecoVenda(),
            produto.getCreatedAt());
    }
}
