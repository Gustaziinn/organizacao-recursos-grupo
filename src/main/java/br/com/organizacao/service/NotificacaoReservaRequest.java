package br.com.organizacao.service;

import java.time.LocalDateTime;

import br.com.organizacao.domain.entity.TipoRecurso;

public record NotificacaoReservaRequest(
        Long reservaId,
        Long recursoId,
        TipoRecurso tipoRecurso,
        LocalDateTime inicio,
        LocalDateTime fim,
        Long solicitanteId) {
}
