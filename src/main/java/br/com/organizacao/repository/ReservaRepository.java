package br.com.organizacao.repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.organizacao.domain.entity.EstadoReserva;
import br.com.organizacao.domain.entity.Reserva;
import br.com.organizacao.domain.entity.TipoRecurso;
import jakarta.persistence.LockModeType;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select r from Reserva r
            where r.recursoId = :recursoId
              and r.tipoRecurso = :tipoRecurso
              and r.estado in :estados
              and r.inicio < :fim
              and r.fim > :inicio
            """)
    List<Reserva> buscarConflitantes(
            @Param("recursoId") Long recursoId,
            @Param("tipoRecurso") TipoRecurso tipoRecurso,
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim,
            @Param("estados") Collection<EstadoReserva> estados);

    List<Reserva> findAllByRecursoIdAndTipoRecurso(Long recursoId, TipoRecurso tipoRecurso);
}
