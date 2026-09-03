package br.com.leonardo.stockflow.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class TratadorDeErros {
    
    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ProblemDetail naoEncontrado(RecursoNaoEncontradoException e) {
        ProblemDetail problema = 
                ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND,  e.getMessage());
        
        problema.setTitle("Recurso não encontrado");
        return problema;
    }

    @ExceptionHandler(EstoqueInsuficienteException.class)
    public ProblemDetail estoqueInsuficiente(EstoqueInsuficienteException e) {
        ProblemDetail problema =
                ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
        problema.setTitle("Estoque insuficiente");
        // Os campos que a exceção carrega viram dados na resposta.
        problema.setProperty("produtoId", e.getProdutoId());
        problema.setProperty("disponivel", e.getDisponivel());
        problema.setProperty("solicitado", e.getSolicitado());
        return problema;
    }

    @ExceptionHandler(TransicaoInvalidaException.class)
    public ProblemDetail transicaoInvalida(TransicaoInvalidaException e) {
        ProblemDetail problema =
                ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
        problema.setTitle("Transição de status inválida");
        problema.setProperty("statusAtual", e.getAtual());
        problema.setProperty("transicoesPermitidas", e.getAtual().proximosPermitidos());
        return problema;
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ProblemDetail conflitoDeConcorrencia(ObjectOptimisticLockingFailureException e) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
                "O registro foi alterado por outra operação simultânea. Tente novamente.");
        problema.setTitle("Conflito de concorrência");
        return problema;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail validacao(MethodArgumentNotValidException e) {
        Map<String, String> erros = new HashMap<>();
        e.getBindingResult().getFieldErrors()
                .forEach(erro -> erros.put(erro.getField(), erro.getDefaultMessage()));

        ProblemDetail problema = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, "Um ou mais campos são inválidos");
        problema.setTitle("Erro de validação");
        problema.setProperty("erros", erros);
        return problema;
    }
}
