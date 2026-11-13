package br.com.organizacao.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;

import br.com.organizacao.domain.entity.EstadoReserva;
import br.com.organizacao.domain.entity.Reserva;
import br.com.organizacao.domain.entity.TipoRecurso;
import br.com.organizacao.exception.ConflitoReservaException;
import br.com.organizacao.exception.ReservaNaoEncontradaException;
import br.com.organizacao.repository.ReservaRepository;

@Service
public class ReservaService {

    private static final List<EstadoReserva> ESTADOS_QUE_RESERVAM_RECURSO = List.of(
            EstadoReserva.SOLICITADA,
            EstadoReserva.APROVADA,
            EstadoReserva.EM_USO);

    private final ReservaRepository reservaRepository;
    private final RecursoLockService recursoLockService;
    private final AuditoriaService auditoriaService;
    private final NotificacaoClient notificacaoClient;

    public ReservaService(
            ReservaRepository reservaRepository,
            RecursoLockService recursoLockService,
            AuditoriaService auditoriaService,
            NotificacaoClient notificacaoClient) {
        this.reservaRepository = reservaRepository;
        this.recursoLockService = recursoLockService;
        this.auditoriaService = auditoriaService;
        this.notificacaoClient = notificacaoClient;
    }

    @Transactional
    public Reserva criarReserva(
            Long recursoId,
            TipoRecurso tipoRecurso,
            LocalDateTime inicio,
            LocalDateTime fim,
            Long solicitanteId) {
        Objects.requireNonNull(recursoId, "recursoId é obrigatório");
        Objects.requireNonNull(tipoRecurso, "tipoRecurso é obrigatório");
        Objects.requireNonNull(inicio, "inicio é obrigatório");
        Objects.requireNonNull(fim, "fim é obrigatório");
        Objects.requireNonNull(solicitanteId, "solicitanteId é obrigatório");
        if (!fim.isAfter(inicio)) {
            throw new IllegalArgumentException("fim deve ser posterior a inicio");
        }

        recursoLockService.bloquearEValidar(tipoRecurso, recursoId);
        if (!reservaRepository.buscarConflitantes(
                recursoId, tipoRecurso, inicio, fim, ESTADOS_QUE_RESERVAM_RECURSO).isEmpty()) {
            throw new ConflitoReservaException();
        }

        Reserva reserva = reservaRepository.saveAndFlush(new Reserva(
                recursoId,
                tipoRecurso,
                inicio,
                fim,
                EstadoReserva.SOLICITADA,
                solicitanteId));
        notificacaoClient.notificarReservaCriada(reserva);
        return reserva;
    }

    @Transactional
    public Reserva alterarEstado(Long reservaId, EstadoReserva novoEstado) {
        Objects.requireNonNull(novoEstado, "novoEstado é obrigatório");
        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new ReservaNaoEncontradaException(reservaId));
        EstadoReserva estadoAnterior = reserva.getEstado();
        if (estadoAnterior != novoEstado) {
            reserva.setEstado(novoEstado);
            auditoriaService.registrarMudancaEstado(reserva.getId(), estadoAnterior, novoEstado);
        }
        return reserva;
    }

    @Transactional
    @PreAuthorize("hasAnyRole('RESPONSAVEL', 'ADMINISTRADOR')")
    public Reserva aprovarReserva(Long reservaId) {
        return alterarEstado(reservaId, EstadoReserva.APROVADA);
    }
}
