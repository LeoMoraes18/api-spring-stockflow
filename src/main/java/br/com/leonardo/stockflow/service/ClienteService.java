package br.com.leonardo.stockflow.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.leonardo.stockflow.domain.Cliente;
import br.com.leonardo.stockflow.dto.ClienteResponse;
import br.com.leonardo.stockflow.dto.CriarClienteRequest;
import br.com.leonardo.stockflow.exception.DocumentoJaCadastradoException;
import br.com.leonardo.stockflow.exception.RecursoNaoEncontradoException;
import br.com.leonardo.stockflow.repository.ClienteRepository;

@Service 
public class ClienteService {
    
    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    @Transactional
    public ClienteResponse criar(CriarClienteRequest request) {
        if (request.documento() != null
                && clienteRepository.existsByDocumentoAndDeletedAtIsNull(request.documento())) {
            
            throw new DocumentoJaCadastradoException(request.documento());
        }
        Cliente cliente = new Cliente(request.nome(), request.documento());
        return toResponse(clienteRepository.save(cliente));
    }

    @Transactional(readOnly =  true)
    public ClienteResponse buscaPorId(UUID id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente", id));
        
        return toResponse(cliente);
    }

    @Transactional(readOnly = true)
    public Page<ClienteResponse> listar(Pageable pageable) {
        return clienteRepository.findAll(pageable).map(this::toResponse);
    }

    private ClienteResponse toResponse(Cliente cliente) {
        return new ClienteResponse(
            cliente.getId(),
            cliente.getNome(),
            cliente.getDocumento()
        );
    }
}
