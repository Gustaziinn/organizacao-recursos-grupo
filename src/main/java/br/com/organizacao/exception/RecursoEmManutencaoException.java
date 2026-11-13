package br.com.organizacao.exception;

public class RecursoEmManutencaoException extends RuntimeException {

    public RecursoEmManutencaoException() {
        super("O recurso está em manutenção.");
    }
}
