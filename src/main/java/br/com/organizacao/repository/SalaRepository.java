package br.com.organizacao.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.organizacao.domain.entity.Sala;
import jakarta.persistence.LockModeType;

public interface SalaRepository extends JpaRepository<Sala, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Sala s where s.id = :id")
    Optional<Sala> buscarPorIdComLock(@Param("id") Long id);
}
