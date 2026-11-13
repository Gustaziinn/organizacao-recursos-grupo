package br.com.organizacao.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.organizacao.domain.entity.Auditoria;
import br.com.organizacao.domain.entity.EstadoReserva;
import br.com.organizacao.repository.AuditoriaRepository;

@Service
public class AuditoriaService {

    private final AuditoriaRepository auditoriaRepository;

    public AuditoriaService(AuditoriaRepository auditoriaRepository) {
        this.auditoriaRepository = auditoriaRepository;
    }

    @Transactional
    public void registrarMudancaEstado(Long reservaId, EstadoReserva anterior, EstadoReserva novo) {
        if (anterior != novo) {
            auditoriaRepository.save(new Auditoria(reservaId, anterior, novo));
        }
    }
}
