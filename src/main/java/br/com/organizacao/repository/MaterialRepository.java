package br.com.organizacao.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.organizacao.domain.entity.Material;
import jakarta.persistence.LockModeType;

public interface MaterialRepository extends JpaRepository<Material, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from Material m where m.id = :id")
    Optional<Material> buscarPorIdComLock(@Param("id") Long id);
}
