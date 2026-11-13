package br.com.organizacao.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.organizacao.domain.entity.Professor;
import jakarta.persistence.LockModeType;

public interface ProfessorRepository extends JpaRepository<Professor, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Professor p where p.id = :id")
    Optional<Professor> buscarPorIdComLock(@Param("id") Long id);
}
