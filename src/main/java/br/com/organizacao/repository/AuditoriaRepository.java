package br.com.organizacao.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.organizacao.domain.entity.Auditoria;

public interface AuditoriaRepository extends JpaRepository<Auditoria, Long> {

    List<Auditoria> findAllByReservaIdOrderByAlteradoEmAsc(Long reservaId);
}
