package br.com.organizacao.exception;

public class ConflitoReservaException extends RuntimeException {

    public ConflitoReservaException() {
        super("O recurso já possui uma reserva nesse intervalo.");
    }
}
