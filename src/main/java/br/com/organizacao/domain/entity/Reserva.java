package br.com.organizacao.domain.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.Check;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "reservas")
@Check(name = "ck_reserva_periodo", constraints = "fim > inicio")
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "recurso_id", nullable = false)
    private Long recursoId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_recurso", nullable = false)
    private TipoRecurso tipoRecurso;

    @Column(nullable = false)
    private LocalDateTime inicio;

    @Column(nullable = false)
    private LocalDateTime fim;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoReserva estado;

    @Column(name = "solicitante_id", nullable = false)
    private Long solicitanteId;

    protected Reserva() {
    }

    public Reserva(
            Long recursoId,
            TipoRecurso tipoRecurso,
            LocalDateTime inicio,
            LocalDateTime fim,
            EstadoReserva estado,
            Long solicitanteId) {
        this.recursoId = recursoId;
        this.tipoRecurso = tipoRecurso;
        this.inicio = inicio;
        this.fim = fim;
        this.estado = estado;
        this.solicitanteId = solicitanteId;
    }

    public Long getId() {
        return id;
    }

    public Long getRecursoId() {
        return recursoId;
    }

    public TipoRecurso getTipoRecurso() {
        return tipoRecurso;
    }

    public LocalDateTime getInicio() {
        return inicio;
    }

    public LocalDateTime getFim() {
        return fim;
    }

    public EstadoReserva getEstado() {
        return estado;
    }

    public void setEstado(EstadoReserva estado) {
        this.estado = estado;
    }

    public Long getSolicitanteId() {
        return solicitanteId;
    }
}
