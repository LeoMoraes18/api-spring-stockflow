package br.com.leonardo.stockflow.exception;

public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(String recurso, Object id) {
        super("%s nao encontrado: %s".formatted(recurso, id));
    }
}
