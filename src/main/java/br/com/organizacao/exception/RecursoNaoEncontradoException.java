package br.com.organizacao.exception;

public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(Long recursoId) {
        super("Recurso não encontrado: " + recursoId);
    }
}
