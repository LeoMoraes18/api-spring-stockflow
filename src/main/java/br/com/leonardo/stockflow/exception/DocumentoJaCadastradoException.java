package br.com.leonardo.stockflow.exception;

public class DocumentoJaCadastradoException extends RuntimeException{

    private final String documento;

    public DocumentoJaCadastradoException(String documento) {
        super("Já existe um cliente ativo com o documento %s".formatted(documento));
        this.documento = documento;
    }

    public String getDocumento() {
        return documento;
    }
    
}
