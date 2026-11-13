package br.com.organizacao.domain.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "auditoria")
public class Auditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reserva_id", nullable = false)
    private Long reservaId;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_anterior", nullable = false)
    private EstadoReserva estadoAnterior;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_novo", nullable = false)
    private EstadoReserva estadoNovo;

    @Column(name = "alterado_em", nullable = false)
    private LocalDateTime alteradoEm;

    protected Auditoria() {
    }

    public Auditoria(Long reservaId, EstadoReserva estadoAnterior, EstadoReserva estadoNovo) {
        this.reservaId = reservaId;
        this.estadoAnterior = estadoAnterior;
        this.estadoNovo = estadoNovo;
        this.alteradoEm = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Long getReservaId() {
        return reservaId;
    }

    public EstadoReserva getEstadoAnterior() {
        return estadoAnterior;
    }

    public EstadoReserva getEstadoNovo() {
        return estadoNovo;
    }

    public LocalDateTime getAlteradoEm() {
        return alteradoEm;
    }
}
