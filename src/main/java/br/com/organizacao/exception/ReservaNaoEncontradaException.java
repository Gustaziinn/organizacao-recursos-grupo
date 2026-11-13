package br.com.organizacao.exception;

public class ReservaNaoEncontradaException extends RuntimeException {

    public ReservaNaoEncontradaException(Long reservaId) {
        super("Reserva não encontrada: " + reservaId);
    }
}
